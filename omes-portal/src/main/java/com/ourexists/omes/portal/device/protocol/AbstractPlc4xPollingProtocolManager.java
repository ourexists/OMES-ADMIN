/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.device.protocol;

import com.alibaba.fastjson2.JSONObject;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.core.equip.protocol.ProtocolConnect;
import com.ourexists.omes.device.core.equip.protocol.ProtocolManager;
import com.ourexists.omes.device.feign.EquipFeign;
import com.ourexists.omes.device.feign.WorkshopFeign;
import com.ourexists.omes.device.model.*;
import com.ourexists.omes.portal.device.collect.PlcEquipDataParser;
import com.ourexists.omes.portal.device.collect.PlcWorkshopDataParser;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.PlcConnectionManager;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.plc4x.java.utils.cache.CachedPlcConnectionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * PLC4X 轮询协议管理器公共基类。
 * <p>
 * 提供 cron 定时调度、连接缓存（CachedPlcConnectionManager）、
 * tag 配置加载等公共能力；子类仅需实现：
 * <ul>
 *   <li>{@link #protocol()} — 协议标识</li>
 *   <li>{@link #buildConnectSpec} — 根据 connect 参数构建连接 URL 与 tag 映射</li>
 *   <li>可选重写 {@link #convertAddress} 做地址格式转换（如 Modbus 地址映射）</li>
 * </ul>
 */
@Slf4j
public abstract class AbstractPlc4xPollingProtocolManager implements ProtocolManager {

    private static final int DEFAULT_POOL_SIZE = 4;
    /** 写入后延迟读的等待时间（ms），给 PLC 扫描周期预留时间 */
    private static final long WRITE_READ_DELAY_MS = 500L;

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final Map<String, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();
    private final Map<String, ConnectSpec> specMap = new ConcurrentHashMap<>();
    /** 按 connectId 串行执行 doRead，避免同连接并发及保证任务按序完成 */
    private final Map<String, ExecutorService> readExecutorMap = new ConcurrentHashMap<>();

    private final PlcConnectionManager connectionManager;
    private final EquipFeign equipFeign;
    private final WorkshopFeign workshopFeign;
    private final PlcEquipDataParser equipDataParser;
    private final PlcWorkshopDataParser workshopDataParser;
    private final int readTimeoutMs;
    private final String threadPrefix;

    protected AbstractPlc4xPollingProtocolManager(EquipFeign equipFeign,
                                                  WorkshopFeign workshopFeign,
                                                  PlcEquipDataParser equipDataParser,
                                                  PlcWorkshopDataParser workshopDataParser,
                                                  int readTimeoutMs,
                                                  String threadPrefix) {
        this.equipFeign = equipFeign;
        this.workshopFeign = workshopFeign;
        this.equipDataParser = equipDataParser;
        this.workshopDataParser = workshopDataParser;
        this.readTimeoutMs = readTimeoutMs;
        this.threadPrefix = threadPrefix;
        this.connectionManager = CachedPlcConnectionManager.getBuilder()
                .withMaxLeaseTime(Duration.ofSeconds(30))
                .withMaxWaitTime(Duration.ofSeconds(20))
                .build();
    }

    /**
     * 根据 ProtocolConnect 和 tag 映射构建连接规格（URL + tags）。
     *
     * @return null 表示参数不合法，跳过启动
     */
    protected abstract ConnectSpec buildConnectSpec(ProtocolConnect connect, Map<String, String> tags);

    /**
     * 地址转换钩子，默认原样返回。子类可重写以进行协议特定的地址格式转换。
     */
    protected String convertAddress(String address) {
        return address;
    }

    @PostConstruct
    public void init() {
        scheduler.setPoolSize(DEFAULT_POOL_SIZE);
        scheduler.setThreadNamePrefix(threadPrefix);
        scheduler.setDaemon(true);
        scheduler.initialize();
    }

    @PreDestroy
    public void destroy() {
        stopAll();
        readExecutorMap.forEach((id, ex) -> {
            ex.shutdown();
            try {
                if (!ex.awaitTermination(3, TimeUnit.SECONDS)) ex.shutdownNow();
            } catch (InterruptedException ie) {
                ex.shutdownNow();
                Thread.currentThread().interrupt();
            }
        });
        readExecutorMap.clear();
        try {
            scheduler.shutdown();
        } catch (Exception ignored) {
        }
    }

    @Override
    public synchronized boolean start(ProtocolConnect connect) {
        if (connect == null || !StringUtils.hasText(connect.getId())) {
            return false;
        }
        String connectId = connect.getId();
        if (taskMap.containsKey(connectId)) {
            log.debug("{} polling task already exists, connectId={}", protocol(), connectId);
            return false;
        }
        if (!StringUtils.hasText(connect.getUri())) {
            log.warn("{} polling skipped: uri is empty, connectId={}", protocol(), connectId);
            return false;
        }
        if (!StringUtils.hasText(connect.getCollectCron())) {
            log.warn("{} polling skipped: collectCron is empty, connectId={}", protocol(), connectId);
            return false;
        }

        Map<String, String> tagsFromConfig = buildTagsFromGatewayConfig(connectId);
        if (tagsFromConfig == null || tagsFromConfig.isEmpty()) {
            return false;
        }
        ConnectSpec spec = buildConnectSpec(connect, tagsFromConfig);
        if (spec == null || spec.tagNames.isEmpty()) {
            log.warn("{} polling skipped: no tags, connectId={}", protocol(), connectId);
            return false;
        }

        ExecutorService readExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, threadPrefix + connectId);
            t.setDaemon(true);
            return t;
        });
        readExecutorMap.put(connectId, readExecutor);

        Runnable trigger = () -> {
            readExecutor.execute(() -> {
                UserContext.defaultTenant();
                try {
                    String payload = doRead(connectId, spec);
                    if (StringUtils.hasText(payload)) {
                        equipDataParser.parse(connectId, payload);
                        workshopDataParser.parse(connectId, payload);
                    }
                } catch (Exception e) {
                    log.error("{} polling error, connectId={}, uri={}", protocol(), connectId, spec.connectionUrl, e);
                }
            });
        };

        ScheduledFuture<?> future = scheduler.schedule(trigger, new CronTrigger(connect.getCollectCron()));
        taskMap.put(connectId, future);
        specMap.put(connectId, spec);
        log.info("{} polling started, connectId={}, cron={}, uri={}",
                protocol(), connectId, connect.getCollectCron(), connect.getUri());
        return true;
    }

    @Override
    public synchronized boolean stop(String connectId) {
        if (!StringUtils.hasText(connectId)) {
            return false;
        }
        ScheduledFuture<?> future = taskMap.remove(connectId);
        if (future != null) {
            future.cancel(false);
            ExecutorService ex = readExecutorMap.remove(connectId);
            if (ex != null) {
                ex.shutdown();
                try {
                    if (!ex.awaitTermination(3, TimeUnit.SECONDS)) {
                        ex.shutdownNow();
                    }
                } catch (InterruptedException ie) {
                    ex.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
            specMap.remove(connectId);
            log.info("{} polling stopped, connectId={}", protocol(), connectId);
            return true;
        }
        return false;
    }

    @Override
    public synchronized void stopAll() {
        List<String> keys = new ArrayList<>(taskMap.keySet());
        for (String key : keys) {
            stop(key);
        }
    }

    @Override
    public boolean write(String connectId, String address, Object value) {
        if (!StringUtils.hasText(connectId) || !StringUtils.hasText(address) || value == null) {
            return false;
        }
        ConnectSpec spec = specMap.get(connectId);
        if (spec == null) {
            log.warn("{} write failed: no active connection spec for connectId={}", protocol(), connectId);
            return false;
        }
        try (PlcConnection connection = connectionManager.getConnection(spec.connectionUrl)) {
            if (!connection.getMetadata().isWriteSupported()) {
                log.warn("{} connection does not support write: {}", protocol(), spec.connectionUrl);
                return false;
            }
            PlcWriteRequest.Builder builder = connection.writeRequestBuilder();
            builder.addTagAddress("ctrl", convertAddress(address), value);
            PlcWriteResponse response = builder.build().execute().get(readTimeoutMs, TimeUnit.MILLISECONDS);
            if (PlcResponseCode.OK.equals(response.getResponseCode("ctrl"))) {
                log.info("{} write success: connectId={}, address={}, value={}", protocol(), connectId, address, value);
                scheduleReadAfterWrite(connectId);
                return true;
            } else {
                log.warn("{} write response not OK: connectId={}, address={}, code={}",
                        protocol(), connectId, address, response.getResponseCode("ctrl"));
                return false;
            }
        } catch (Exception e) {
            log.error("{} write error: connectId={}, address={}, value={}", protocol(), connectId, address, value, e);
            return false;
        }
    }

    /** 写入后延迟触发一次读，刷新 PLC 端实际值（给扫描周期留出时间） */
    private void scheduleReadAfterWrite(String connectId) {
        ConnectSpec spec = specMap.get(connectId);
        ExecutorService executor = readExecutorMap.get(connectId);
        if (spec == null || executor == null) return;
        scheduler.schedule(() -> {
            if (!readExecutorMap.containsKey(connectId)) return;
            executor.execute(() -> {
                UserContext.defaultTenant();
                try {
                    String payload = doRead(connectId, spec);
                    if (StringUtils.hasText(payload)) {
                        equipDataParser.parse(connectId, payload);
                        workshopDataParser.parse(connectId, payload);
                    }
                } catch (Exception e) {
                    log.debug("{} read-after-write failed, connectId={}: {}", protocol(), connectId, e.getMessage());
                }
            });
        }, Instant.now().plusMillis(WRITE_READ_DELAY_MS));
    }

    // ---- private ----

    private Map<String, String> buildTagsFromGatewayConfig(String gwId) {
        Map<String, String> tagNames = new LinkedHashMap<>();
        try {
            EquipPageQuery query = new EquipPageQuery();
            query.setGwId(gwId);
            query.setQueryConfig(true);
            query.setRequirePage(false);
            List<EquipDto> list = RemoteHandleUtils.getDataFormResponse(equipFeign.selectByPage(query));
            if (list == null) {
                return tagNames;
            }
            for (EquipDto equip : list) {
                GwBindingDto binding = equip.getConfig();
                if (binding == null || binding.getConfig() == null) {
                    continue;
                }
                EquipConfigDetail config = binding.getConfig();
                if (StringUtils.hasText(config.getRunMap())) {
                    tagNames.put(config.getRunMap().trim(), config.getRunMap().trim());
                }
                if (config.getAttrs() != null) {
                    for (EquipAttr attr : config.getAttrs()) {
                        if (StringUtils.hasText(attr.getMap())) {
                            tagNames.put(attr.getMap().trim(), attr.getMap().trim());
                        }
                    }
                }
                if (config.getAlarms() != null) {
                    for (EquipAlarm alarm : config.getAlarms()) {
                        if (StringUtils.hasText(alarm.getMap())) {
                            tagNames.put(alarm.getMap().trim(), alarm.getMap().trim());
                        }
                    }
                }
                if (config.getControls() != null) {
                    for (EquipControl ctrl : config.getControls()) {
                        if (StringUtils.hasText(ctrl.getMap())) {
                            tagNames.put(ctrl.getMap().trim(), ctrl.getMap().trim());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Load {} tags from gateway config failed, gwId={}: {}", protocol(), gwId, e.getMessage());
        }
        try {
            List<WorkshopConfigCollectDto> allCollect = RemoteHandleUtils.getDataFormResponse(workshopFeign.queryConfigCollectByGwId(gwId));
            if (allCollect != null) {
                for (WorkshopConfigCollectDto dto : allCollect) {
                    if (dto == null || dto.getConfig() == null || dto.getConfig().getAttrs() == null) continue;
                    for (WorkshopConfigCollectAttr attr : dto.getConfig().getAttrs()) {
                        if (gwId.equals(attr.getGwId()) && StringUtils.hasText(attr.getMap())) {
                            String map = attr.getMap().trim();
                            tagNames.put(map, map);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Load {} tags from workshop config collect failed, gwId={}: {}", protocol(), gwId, e.getMessage());
        }
        return tagNames;
    }

    private static final int MAX_READ_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1_000L;

    private String doRead(String connectId, ConnectSpec spec) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_READ_RETRIES; attempt++) {
            try (PlcConnection connection = connectionManager.getConnection(spec.connectionUrl)) {
                if (!connection.isConnected()) {
                    throw new IllegalStateException("Cached connection is stale / not connected");
                }
                if (!connection.getMetadata().isReadSupported()) {
                    log.warn("{} connection does not support read: {}", protocol(), spec.connectionUrl);
                    return null;
                }

                PlcReadRequest.Builder builder = connection.readRequestBuilder();
                for (Map.Entry<String, String> e : spec.tagNames.entrySet()) {
                    builder.addTagAddress(e.getKey(), convertAddress(e.getValue()));
                }
                PlcReadResponse response = builder.build().execute().get(readTimeoutMs, TimeUnit.MILLISECONDS);

                Map<String, Object> result = new LinkedHashMap<>();
                for (String tagName : spec.tagNames.keySet()) {
                    if (PlcResponseCode.OK.equals(response.getResponseCode(tagName))) {
                        result.put(tagName, response.getObject(tagName));
                    }
                }
                return result.isEmpty() ? null : JSONObject.toJSONString(result);
            } catch (Exception e) {
                lastException = e;
                log.warn("{} read attempt {}/{} failed (connectId={}, uri={}): {}",
                        protocol(), attempt, MAX_READ_RETRIES, connectId, spec.connectionUrl, e.getMessage());
                if (attempt < MAX_READ_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw lastException;
    }

    // ---- inner class ----

    protected static class ConnectSpec {
        final String connectionUrl;
        final Map<String, String> tagNames;

        public ConnectSpec(String connectionUrl, Map<String, String> tagNames) {
            this.connectionUrl = connectionUrl;
            this.tagNames = tagNames;
        }
    }

    protected static int parsePort(String s, int defaultPort) {
        if (!StringUtils.hasText(s)) return defaultPort;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defaultPort;
        }
    }
}
