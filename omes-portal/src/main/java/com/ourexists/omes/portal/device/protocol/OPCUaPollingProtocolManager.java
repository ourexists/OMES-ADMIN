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
 * OPC UA 定时轮询协议管理器（基于 Apache PLC4X plc4j）：
 * - start：为指定 connect 建立 cron 定时任务，按 params 配置的 tag 地址读 OPC UA 节点并上报
 * - stop：移除并取消该 connect 的定时任务，关闭连接
 *
 * 连接采用长连接模式：启动时建立连接并常驻，轮询时复用；异常时自动重连；stop 时关闭。
 *
 * params JSON 示例：
 * {
 *   "port": 4840,
 *   "discovery": true,
 *   "username": "admin",
 *   "password": "password",
 *   "securityPolicy": "None"
 * }
 *
 * 设备 map 地址格式（PLC4X OPC UA）：
 * - 字符串标识：ns=2;s=HelloWorld/ScalarTypes/Boolean
 * - 数字标识：ns=1;i=1337
 * - 可选数据类型：ns=2;s=xxx;INT
 */
@Slf4j
@Component
public class OPCUaPollingProtocolManager implements ProtocolManager {

    private static final int DEFAULT_POOL_SIZE = 4;
    private static final int DEFAULT_PORT = 4840;
    private static final int READ_TIMEOUT_MS = 30_000;

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final Map<String, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();
    private final Map<String, OPCUaConnectSpec> connectSpecMap = new ConcurrentHashMap<>();
    private final Map<String, PlcConnection> connectionMap = new ConcurrentHashMap<>();
    private final Map<String, Object> connectLocks = new ConcurrentHashMap<>();

    private PlcDriverManager plcDriverManager;
    private EquipFeign equipFeign;
    private S7EquipDataParser equipDataParser;

    public OPCUaPollingProtocolManager(EquipFeign equipFeign,
                                      S7EquipDataParser equipDataParser) {
        this.plcDriverManager = PlcDriverManager.getDefault();
        this.equipFeign = equipFeign;
        this.equipDataParser = equipDataParser;
    }

    @PostConstruct
    public void init() {
        scheduler.setPoolSize(DEFAULT_POOL_SIZE);
        scheduler.setThreadNamePrefix("opcua-polling-");
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
        return "OPC UA";
    }

    @Override
    public synchronized boolean start(ProtocolConnect connect) {
        if (connect == null || !StringUtils.hasText(connect.getId())) {
            return false;
        }
        String connectId = connect.getId();
        if (taskMap.containsKey(connectId)) {
            log.debug("OPC UA polling task already exists, connectId={}", connectId);
            return false;
        }
        if (!StringUtils.hasText(connect.getUri())) {
            log.warn("OPC UA polling skipped: uri (server address) is empty, connectId={}", connectId);
            return false;
        }
        if (!StringUtils.hasText(connect.getCollectCron())) {
            log.warn("OPC UA polling skipped: collectCron is empty, connectId={}", connectId);
            return false;
        }

        Map<String, String> tagsFromConfig = buildTagsFromGatewayConfig(connectId);
        if (tagsFromConfig == null || tagsFromConfig.isEmpty()) {
            return false;
        }
        OPCUaConnectSpec spec = OPCUaConnectSpec.from(connect, tagsFromConfig);
        if (spec == null) {
            return false;
        }
        if (spec.tagNames.isEmpty()) {
            log.warn("OPC UA polling skipped: no tags from gateway device config or params, connectId={}", connectId);
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
                log.error("OPC UA polling error, connectId={}, uri={}", connectId, spec.connectionUrl, e);
            }
        };

        ScheduledFuture<?> future = scheduler.schedule(job, new CronTrigger(connect.getCollectCron()));
        taskMap.put(connectId, future);
        connectSpecMap.put(connectId, spec);
        log.info("OPC UA polling started, connectId={}, cron={}, uri={}", connectId, connect.getCollectCron(), connect.getUri());
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
            log.info("OPC UA polling stopped, connectId={}", connectId);
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
     * 通过网关 id 查询该网关下设备的采集配置，汇总需要采集的属性映射（tag 名 -> OPC UA 节点地址）。
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
            log.debug("Load OPC UA tags from gateway device config failed, gwId={}: {}", gwId, e.getMessage());
        }
        return tagNames;
    }

    private String doRead(String connectId, OPCUaConnectSpec spec) throws Exception {
        Object lock = connectLocks.computeIfAbsent(connectId, k -> new Object());
        synchronized (lock) {
            return doReadWithConnection(connectId, spec);
        }
    }

    private String doReadWithConnection(String connectId, OPCUaConnectSpec spec) throws Exception {
        PlcConnection connection = getOrCreateConnection(connectId, spec);
        if (connection == null) {
            return null;
        }
        try {
            if (!connection.getMetadata().isReadSupported()) {
                log.warn("OPC UA connection does not support read: {}", spec.connectionUrl);
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

    private PlcConnection getOrCreateConnection(String connectId, OPCUaConnectSpec spec) {
        PlcConnection conn = connectionMap.get(connectId);
        if (conn != null) {
            try {
                if (conn.isConnected()) {
                    return conn;
                }
            } catch (Exception e) {
                log.debug("OPC UA connection health check failed, connectId={}, will reconnect: {}", connectId, e.getMessage());
            }
            invalidateConnection(connectId);
        }
        try {
            conn = plcDriverManager.getConnectionManager().getConnection(spec.connectionUrl);
            connectionMap.put(connectId, conn);
            log.debug("OPC UA long connection established, connectId={}, uri={}", connectId, spec.connectionUrl);
            return conn;
        } catch (Exception e) {
            log.warn("OPC UA connection failed, connectId={}, uri={}: {}", connectId, spec.connectionUrl, e.getMessage());
            return null;
        }
    }

    private void invalidateConnection(String connectId) {
        PlcConnection conn = connectionMap.remove(connectId);
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                log.debug("OPC UA connection close error, connectId={}: {}", connectId, e.getMessage());
            }
            log.debug("OPC UA connection invalidated, connectId={}, will reconnect on next poll", connectId);
        }
    }

    private void closeConnection(String connectId) {
        PlcConnection conn = connectionMap.remove(connectId);
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                log.debug("OPC UA connection close error, connectId={}: {}", connectId, e.getMessage());
            }
        }
    }

    private static class OPCUaConnectSpec {
        final String connectionUrl;
        final Map<String, String> tagNames;

        OPCUaConnectSpec(String connectionUrl, Map<String, String> tagNames) {
            this.connectionUrl = connectionUrl;
            this.tagNames = tagNames;
        }

        static OPCUaConnectSpec from(ProtocolConnect connect, Map<String, String> tagsFromConfig) {
            String uri = connect.getUri().trim().replaceAll("/$", "");
            String hostPart = uri.replaceFirst("^[a-zA-Z0-9+.]+://", "");
            String[] hostPort = hostPart.split(":", 2);
            String host = hostPort[0];
            int port = hostPort.length > 1 ? parsePort(hostPort[1], DEFAULT_PORT) : DEFAULT_PORT;
            boolean discovery = true;
            String username = null;
            String password = null;
            String securityPolicy = "None";

            if (StringUtils.hasText(connect.getParams())) {
                try {
                    JSONObject jo = JSONObject.parseObject(connect.getParams());
                    if (jo != null) {
                        port = jo.getIntValue("port", port);
                        discovery = jo.getBooleanValue("discovery", true);
                        username = jo.getString("username");
                        password = jo.getString("password");
                        if (StringUtils.hasText(jo.getString("securityPolicy"))) {
                            securityPolicy = jo.getString("securityPolicy");
                        }
                    }
                } catch (Exception e) {
                    log.debug("Parse OPC UA params failed: {}", e.getMessage());
                }
            }

            StringBuilder url = new StringBuilder("opcua:tcp://").append(host).append(":").append(port);
            List<String> options = new ArrayList<>();
            options.add("discovery=" + discovery);
            options.add("security-policy=" + securityPolicy);
            if (StringUtils.hasText(username)) {
                options.add("username=" + username);
            }
            if (StringUtils.hasText(password)) {
                options.add("password=" + password);
            }
            if (!options.isEmpty()) {
                url.append("?").append(String.join("&", options));
            }

            Map<String, String> tags = new LinkedHashMap<>();
            if (tagsFromConfig != null && !tagsFromConfig.isEmpty()) {
                tags.putAll(tagsFromConfig);
            }
            return new OPCUaConnectSpec(url.toString(), tags);
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
