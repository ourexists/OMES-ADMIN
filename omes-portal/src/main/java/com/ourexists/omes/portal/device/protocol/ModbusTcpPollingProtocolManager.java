/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.device.protocol;

import com.alibaba.fastjson2.JSONObject;
import com.ourexists.omes.device.core.equip.protocol.ProtocolConnect;
import com.ourexists.omes.device.feign.EquipFeign;
import com.ourexists.omes.portal.device.collect.S7EquipDataParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public ModbusTcpPollingProtocolManager(EquipFeign equipFeign, S7EquipDataParser equipDataParser) {
        super(equipFeign, equipDataParser, READ_TIMEOUT_MS, "modbus-polling-");
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
