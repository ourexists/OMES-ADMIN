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
import com.ourexists.omes.device.model.*;
import com.ourexists.omes.portal.device.collect.S7EquipDataParser;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.PlcDriverManager;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * S7 定时轮询协议管理器（基于 Apache PLC4X plc4j）：
 * - start：为指定 connect 建立 cron 定时任务，按 params 配置的 tag 地址读 PLC 并上报
 * - stop：移除并取消该 connect 的定时任务，关闭连接
 *
 * 注意：该类内部单独维护 scheduler/taskMap，不依赖全局任务管理器。
 * 连接采用长连接模式：启动时建立连接并常驻，轮询时复用；异常时自动重连；stop 时关闭。
 *
 * params JSON 示例：
 * {
 *   "remoteRack": 0,
 *   "remoteSlot": 1,
 *   "controllerType": "S7_1200",
 *   "tags": { "tag1": "%DB1:0:INT", "tag2": "%M0:BYTE" }
 * }
 * 或 "tags" 为数组: [ {"name":"tag1","address":"%DB1:0:INT"} ]
 */
@Slf4j
@Component
public class S7PollingProtocolManager implements ProtocolManager {

    private static final int DEFAULT_POOL_SIZE = 4;
    private static final int READ_TIMEOUT_MS = 10_000;

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final Map<String, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();
    private final Map<String, S7ConnectSpec> connectSpecMap = new ConcurrentHashMap<>();
    private final Map<String, PlcConnection> connectionMap = new ConcurrentHashMap<>();
    private final Map<String, Object> connectLocks = new ConcurrentHashMap<>();

    private PlcDriverManager plcDriverManager;

    private EquipFeign equipFeign;

    private S7EquipDataParser equipDataParser;

    public S7PollingProtocolManager(EquipFeign equipFeign,
                                    S7EquipDataParser equipDataParser) {
        this.plcDriverManager = PlcDriverManager.getDefault();
        this.equipFeign = equipFeign;
        this.equipDataParser = equipDataParser;
    }

    @PostConstruct
    public void init() {
        scheduler.setPoolSize(DEFAULT_POOL_SIZE);
        scheduler.setThreadNamePrefix("s7-polling-");
        scheduler.setDaemon(true);
        scheduler.initialize();
    }

    @PreDestroy
    public void destroy() {
        stopAll();
        try {
            scheduler.shutdown();
        } catch (Exception ignored) {
        }
    }

    @Override
    public String protocol() {
        return "S7";
    }

    @Override
    public synchronized boolean start(ProtocolConnect connect) {
        if (connect == null || !StringUtils.hasText(connect.getId())) {
            return false;
        }
        String connectId = connect.getId();
        if (taskMap.containsKey(connectId)) {
            log.debug("S7 polling task already exists, connectId={}", connectId);
            return false;
        }
        if (!StringUtils.hasText(connect.getUri())) {
            log.warn("S7 polling skipped: uri (PLC IP) is empty, connectId={}", connectId);
            return false;
        }
        if (!StringUtils.hasText(connect.getCollectCron())) {
            log.warn("S7 polling skipped: collectCron is empty, connectId={}", connectId);
            return false;
        }

        // tags 优先从该网关下设备的采集配置查询，若无则从 params 解析
        Map<String, String> tagsFromConfig = buildTagsFromGatewayConfig(connectId);
        if (tagsFromConfig == null || tagsFromConfig.isEmpty()) {
            return false;
        }
        S7ConnectSpec spec = S7ConnectSpec.from(connect, tagsFromConfig);
        if (spec == null) {
            return false;
        }
        if (spec.tagNames.isEmpty()) {
            log.warn("S7 polling skipped: no tags from gateway device config or params, connectId={}", connectId);
            return false;
        }

        Runnable job = () -> {
            UserContext.defaultTenant();
            try {
                String payload = doRead(connectId, spec);
                if (StringUtils.hasText(payload)) {
                    equipDataParser.parse(connectId, payload);
                }
            } catch (Exception e) {
                log.error("S7 polling error, connectId={}, uri={}", connectId, spec.connectionUrl, e);
            }
        };

        ScheduledFuture<?> future = scheduler.schedule(job, new CronTrigger(connect.getCollectCron()));
        taskMap.put(connectId, future);
        connectSpecMap.put(connectId, spec);
        log.info("S7 polling started, connectId={}, cron={}, uri={}", connectId, connect.getCollectCron(), connect.getUri());
        return true;
    }

    @Override
    public synchronized boolean stop(String connectId) {
        if (!StringUtils.hasText(connectId)) {
            return false;
        }
        ScheduledFuture<?> future = taskMap.remove(connectId);
        connectSpecMap.remove(connectId);
        Object lock = connectLocks.computeIfAbsent(connectId, k -> new Object());
        synchronized (lock) {
            closeConnection(connectId);
        }
        connectLocks.remove(connectId);
        if (future != null) {
            future.cancel(true);
            log.info("S7 polling stopped, connectId={}", connectId);
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

    /**
     * 通过网关 id 查询该网关下设备的采集配置，汇总需要采集的属性映射（tag 名 -> S7 地址）。
     * tag 名使用 设备编号_属性名，与解析端一致。
     */
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
                String selfCode = equip.getSelfCode() != null ? equip.getSelfCode() : equip.getId();
                tagNames.put(selfCode + "_run", config.getRunMap());
                if (config.getAttrs() != null) {
                    for (EquipAttr attr : config.getAttrs()) {
                        if (StringUtils.hasText(attr.getMap())) {
                            String tagName = selfCode + "_" + attr.getMap();
                            tagNames.put(tagName, attr.getMap().trim());
                        }
                    }
                }
                if (config.getAlarms() != null) {
                    for (EquipAlarm alarm : config.getAlarms()) {
                        if (StringUtils.hasText(alarm.getMap())) {
                            String tagName = selfCode + "_" + alarm.getMap();
                            tagNames.put(tagName, alarm.getMap().trim());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Load S7 tags from gateway device config failed, gwId={}: {}", gwId, e.getMessage());
        }
        return tagNames;
    }

    private String doRead(String connectId, S7ConnectSpec spec) throws Exception {
        Object lock = connectLocks.computeIfAbsent(connectId, k -> new Object());
        synchronized (lock) {
            return doReadWithConnection(connectId, spec);
        }
    }

    private String doReadWithConnection(String connectId, S7ConnectSpec spec) throws Exception {
        PlcConnection connection = getOrCreateConnection(connectId, spec);
        if (connection == null) {
            return null;
        }
        try {
            if (!connection.getMetadata().isReadSupported()) {
                log.warn("S7 connection does not support read: {}", spec.connectionUrl);
                return null;
            }
            PlcReadRequest.Builder builder = connection.readRequestBuilder();
            for (Map.Entry<String, String> e : spec.tagNames.entrySet()) {
                builder.addTagAddress(e.getKey(), e.getValue());
            }
            PlcReadRequest readRequest = builder.build();
            PlcReadResponse response = readRequest.execute().get(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            Map<String, Object> result = new LinkedHashMap<>();
            for (String tagName : spec.tagNames.keySet()) {
                if (PlcResponseCode.OK.equals(response.getResponseCode(tagName))) {
                    Object value = response.getObject(tagName);
                    result.put(tagName, value);
                }
            }
            return result.isEmpty() ? null : JSONObject.toJSONString(result);
        } catch (Exception e) {
            invalidateConnection(connectId);
            throw e;
        }
    }

    private PlcConnection getOrCreateConnection(String connectId, S7ConnectSpec spec) {
        PlcConnection conn = connectionMap.get(connectId);
        // 连接有效性预检：若已断开则失效并重建，实现健康复用
        if (conn != null) {
            try {
                if (conn.isConnected()) {
                    return conn;
                }
            } catch (Exception e) {
                log.debug("S7 connection health check failed, connectId={}, will reconnect: {}", connectId, e.getMessage());
            }
            invalidateConnection(connectId);
        }
        try {
            conn = plcDriverManager.getConnectionManager().getConnection(spec.connectionUrl);
            connectionMap.put(connectId, conn);
            log.debug("S7 long connection established, connectId={}, uri={}", connectId, spec.connectionUrl);
            return conn;
        } catch (Exception e) {
            log.warn("S7 connection failed, connectId={}, uri={}: {}", connectId, spec.connectionUrl, e.getMessage());
            return null;
        }
    }

    private void invalidateConnection(String connectId) {
        PlcConnection conn = connectionMap.remove(connectId);
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                log.debug("S7 connection close error, connectId={}: {}", connectId, e.getMessage());
            }
            log.debug("S7 connection invalidated, connectId={}, will reconnect on next poll", connectId);
        }
    }

    private void closeConnection(String connectId) {
        PlcConnection conn = connectionMap.remove(connectId);
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                log.debug("S7 connection close error, connectId={}: {}", connectId, e.getMessage());
            }
        }
    }

    private static class S7ConnectSpec {
        final String connectionUrl;
        final Map<String, String> tagNames;

        S7ConnectSpec(String connectionUrl, Map<String, String> tagNames) {
            this.connectionUrl = connectionUrl;
            this.tagNames = tagNames;
        }

        static S7ConnectSpec from(ProtocolConnect connect, Map<String, String> tagsFromConfig) {
            String host = connect.getUri().trim();
            if (!host.contains("://")) {
                host = "s7://" + host;
            }
            StringBuilder url = new StringBuilder(host);
            Map<String, String> tags = new LinkedHashMap<>();

            // 若已从网关设备配置加载到 tags，直接使用
            if (tagsFromConfig != null && !tagsFromConfig.isEmpty()) {
                tags.putAll(tagsFromConfig);
            }

            if (StringUtils.hasText(connect.getParams())) {
                try {
                    JSONObject jo = JSONObject.parseObject(connect.getParams());
                    if (jo != null) {
                        int remoteRack = jo.getIntValue("remoteRack", 0);
                        int remoteSlot = jo.getIntValue("remoteSlot", 1);
                        String controllerType = jo.getString("controllerType");
                        url.append(url.toString().contains("?") ? "&" : "?");
                        url.append("remote-rack=").append(remoteRack)
                                .append("&remote-slot=").append(remoteSlot);
                        if (StringUtils.hasText(controllerType)) {
                            url.append("&controller-type=").append(controllerType.trim());
                        }
                        // 仅当未从设备配置加载到 tags 时，才从 params 解析 tags
                        if (tags.isEmpty()) {
                            return null;
                        }
                    }
                } catch (Exception e) {
                    log.debug("Parse S7 params failed: {}", e.getMessage());
                }
            }
            return new S7ConnectSpec(url.toString(), tags);
        }
    }
}
