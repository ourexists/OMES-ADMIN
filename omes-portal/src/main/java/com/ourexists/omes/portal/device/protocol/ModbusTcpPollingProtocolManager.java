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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Modbus TCP 定时轮询协议管理器（基于 Apache PLC4X plc4j）。
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
public class ModbusTcpPollingProtocolManager extends AbstractPlc4xPollingProtocolManager {

    private static final int DEFAULT_PORT = 502;
    private static final int READ_TIMEOUT_MS = 10_000;

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
}
