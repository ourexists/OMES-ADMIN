package com.ourexists.omes.portal.rabbitmq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipRealtimeManager;
import com.ourexists.omes.device.feign.EquipRecordAlarmFeign;
import com.ourexists.omes.device.feign.EquipRecordOnlineFeign;
import com.ourexists.omes.device.feign.EquipRecordRunFeign;
import com.ourexists.omes.device.model.EquipRecordAlarmDto;
import com.ourexists.omes.device.model.EquipRecordOnlineDto;
import com.ourexists.omes.device.model.EquipRecordRunDto;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 将 {@code type=change} 的 MQ 负载在内存中按条数阈值聚合并调用 device 批量接口；未到阈值时按间隔刷盘，降低 Feign 次数。
 * <p>
 * 注意：单条消息在 listener 返回后即被 Rabbit 确认；聚合窗口内进程崩溃可能丢失尚未刷盘的已确认消息，可通过缩小 {@code batch-flush-ms} 缓解。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EquipStreamPersistChangeAggregator {

    private static final String FIELD_REALTIME = "realtime";

    private final ObjectMapper objectMapper;
    private final EquipRecordAlarmFeign equipRecordAlarmFeign;
    private final EquipRecordRunFeign equipRecordRunFeign;
    private final EquipRecordOnlineFeign equipRecordOnlineFeign;
    private final EquipRealtimeManager equipRealtimeManager;

    private final Object bufferLock = new Object();

    @Value("${omes.device.rabbitmq.equip-stream-persist-batch-size:32}")
    private int batchSize;

    @Value("${omes.device.rabbitmq.equip-stream-persist-batch-flush-ms:200}")
    private long batchFlushMs;

    private final List<EquipRecordAlarmDto> alarms = new ArrayList<>();
    private final List<EquipRecordRunDto> runs = new ArrayList<>();
    private final List<EquipRecordOnlineDto> onlines = new ArrayList<>();
    private final List<EquipRealtime> realtimes = new ArrayList<>();

    private int bufferedChangeMessages;

    private ScheduledExecutorService flushScheduler;
    private ScheduledFuture<?> debouncedFlush;

    @PostConstruct
    void startScheduler() {
        if (batchSize < 1) {
            batchSize = 1;
        }
        if (batchFlushMs < 20L) {
            batchFlushMs = 20L;
        }
        flushScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "equip-stream-persist-change-flush");
            t.setDaemon(true);
            return t;
        });
    }

    @PreDestroy
    void shutdown() {
        Takeaway leftover;
        synchronized (bufferLock) {
            cancelDebouncedFlushLocked();
            if (alarms.isEmpty() && runs.isEmpty() && onlines.isEmpty() && realtimes.isEmpty()) {
                leftover = null;
            } else {
                leftover = drainTakeawayLocked();
            }
        }
        if (leftover != null) {
            try {
                applyTakeaway(leftover);
            } catch (Exception e) {
                log.error("Equip stream persist: flush on shutdown failed", e);
            }
        }
        flushScheduler.shutdown();
        try {
            if (!flushScheduler.awaitTermination(8, TimeUnit.SECONDS)) {
                flushScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            flushScheduler.shutdownNow();
        }
    }

    /**
     * 合并一条 change JSON；达到 {@link #batchSize} 条 change 消息或定时到期时刷盘。
     */
    public void offer(JsonNode root) throws Exception {
        Takeaway takeaway;
        synchronized (bufferLock) {
            appendFromRoot(root);
            bufferedChangeMessages++;
            if (bufferedChangeMessages >= batchSize) {
                cancelDebouncedFlushLocked();
                takeaway = drainTakeawayLocked();
            } else {
                scheduleDebouncedFlushLocked();
                return;
            }
        }
        applyTakeaway(takeaway);
    }

    private void appendFromRoot(JsonNode root) throws Exception {
        if (root.hasNonNull("alarm")) {
            alarms.add(objectMapper.treeToValue(root.get("alarm"), EquipRecordAlarmDto.class));
        }
        if (root.hasNonNull("run")) {
            runs.add(objectMapper.treeToValue(root.get("run"), EquipRecordRunDto.class));
        }
        if (root.hasNonNull("online")) {
            onlines.add(objectMapper.treeToValue(root.get("online"), EquipRecordOnlineDto.class));
        }
        if (root.hasNonNull(FIELD_REALTIME)) {
            EquipRealtime rt = objectMapper.treeToValue(root.get(FIELD_REALTIME), EquipRealtime.class);
            if (rt != null && StringUtils.hasText(rt.getSelfCode())) {
                realtimes.add(rt);
            }
        }
    }

    private void scheduleDebouncedFlushLocked() {
        cancelDebouncedFlushLocked();
        debouncedFlush = flushScheduler.schedule(this::debouncedFlushRun, batchFlushMs, TimeUnit.MILLISECONDS);
    }

    private void cancelDebouncedFlushLocked() {
        if (debouncedFlush != null) {
            debouncedFlush.cancel(false);
            debouncedFlush = null;
        }
    }

    private void debouncedFlushRun() {
        try {
            Takeaway takeaway;
            synchronized (bufferLock) {
                cancelDebouncedFlushLocked();
                if (alarms.isEmpty() && runs.isEmpty() && onlines.isEmpty() && realtimes.isEmpty()) {
                    return;
                }
                takeaway = drainTakeawayLocked();
            }
            applyTakeaway(takeaway);
        } catch (Exception e) {
            log.error("Equip stream persist: debounced flush failed", e);
        }
    }

    private Takeaway drainTakeawayLocked() {
        Takeaway t = new Takeaway(
                new ArrayList<>(alarms),
                new ArrayList<>(runs),
                new ArrayList<>(onlines),
                new ArrayList<>(realtimes));
        alarms.clear();
        runs.clear();
        onlines.clear();
        realtimes.clear();
        bufferedChangeMessages = 0;
        return t;
    }

    private void applyTakeaway(Takeaway t) throws Exception {
        UserContext.defaultTenant();
        if (!t.alarms.isEmpty()) {
            RemoteHandleUtils.getDataFormResponse(equipRecordAlarmFeign.addBatch(t.alarms));
        }
        if (!t.runs.isEmpty()) {
            RemoteHandleUtils.getDataFormResponse(equipRecordRunFeign.addBatch(t.runs));
        }
        if (!t.onlines.isEmpty()) {
            RemoteHandleUtils.getDataFormResponse(equipRecordOnlineFeign.addBatch(t.onlines));
        }
        for (EquipRealtime rt : t.realtimes) {
            UserContext.getTenant().setTenantId(rt.getTenantId());
            equipRealtimeManager.addOrUpdate(rt);
        }
    }

    private record Takeaway(
            List<EquipRecordAlarmDto> alarms,
            List<EquipRecordRunDto> runs,
            List<EquipRecordOnlineDto> onlines,
            List<EquipRealtime> realtimes) {
    }
}
