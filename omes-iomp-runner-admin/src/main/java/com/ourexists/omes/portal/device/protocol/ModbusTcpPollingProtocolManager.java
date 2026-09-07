/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.device.protocol;

import com.alibaba.fastjson2.JSONObject;
import com.ourexists.omes.device.core.equip.protocol.ProtocolConnect;
import com.ourexists.omes.device.feign.EquipFeign;
import com.ourexists.omes.device.feign.WorkshopFeign;
import com.ourexists.omes.portal.device.collect.PlcEquipDataParser;
import com.ourexists.omes.portal.device.collect.PlcWorkshopDataParser;
import com.ourexists.omes.portal.mq.EquipRealtimeStreamOutbound;
import lombok.extern.slf4j.Slf4j;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Modbus TCP 定时轮询协议管理器（基于 Apache PLC4X plc4j）。
 *
 * params JSON 示例：
 * {
 *   "port": 502,
 *   "unitId": 1,
 *   "timeout": 10000
 * }
 * timeout: 连接/请求超时(ms)，默认 10000，用于缓解 ConnectTimeoutException
 *
 * 设备 map 地址格式（PLC4X Modbus）：
 * - holding-register:1 或 holding-register:1:INT
 * - input-register:1
 * - coil:1
 * - discrete-input:1
 */
@Slf4j
@Component
public class ModbusTcpPollingProtocolManager extends AbstractPlc4xPollingProtocolManager {

    private static final int DEFAULT_PORT = 502;
    private static final int READ_TIMEOUT_MS = 10_000;
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 10_000;
    private static final int MAX_REGISTER_SPAN = 125;
    private static final int MAX_BIT_SPAN = 2000;
    private static final int FALLBACK_BATCH_SIZE = 100;
    private static final Pattern SIMPLE_ADDR = Pattern.compile(
            "^(holding-register|input-register|coil|discrete-input):(\\d+)$",
            Pattern.CASE_INSENSITIVE
    );

    public ModbusTcpPollingProtocolManager(EquipFeign equipFeign,
                                           WorkshopFeign workshopFeign,
                                           PlcEquipDataParser equipDataParser,
                                           PlcWorkshopDataParser workshopDataParser,
                                           EquipRealtimeStreamOutbound equipRealtimeStreamOutbound) {
        super(equipFeign, workshopFeign, equipDataParser, workshopDataParser, equipRealtimeStreamOutbound, READ_TIMEOUT_MS, "modbus-polling-");
    }

    @Override
    public String protocol() {
        return "Modbus TCP";
    }

    @Override
    protected ConnectSpec buildConnectSpec(ProtocolConnect connect, Map<String, String> tags) {
        String uri = connect.getUri().trim().replaceAll("/$", "");
        String hostPart = uri.replaceFirst("^[a-zA-Z0-9+-]+://", "");
        String[] hostPort = hostPart.split(":", 2);
        String host = hostPort[0];
        int port = hostPort.length > 1 ? parsePort(hostPort[1], DEFAULT_PORT) : DEFAULT_PORT;
        int unitId = 1;

        int timeout = DEFAULT_CONNECT_TIMEOUT_MS;
        if (StringUtils.hasText(connect.getParams())) {
            try {
                JSONObject jo = JSONObject.parseObject(connect.getParams());
                if (jo != null) {
                    port = jo.getIntValue("port", port);
                    unitId = jo.getIntValue("unitId", 1);
                    timeout = jo.getIntValue("timeout", timeout);
                }
            } catch (Exception e) {
                log.debug("Parse Modbus params failed: {}", e.getMessage());
            }
        }

        StringBuilder url = new StringBuilder("modbus-tcp://").append(host).append(":").append(port);
        List<String> params = new ArrayList<>();
        if (unitId != 1) {
            params.add("unit-identifier=" + unitId);
        }
        params.add("request-timeout=" + timeout);
        if (!params.isEmpty()) {
            url.append("?").append(String.join("&", params));
        }

        return new ConnectSpec(url.toString(), new LinkedHashMap<>(tags));
    }

    /**
     * 将配置的 map 地址转换为 PLC4X Modbus 地址。
     * 支持 PLC4X 原生格式（holding-register:1）以及 Modbus 惯例数字（40001）的自动转换。
     */
    @Override
    protected String convertAddress(String address) {
        if (!StringUtils.hasText(address)) {
            return address;
        }
        String s = address.trim();
        if (s.startsWith("holding-register:") || s.startsWith("input-register:")
                || s.startsWith("coil:") || s.startsWith("discrete-input:")) {
            return s;
        }
        try {
            int num = Integer.parseInt(s);
            if (num >= 40001 && num <= 49999) return "holding-register:" + (num - 40000);
            if (num >= 30001 && num <= 39999) return "input-register:" + (num - 30000);
            if (num >= 10001 && num <= 19999) return "coil:" + (num - 10000);
            if (num >= 1 && num <= 9999) return "discrete-input:" + num;
        } catch (NumberFormatException ignored) {
        }
        return s;
    }

    /**
     * Modbus 读取按区域与跨度分块，避免单请求数据量过大触发 PDU/设备限制。
     */
    @Override
    protected String readOnce(PlcConnection connection, ConnectSpec spec) throws Exception {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, List<SimpleTag>> grouped = new LinkedHashMap<>();
        List<FallbackTag> fallback = new ArrayList<>();

        for (Map.Entry<String, String> e : spec.tagNames.entrySet()) {
            String converted = convertAddress(e.getValue());
            SimpleTag parsed = parseSimpleTag(e.getKey(), converted);
            if (parsed != null) {
                grouped.computeIfAbsent(parsed.area, k -> new ArrayList<>()).add(parsed);
            } else {
                fallback.add(new FallbackTag(e.getKey(), converted));
            }
        }

        int chunkNo = 0;
        for (Map.Entry<String, List<SimpleTag>> group : grouped.entrySet()) {
            String area = group.getKey();
            List<SimpleTag> tags = group.getValue();
            tags.sort(Comparator.comparingInt(t -> t.address));
            int maxSpan = maxSpanForArea(area);

            int i = 0;
            while (i < tags.size()) {
                int start = tags.get(i).address;
                int end = start;
                List<SimpleTag> chunkTags = new ArrayList<>();
                chunkTags.add(tags.get(i));
                i++;
                while (i < tags.size()) {
                    SimpleTag next = tags.get(i);
                    int newEnd = Math.max(end, next.address);
                    if (newEnd - start + 1 <= maxSpan) {
                        chunkTags.add(next);
                        end = newEnd;
                        i++;
                    } else {
                        break;
                    }
                }

                chunkNo++;
                readChunk(connection, area, start, end, chunkTags, chunkNo, result);
            }
        }

        for (int i = 0; i < fallback.size(); i += FALLBACK_BATCH_SIZE) {
            int end = Math.min(i + FALLBACK_BATCH_SIZE, fallback.size());
            readFallbackBatch(connection, fallback.subList(i, end), result);
        }

        return result.isEmpty() ? null : JSONObject.toJSONString(result);
    }

    private void readChunk(PlcConnection connection,
                           String area,
                           int start,
                           int end,
                           List<SimpleTag> chunkTags,
                           int chunkNo,
                           Map<String, Object> result) throws Exception {
        int quantity = end - start + 1;
        String alias = "__chunk_" + chunkNo;
        String query = area + ":" + start + "[" + quantity + "]";
        PlcReadRequest.Builder builder = connection.readRequestBuilder();
        builder.addTagAddress(alias, query);
        PlcReadResponse response = builder.build().execute().get(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (!PlcResponseCode.OK.equals(response.getResponseCode(alias))) {
            return;
        }
        Object block = response.getObject(alias);
        for (SimpleTag tag : chunkTags) {
            int index = tag.address - start;
            Object value = getElement(block, index);
            if (value != null) {
                result.put(tag.tagName, value);
            }
        }
    }

    private void readFallbackBatch(PlcConnection connection, List<FallbackTag> batch, Map<String, Object> result) throws Exception {
        PlcReadRequest.Builder builder = connection.readRequestBuilder();
        for (FallbackTag tag : batch) {
            builder.addTagAddress(tag.tagName, tag.address);
        }
        PlcReadResponse response = builder.build().execute().get(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        for (FallbackTag tag : batch) {
            if (PlcResponseCode.OK.equals(response.getResponseCode(tag.tagName))) {
                result.put(tag.tagName, response.getObject(tag.tagName));
            }
        }
    }

    private static SimpleTag parseSimpleTag(String tagName, String convertedAddress) {
        if (!StringUtils.hasText(convertedAddress)) {
            return null;
        }
        Matcher m = SIMPLE_ADDR.matcher(convertedAddress.trim());
        if (!m.matches()) {
            return null;
        }
        String area = m.group(1).toLowerCase();
        int address = Integer.parseInt(m.group(2));
        return new SimpleTag(tagName, area, address);
    }

    private static int maxSpanForArea(String area) {
        if ("coil".equals(area) || "discrete-input".equals(area)) {
            return MAX_BIT_SPAN;
        }
        return MAX_REGISTER_SPAN;
    }

    private static Object getElement(Object source, int index) {
        if (source == null || index < 0) {
            return null;
        }
        if (source instanceof List<?> list) {
            return index < list.size() ? list.get(index) : null;
        }
        Class<?> cls = source.getClass();
        if (cls.isArray()) {
            return index < Array.getLength(source) ? Array.get(source, index) : null;
        }
        return index == 0 ? source : null;
    }

    private record SimpleTag(String tagName, String area, int address) {
    }

    private record FallbackTag(String tagName, String address) {
    }

    /**
     * 线圈（coil）写入时 PLC4X 要求 Boolean，将 0/1 等转为 Boolean。
     */
    @Override
    public boolean write(String connectId, String address, Object value) {
        if (!StringUtils.hasText(connectId) || !StringUtils.hasText(address) || value == null) {
            return false;
        }
        String convertedAddr = convertAddress(address);
        Object writeValue = value;
        if (convertedAddr != null && convertedAddr.startsWith("coil:")) {
            if (value instanceof Boolean b) {
                writeValue = b;
            } else if (value instanceof Number n) {
                writeValue = n.intValue() != 0;
            } else {
                String str = value.toString().trim().toLowerCase();
                writeValue = "true".equals(str) || "1".equals(str) || "on".equals(str);
            }
        }
        return super.write(connectId, address, writeValue);
    }
}
