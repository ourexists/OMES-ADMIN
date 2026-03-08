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
 * Modbus TCP 定时轮询协议管理器（基于 Apache PLC4X plc4j）：
 * - start：为指定 connect 建立 cron 定时任务，按 params 配置的 tag 地址读 PLC 并上报
 * - stop：移除并取消该 connect 的定时任务，关闭连接
 *
 * 连接采用长连接模式：启动时建立连接并常驻，轮询时复用；异常时自动重连；stop 时关闭。
 *
 * params JSON 示例：
 * {
 *   "port": 502,
 *   "unitId": 1
 * }
 *
 * 设备 map 地址格式（PLC4X Modbus）：
 * - holding-register:1 或 holding-register:1:INT
 * - input-register:1
 * - coil:1
 * - discrete-input:1
 */
@Slf4j
@Component
public class ModbusTcpPollingProtocolManager implements ProtocolManager {

    private static final int DEFAULT_POOL_SIZE = 4;
    private static final int DEFAULT_PORT = 502;
    private static final int READ_TIMEOUT_MS = 10_000;

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final Map<String, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();
    private final Map<String, ModbusConnectSpec> connectSpecMap = new ConcurrentHashMap<>();
    private final Map<String, PlcConnection> connectionMap = new ConcurrentHashMap<>();
    private final Map<String, Object> connectLocks = new ConcurrentHashMap<>();

    private PlcDriverManager plcDriverManager;
    private EquipFeign equipFeign;
    private S7EquipDataParser equipDataParser;

    public ModbusTcpPollingProtocolManager(EquipFeign equipFeign,
                                           S7EquipDataParser equipDataParser) {
        this.plcDriverManager = PlcDriverManager.getDefault();
        this.equipFeign = equipFeign;
        this.equipDataParser = equipDataParser;
    }

    @PostConstruct
    public void init() {
        scheduler.setPoolSize(DEFAULT_POOL_SIZE);
        scheduler.setThreadNamePrefix("modbus-polling-");
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
        return "Modbus TCP";
    }

    @Override
    public synchronized boolean start(ProtocolConnect connect) {
        if (connect == null || !StringUtils.hasText(connect.getId())) {
            return false;
        }
        String connectId = connect.getId();
        if (taskMap.containsKey(connectId)) {
            log.debug("Modbus TCP polling task already exists, connectId={}", connectId);
            return false;
        }
        if (!StringUtils.hasText(connect.getUri())) {
            log.warn("Modbus TCP polling skipped: uri (PLC IP) is empty, connectId={}", connectId);
            return false;
        }
        if (!StringUtils.hasText(connect.getCollectCron())) {
            log.warn("Modbus TCP polling skipped: collectCron is empty, connectId={}", connectId);
            return false;
        }

        Map<String, String> tagsFromConfig = buildTagsFromGatewayConfig(connectId);
        if (tagsFromConfig == null || tagsFromConfig.isEmpty()) {
            return false;
        }
        ModbusConnectSpec spec = ModbusConnectSpec.from(connect, tagsFromConfig);
        if (spec == null) {
            return false;
        }
        if (spec.tagNames.isEmpty()) {
            log.warn("Modbus TCP polling skipped: no tags from gateway device config or params, connectId={}", connectId);
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
                log.error("Modbus TCP polling error, connectId={}, uri={}", connectId, spec.connectionUrl, e);
            }
        };

        ScheduledFuture<?> future = scheduler.schedule(job, new CronTrigger(connect.getCollectCron()));
        taskMap.put(connectId, future);
        connectSpecMap.put(connectId, spec);
        log.info("Modbus TCP polling started, connectId={}, cron={}, uri={}", connectId, connect.getCollectCron(), connect.getUri());
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
            log.info("Modbus TCP polling stopped, connectId={}", connectId);
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
     * 通过网关 id 查询该网关下设备的采集配置，汇总需要采集的属性映射（tag 名 -> Modbus 地址）。
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
                if (StringUtils.hasText(config.getRunMap())) {
                    tagNames.put(selfCode + "_run", config.getRunMap().trim());
                }
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
            log.debug("Load Modbus tags from gateway device config failed, gwId={}: {}", gwId, e.getMessage());
        }
        return tagNames;
    }

    private String doRead(String connectId, ModbusConnectSpec spec) throws Exception {
        Object lock = connectLocks.computeIfAbsent(connectId, k -> new Object());
        synchronized (lock) {
            return doReadWithConnection(connectId, spec);
        }
    }

    private String doReadWithConnection(String connectId, ModbusConnectSpec spec) throws Exception {
        PlcConnection connection = getOrCreateConnection(connectId, spec);
        if (connection == null) {
            return null;
        }
        try {
            if (!connection.getMetadata().isReadSupported()) {
                log.warn("Modbus connection does not support read: {}", spec.connectionUrl);
                return null;
            }
            PlcReadRequest.Builder builder = connection.readRequestBuilder();
            for (Map.Entry<String, String> e : spec.tagNames.entrySet()) {
                String address = toPlc4xAddress(e.getValue());
                builder.addTagAddress(e.getKey(), address);
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

    /**
     * 将配置的 map 地址转换为 PLC4X Modbus 地址。
     * 支持：holding-register:1、input-register:1、coil:1、discrete-input:1
     * 以及 Modbus 惯例 40001、30001、10001、00001 的转换。
     */
    private String toPlc4xAddress(String map) {
        if (!StringUtils.hasText(map)) {
            return map;
        }
        String s = map.trim();
        if (s.startsWith("holding-register:") || s.startsWith("input-register:")
                || s.startsWith("coil:") || s.startsWith("discrete-input:")) {
            return s;
        }
        try {
            int num = Integer.parseInt(s);
            if (num >= 40001 && num <= 49999) {
                return "holding-register:" + (num - 40000);
            }
            if (num >= 30001 && num <= 39999) {
                return "input-register:" + (num - 30000);
            }
            if (num >= 10001 && num <= 19999) {
                return "coil:" + (num - 10000);
            }
            if (num >= 1 && num <= 9999) {
                return "discrete-input:" + num;
            }
        } catch (NumberFormatException ignored) {
        }
        return s;
    }

    private PlcConnection getOrCreateConnection(String connectId, ModbusConnectSpec spec) {
        PlcConnection conn = connectionMap.get(connectId);
        if (conn != null) {
            try {
                if (conn.isConnected()) {
                    return conn;
                }
            } catch (Exception e) {
                log.debug("Modbus connection health check failed, connectId={}, will reconnect: {}", connectId, e.getMessage());
            }
            invalidateConnection(connectId);
        }
        try {
            conn = plcDriverManager.getConnectionManager().getConnection(spec.connectionUrl);
            connectionMap.put(connectId, conn);
            log.debug("Modbus long connection established, connectId={}, uri={}", connectId, spec.connectionUrl);
            return conn;
        } catch (Exception e) {
            log.warn("Modbus connection failed, connectId={}, uri={}: {}", connectId, spec.connectionUrl, e.getMessage());
            return null;
        }
    }

    private void invalidateConnection(String connectId) {
        PlcConnection conn = connectionMap.remove(connectId);
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                log.debug("Modbus connection close error, connectId={}: {}", connectId, e.getMessage());
            }
            log.debug("Modbus connection invalidated, connectId={}, will reconnect on next poll", connectId);
        }
    }

    private void closeConnection(String connectId) {
        PlcConnection conn = connectionMap.remove(connectId);
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                log.debug("Modbus connection close error, connectId={}: {}", connectId, e.getMessage());
            }
        }
    }

    private static class ModbusConnectSpec {
        final String connectionUrl;
        final Map<String, String> tagNames;

        ModbusConnectSpec(String connectionUrl, Map<String, String> tagNames) {
            this.connectionUrl = connectionUrl;
            this.tagNames = tagNames;
        }

        static ModbusConnectSpec from(ProtocolConnect connect, Map<String, String> tagsFromConfig) {
            String uri = connect.getUri().trim().replaceAll("/$", "");
            String hostPart = uri.replaceFirst("^[a-zA-Z0-9+-]+://", "");
            String[] hostPort = hostPart.split(":", 2);
            String host = hostPort[0];
            int port = hostPort.length > 1 ? parsePort(hostPort[1], DEFAULT_PORT) : DEFAULT_PORT;
            int unitId = 1;
            if (StringUtils.hasText(connect.getParams())) {
                try {
                    JSONObject jo = JSONObject.parseObject(connect.getParams());
                    if (jo != null) {
                        port = jo.getIntValue("port", port);
                        unitId = jo.getIntValue("unitId", 1);
                    }
                } catch (Exception e) {
                    log.debug("Parse Modbus params failed: {}", e.getMessage());
                }
            }
            StringBuilder url = new StringBuilder("modbus-tcp://").append(host).append(":").append(port);
            if (unitId != 1) {
                url.append("?unit-identifier=").append(unitId);
            }

            Map<String, String> tags = new LinkedHashMap<>();
            if (tagsFromConfig != null && !tagsFromConfig.isEmpty()) {
                tags.putAll(tagsFromConfig);
            }
            return new ModbusConnectSpec(url.toString(), tags);
        }

        private static int parsePort(String s, int defaultPort) {
            if (!StringUtils.hasText(s)) return defaultPort;
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException e) {
                return defaultPort;
            }
        }
    }
}
