package com.ourexists.omes.portal.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipRealtimeManager;
import com.ourexists.omes.device.feign.*;
import com.ourexists.omes.device.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 状态快照 / 采集快照 经 Aggregator 攒批后调用 {@link EquipStateSnapshotFeign#addBatch}、{@link EquipCollectFeign#addBatch}。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EquipPersistBatchWriter {

    private static final String FIELD_SNAPSHOT = "snapshot";
    private static final String FIELD_COLLECT = "collect";
    private static final String FIELD_REALTIME = "realtime";

    private final ObjectMapper objectMapper;
    private final EquipStateSnapshotFeign equipStateSnapshotFeign;
    private final EquipCollectFeign equipCollectFeign;
    private final EquipRecordAlarmFeign equipRecordAlarmFeign;
    private final EquipRecordRunFeign equipRecordRunFeign;
    private final EquipRecordOnlineFeign equipRecordOnlineFeign;


    public void writeAggregatedBatch(Collection<?> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return;
        }
        List<EquipRecordAlarmDto> alarms = new ArrayList<>();
        List<EquipRecordRunDto> runs = new ArrayList<>();
        List<EquipRecordOnlineDto> onlines = new ArrayList<>();
        List<EquipRealtime> realtimes = new ArrayList<>();
        try {
            for (Object o : payloads) {
                JsonNode root = unwrapPersistRoot(o);
                if (root == null) {
                    continue;
                }
                appendFromRoot(root, alarms, runs, onlines, realtimes);
            }
            UserContext.defaultTenant();
            if (!alarms.isEmpty()) {
                RemoteHandleUtils.getDataFormResponse(equipRecordAlarmFeign.addBatch(alarms));
            }
            if (!runs.isEmpty()) {
                RemoteHandleUtils.getDataFormResponse(equipRecordRunFeign.addBatch(runs));
            }
            if (!onlines.isEmpty()) {
                RemoteHandleUtils.getDataFormResponse(equipRecordOnlineFeign.addBatch(onlines));
            }
        } catch (Exception e) {
            log.error("Equip stream persist: aggregated batch write failed, messageCount={}", payloads.size(), e);
        } finally {
            UserContext.remove();
        }
    }

    private void appendFromRoot(
            JsonNode root,
            List<EquipRecordAlarmDto> alarms,
            List<EquipRecordRunDto> runs,
            List<EquipRecordOnlineDto> onlines,
            List<EquipRealtime> realtimes) throws Exception {
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


    public void writeStateSnapshotBatch(Collection<?> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return;
        }
        List<EquipStateSnapshotDto> dtos = new ArrayList<>();
        try {
            for (Object o : payloads) {
                JsonNode root = unwrapPersistRoot(o);
                if (root == null) {
                    continue;
                }
                if (root.hasNonNull(FIELD_SNAPSHOT)) {
                    dtos.add(objectMapper.treeToValue(root.get(FIELD_SNAPSHOT), EquipStateSnapshotDto.class));
                }
            }
            UserContext.defaultTenant();
            if (!dtos.isEmpty()) {
                RemoteHandleUtils.getDataFormResponse(equipStateSnapshotFeign.addBatch(dtos));
            }
        } catch (Exception e) {
            log.error("Equip stream persist: state snapshot batch write failed, messageCount={}", payloads.size(), e);
        } finally {
            UserContext.remove();
        }
    }

    public void writeCollectSnapshotBatch(Collection<?> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return;
        }
        List<EquipCollectDto> dtos = new ArrayList<>();
        try {
            for (Object o : payloads) {
                JsonNode root = unwrapPersistRoot(o);
                if (root == null) {
                    continue;
                }
                if (root.hasNonNull(FIELD_COLLECT)) {
                    dtos.add(objectMapper.treeToValue(root.get(FIELD_COLLECT), EquipCollectDto.class));
                }
            }
            UserContext.defaultTenant();
            if (!dtos.isEmpty()) {
                RemoteHandleUtils.getDataFormResponse(equipCollectFeign.addBatch(dtos));
            }
        } catch (Exception e) {
            log.error("Equip stream persist: collect snapshot batch write failed, messageCount={}", payloads.size(), e);
        } finally {
            UserContext.remove();
        }
    }

    private static JsonNode unwrapPersistRoot(Object o) {
        if (o instanceof JsonNode j) {
            return j;
        }
        if (o instanceof EquipPersistMqEvent e) {
            return e.getRoot();
        }
        return null;
    }
}
