/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.device.protocol;

import com.alibaba.fastjson2.JSONObject;
import com.github.s7connector.api.DaveArea;
import com.github.s7connector.api.S7Connector;
import com.github.s7connector.api.factory.S7ConnectorFactory;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * S7 统一定时轮询协议管理器（基于 s7connector 库）。
 * <p>
 * 同时支持 S7-200 Smart、S7-300、S7-400、S7-1200、S7-1500 等西门子 PLC，
 * 使用 s7connector（nodave）协议栈，通过 rack/slot 区分不同控制器类型。
 * <p>
 * 常见 rack/slot 参考：
 * <ul>
 *   <li>S7-200 Smart: rack=0, slot=1</li>
 *   <li>S7-300: rack=0, slot=2</li>
 *   <li>S7-400: rack=0, slot=3</li>
 *   <li>S7-1200: rack=0, slot=1</li>
 *   <li>S7-1500: rack=0, slot=1</li>
 * </ul>
 * <p>
 * params JSON 示例：
 * <pre>
 * {
 *   "remoteRack": 0,
 *   "remoteSlot": 1,
 *   "timeout": 5000
 * }
 * </pre>
 *
 * 设备 map 地址格式：
 * <ul>
 *   <li>V 区（S7-200 Smart）：VB100, VW100, VD100, V100.0</li>
 *   <li>I 区：IB0, IW0, I0.0</li>
 *   <li>Q 区：QB0, QW0, Q0.0</li>
 *   <li>M 区：MB0, MW0, MD0, M0.0</li>
 *   <li>DB 区：DB1.DBB0, DB1.DBW0, DB1.DBD0</li>
 * </ul>
 */
@Slf4j
@Component
public class S7PollingProtocolManager implements ProtocolManager {

    private static final int DEFAULT_PORT = 102;
    private static final int DEFAULT_RACK = 0;
    private static final int DEFAULT_SLOT = 1;
    private static final int DEFAULT_TIMEOUT_MS = 5_000;
    private static final int DEFAULT_POOL_SIZE = 4;
    private static final int MAX_READ_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1_000L;

    private static final Pattern V_BWD = Pattern.compile("V([BWD])(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern V_BIT = Pattern.compile("V(\\d+)\\.(\\d)", Pattern.CASE_INSENSITIVE);
    private static final Pattern IQM_BWD = Pattern.compile("([IQM])([BWD])(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern IQM_BIT = Pattern.compile("([IQM])(\\d+)\\.(\\d)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DB_BWD = Pattern.compile("DB(\\d+)\\.DB([BWD])(\\d+)", Pattern.CASE_INSENSITIVE);

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final Map<String, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();
    private final Map<String, S7Connector> connectorMap = new ConcurrentHashMap<>();

    private final EquipFeign equipFeign;
    private final S7EquipDataParser equipDataParser;

    public S7PollingProtocolManager(EquipFeign equipFeign, S7EquipDataParser equipDataParser) {
        this.equipFeign = equipFeign;
        this.equipDataParser = equipDataParser;
    }

    @Override
    public String protocol() {
        return "S7";
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
        connectorMap.forEach((id, c) -> closeQuietly(c));
        connectorMap.clear();
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
            log.debug("S7 polling task already exists, connectId={}", connectId);
            return false;
        }
        if (!StringUtils.hasText(connect.getUri())) {
            log.warn("S7 polling skipped: uri is empty, connectId={}", connectId);
            return false;
        }
        if (!StringUtils.hasText(connect.getCollectCron())) {
            log.warn("S7 polling skipped: collectCron is empty, connectId={}", connectId);
            return false;
        }

        Map<String, String> tags = buildTagsFromGatewayConfig(connectId);
        if (tags == null || tags.isEmpty()) {
            return false;
        }

        ConnectConfig config = parseConnectConfig(connect);

        Runnable job = () -> {
            UserContext.defaultTenant();
            try {
                String payload = doRead(connectId, config, tags);
                if (StringUtils.hasText(payload)) {
                    equipDataParser.parse(connectId, payload);
                }
            } catch (Exception e) {
                log.error("S7 polling error, connectId={}, host={}", connectId, config.host, e);
            }
        };

        ScheduledFuture<?> future = scheduler.schedule(job, new CronTrigger(connect.getCollectCron()));
        taskMap.put(connectId, future);
        log.info("S7 polling started, connectId={}, cron={}, host={}:{}",
                connectId, connect.getCollectCron(), config.host, config.port);
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
            closeConnector(connectId);
            log.info("S7 polling stopped, connectId={}", connectId);
            return true;
        }
        return false;
    }

    @Override
    public synchronized void stopAll() {
        new ArrayList<>(taskMap.keySet()).forEach(this::stop);
    }

    // ---- connection management ----

    private S7Connector getOrCreateConnector(String connectId, ConnectConfig config) {
        S7Connector connector = connectorMap.get(connectId);
        if (connector != null) {
            return connector;
        }
        connector = S7ConnectorFactory
                .buildTCPConnector()
                .withHost(config.host)
                .withPort(config.port)
                .withRack(config.rack)
                .withSlot(config.slot)
                .withTimeout(config.timeout)
                .build();
        connectorMap.put(connectId, connector);
        log.info("S7 connection established, connectId={}, host={}:{}, rack={}, slot={}",
                connectId, config.host, config.port, config.rack, config.slot);
        return connector;
    }

    private void closeConnector(String connectId) {
        closeQuietly(connectorMap.remove(connectId));
    }

    private static void closeQuietly(S7Connector connector) {
        if (connector != null) {
            try {
                connector.close();
            } catch (Exception ignored) {
            }
        }
    }

    // ---- reading ----

    private String doRead(String connectId, ConnectConfig config, Map<String, String> tags) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_READ_RETRIES; attempt++) {
            try {
                S7Connector connector = getOrCreateConnector(connectId, config);
                Map<String, Object> result = new LinkedHashMap<>();
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    S7Address addr = parseAddress(entry.getValue());
                    byte[] data = connector.read(addr.area, addr.areaNumber, addr.byteCount, addr.offset);
                    if (data != null && data.length >= addr.byteCount) {
                        result.put(entry.getKey(), convertValue(data, addr));
                    }
                }
                return result.isEmpty() ? null : JSONObject.toJSONString(result);
            } catch (Exception e) {
                lastException = e;
                closeConnector(connectId);
                log.warn("S7 read attempt {}/{} failed (connectId={}, host={}): {}",
                        attempt, MAX_READ_RETRIES, connectId, config.host, e.getMessage());
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

    // ---- address parsing ----

    static S7Address parseAddress(String raw) {
        String addr = raw.trim().toUpperCase();
        Matcher m;

        m = V_BWD.matcher(addr);
        if (m.matches()) {
            char type = m.group(1).charAt(0);
            int offset = Integer.parseInt(m.group(2));
            return new S7Address(DaveArea.DB, 1, sizeOf(type), offset, type, -1);
        }

        m = V_BIT.matcher(addr);
        if (m.matches()) {
            int offset = Integer.parseInt(m.group(1));
            int bit = Integer.parseInt(m.group(2));
            return new S7Address(DaveArea.DB, 1, 1, offset, 'X', bit);
        }

        m = IQM_BWD.matcher(addr);
        if (m.matches()) {
            DaveArea area = areaOf(m.group(1).charAt(0));
            char type = m.group(2).charAt(0);
            int offset = Integer.parseInt(m.group(3));
            return new S7Address(area, 0, sizeOf(type), offset, type, -1);
        }

        m = IQM_BIT.matcher(addr);
        if (m.matches()) {
            DaveArea area = areaOf(m.group(1).charAt(0));
            int offset = Integer.parseInt(m.group(2));
            int bit = Integer.parseInt(m.group(3));
            return new S7Address(area, 0, 1, offset, 'X', bit);
        }

        m = DB_BWD.matcher(addr);
        if (m.matches()) {
            int dbNum = Integer.parseInt(m.group(1));
            char type = m.group(2).charAt(0);
            int offset = Integer.parseInt(m.group(3));
            return new S7Address(DaveArea.DB, dbNum, sizeOf(type), offset, type, -1);
        }

        throw new IllegalArgumentException("Unsupported S7 address: " + raw);
    }

    private static DaveArea areaOf(char c) {
        return switch (c) {
            case 'I' -> DaveArea.INPUTS;
            case 'Q' -> DaveArea.OUTPUTS;
            case 'M' -> DaveArea.FLAGS;
            default -> throw new IllegalArgumentException("Unknown area: " + c);
        };
    }

    private static int sizeOf(char type) {
        return switch (type) {
            case 'B' -> 1;
            case 'W' -> 2;
            case 'D' -> 4;
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }

    private static Object convertValue(byte[] data, S7Address addr) {
        if (addr.type == 'X') {
            return (data[0] >> addr.bitNumber) & 1;
        }
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        return switch (addr.type) {
            case 'B' -> buf.get() & 0xFF;
            case 'W' -> buf.getShort() & 0xFFFF;
            case 'D' -> buf.getInt() & 0xFFFFFFFFL;
            default -> buf.getInt();
        };
    }

    // ---- config parsing ----

    private ConnectConfig parseConnectConfig(ProtocolConnect connect) {
        String uri = connect.getUri().trim();
        if (uri.startsWith("s7://")) {
            uri = uri.substring("s7://".length());
        }
        String hostPart = uri.split("\\?")[0];
        String host;
        int port = DEFAULT_PORT;
        if (hostPart.contains(":")) {
            String[] parts = hostPart.split(":", 2);
            host = parts[0];
            try {
                port = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                port = DEFAULT_PORT;
            }
        } else {
            host = hostPart;
        }

        int rack = DEFAULT_RACK;
        int slot = DEFAULT_SLOT;
        int timeout = DEFAULT_TIMEOUT_MS;

        if (StringUtils.hasText(connect.getParams())) {
            try {
                JSONObject jo = JSONObject.parseObject(connect.getParams());
                if (jo != null) {
                    rack = jo.getIntValue("remoteRack", rack);
                    slot = jo.getIntValue("remoteSlot", slot);
                    timeout = jo.getIntValue("timeout", timeout);
                }
            } catch (Exception e) {
                log.debug("Parse S7 params failed: {}", e.getMessage());
            }
        }

        return new ConnectConfig(host, port, rack, slot, timeout);
    }

    // ---- tag loading ----

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
                            tagNames.put(attr.getMap(), attr.getMap().trim());
                        }
                    }
                }
                if (config.getAlarms() != null) {
                    for (EquipAlarm alarm : config.getAlarms()) {
                        if (StringUtils.hasText(alarm.getMap())) {
                            tagNames.put(alarm.getMap(), alarm.getMap().trim());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Load S7 tags failed, gwId={}: {}", gwId, e.getMessage());
        }
        return tagNames;
    }

    // ---- inner classes ----

    static class S7Address {
        final DaveArea area;
        final int areaNumber;
        final int byteCount;
        final int offset;
        final char type;
        final int bitNumber;

        S7Address(DaveArea area, int areaNumber, int byteCount, int offset, char type, int bitNumber) {
            this.area = area;
            this.areaNumber = areaNumber;
            this.byteCount = byteCount;
            this.offset = offset;
            this.type = type;
            this.bitNumber = bitNumber;
        }
    }

    static class ConnectConfig {
        final String host;
        final int port;
        final int rack;
        final int slot;
        final int timeout;

        ConnectConfig(String host, int port, int rack, int slot, int timeout) {
            this.host = host;
            this.port = port;
            this.rack = rack;
            this.slot = slot;
            this.timeout = timeout;
        }
    }
}
