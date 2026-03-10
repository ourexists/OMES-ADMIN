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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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
    /** 单次读取最大字节数，需小于 PLC 的 PDU 数据负载（S7-200 Smart ≈ 222） */
    private static final int MAX_READ_BYTES = 200;

    private static final Pattern V_BWD = Pattern.compile("V([BWD])(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern V_BIT = Pattern.compile("V(\\d+)\\.(\\d)", Pattern.CASE_INSENSITIVE);
    private static final Pattern IQM_BWD = Pattern.compile("([IQM])([BWD])(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern IQM_BIT = Pattern.compile("([IQM])(\\d+)\\.(\\d)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DB_BWD = Pattern.compile("DB(\\d+)\\.DB([BWD])(\\d+)", Pattern.CASE_INSENSITIVE);

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final Map<String, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>();
    private final Map<String, S7Connector> connectorMap = new ConcurrentHashMap<>();
    /** 按 connectId 串行执行 doRead，避免同连接并发及保证任务按序完成 */
    private final Map<String, ExecutorService> readExecutorMap = new ConcurrentHashMap<>();
    /** 用于写入后延迟读，需存储 config/tags */
    private final Map<String, ConnectConfig> configStore = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> tagsStore = new ConcurrentHashMap<>();
    /** 写入后延迟读的等待时间（ms），给 PLC 扫描周期预留时间 */
    private static final long WRITE_READ_DELAY_MS = 500L;

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
        configStore.put(connectId, config);
        tagsStore.put(connectId, tags);

        ExecutorService readExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "s7-read-" + connectId);
            t.setDaemon(true);
            return t;
        });
        readExecutorMap.put(connectId, readExecutor);

        Runnable trigger = () -> {
            readExecutor.execute(() -> {
                UserContext.defaultTenant();
                try {
                    String payload = doRead(connectId, config, tags);
                    if (StringUtils.hasText(payload)) {
                        equipDataParser.parse(connectId, payload);
                    }
                } catch (Exception e) {
                    log.error("S7 polling error, connectId={}, host={}", connectId, config.host, e);
                }
            });
        };

        ScheduledFuture<?> future = scheduler.schedule(trigger, new CronTrigger(connect.getCollectCron()));
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
            configStore.remove(connectId);
            tagsStore.remove(connectId);
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

    @Override
    public boolean write(String connectId, String address, Object value) {
        if (!StringUtils.hasText(connectId) || !StringUtils.hasText(address) || value == null) {
            return false;
        }
        ScheduledFuture<?> task = taskMap.get(connectId);
        if (task == null) {
            log.warn("S7 write failed: no active polling task for connectId={}", connectId);
            return false;
        }
        S7Connector connector = connectorMap.get(connectId);
        if (connector == null) {
            log.warn("S7 write failed: no active connection for connectId={}", connectId);
            return false;
        }
        try {
            S7Address addr = parseAddress(address);
            byte[] data = buildWriteBytes(addr, value);
            connector.write(addr.area, addr.areaNumber, addr.offset, data);
            log.info("S7 write success: connectId={}, address={}, value={}", connectId, address, value);
            scheduleReadAfterWrite(connectId);
            return true;
        } catch (Exception e) {
            log.error("S7 write error: connectId={}, address={}, value={}", connectId, address, value, e);
            return false;
        }
    }

    /** 写入后延迟触发一次读，刷新 PLC 端实际值（给扫描周期留出时间） */
    private void scheduleReadAfterWrite(String connectId) {
        ConnectConfig config = configStore.get(connectId);
        Map<String, String> tags = tagsStore.get(connectId);
        ExecutorService executor = readExecutorMap.get(connectId);
        if (config == null || tags == null || executor == null) return;
        scheduler.schedule(() -> {
            if (!readExecutorMap.containsKey(connectId)) return;
            executor.execute(() -> {
                UserContext.defaultTenant();
                try {
                    String payload = doRead(connectId, config, tags);
                    if (StringUtils.hasText(payload)) {
                        equipDataParser.parse(connectId, payload);
                    }
                } catch (Exception e) {
                    log.debug("S7 read-after-write failed, connectId={}: {}", connectId, e.getMessage());
                }
            });
        }, Instant.now().plusMillis(WRITE_READ_DELAY_MS));
    }

    private static byte[] buildWriteBytes(S7Address addr, Object value) {
        if (addr.type == 'X') {
            int bitVal = toBoolInt(value);
            return new byte[]{(byte) (bitVal & 1)};
        }
        long num = toLong(value);
        ByteBuffer buf = ByteBuffer.allocate(addr.byteCount).order(ByteOrder.BIG_ENDIAN);
        switch (addr.type) {
            case 'B' -> buf.put((byte) (num & 0xFF));
            case 'W' -> buf.putShort((short) (num & 0xFFFF));
            case 'D' -> buf.putInt((int) num);
            default -> buf.putInt((int) num);
        }
        return buf.array();
    }

    private static int toBoolInt(Object value) {
        if (value instanceof Boolean b) return b ? 1 : 0;
        if (value instanceof Number n) return n.intValue() != 0 ? 1 : 0;
        String s = value.toString().trim().toLowerCase();
        return "true".equals(s) || "1".equals(s) || "on".equals(s) ? 1 : 0;
    }

    private static long toLong(Object value) {
        if (value instanceof Number n) return n.longValue();
        return Long.parseLong(value.toString().trim());
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

                Map<String, S7Address> parsedTags = new LinkedHashMap<>();
                for (Map.Entry<String, String> entry : tags.entrySet()) {
                    parsedTags.put(entry.getKey(), parseAddress(entry.getValue()));
                }

                Map<String, List<Map.Entry<String, S7Address>>> groups = new LinkedHashMap<>();
                for (Map.Entry<String, S7Address> entry : parsedTags.entrySet()) {
                    S7Address addr = entry.getValue();
                    String groupKey = addr.area.name() + "_" + addr.areaNumber;
                    groups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(entry);
                }

                Map<String, Object> result = new LinkedHashMap<>();
                for (List<Map.Entry<String, S7Address>> group : groups.values()) {
                    group.sort(Comparator.comparingInt(e -> e.getValue().offset));
                    for (List<Map.Entry<String, S7Address>> chunk : splitChunks(group)) {
                        S7Address first = chunk.get(0).getValue();
                        int minOffset = first.offset;
                        int maxEnd = 0;
                        for (Map.Entry<String, S7Address> e : chunk) {
                            maxEnd = Math.max(maxEnd, e.getValue().offset + e.getValue().byteCount);
                        }
                        int totalBytes = maxEnd - minOffset;
                        byte[] block = connector.read(first.area, first.areaNumber, totalBytes, minOffset);
                        if (block == null || block.length < totalBytes) {
                            continue;
                        }
                        for (Map.Entry<String, S7Address> e : chunk) {
                            S7Address a = e.getValue();
                            byte[] slice = new byte[a.byteCount];
                            System.arraycopy(block, a.offset - minOffset, slice, 0, a.byteCount);
                            result.put(e.getKey(), convertValue(slice, a));
                        }
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

    /**
     * 将已按 offset 排序的 tag 列表拆分为若干块，每块字节跨度不超过 MAX_READ_BYTES。
     */
    private static <T extends Map.Entry<String, S7Address>> List<List<T>> splitChunks(List<T> sorted) {
        List<List<T>> chunks = new ArrayList<>();
        List<T> cur = new ArrayList<>();
        int chunkStart = 0, chunkEnd = 0;
        for (T entry : sorted) {
            S7Address a = entry.getValue();
            if (cur.isEmpty()) {
                cur.add(entry);
                chunkStart = a.offset;
                chunkEnd = a.offset + a.byteCount;
            } else {
                int newEnd = Math.max(chunkEnd, a.offset + a.byteCount);
                if (newEnd - chunkStart <= MAX_READ_BYTES) {
                    cur.add(entry);
                    chunkEnd = newEnd;
                } else {
                    chunks.add(cur);
                    cur = new ArrayList<>();
                    cur.add(entry);
                    chunkStart = a.offset;
                    chunkEnd = a.offset + a.byteCount;
                }
            }
        }
        if (!cur.isEmpty()) {
            chunks.add(cur);
        }
        return chunks;
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
                if (config.getControls() != null) {
                    for (EquipControl ctrl : config.getControls()) {
                        if (StringUtils.hasText(ctrl.getMap())) {
                            tagNames.put(ctrl.getMap().trim(), ctrl.getMap().trim());
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
