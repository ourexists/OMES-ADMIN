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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OPC UA 定时轮询协议管理器（基于 Apache PLC4X plc4j）。
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
 */
@Slf4j
@Component
public class OPCUaPollingProtocolManager extends AbstractPlc4xPollingProtocolManager {

    private static final int DEFAULT_PORT = 4840;
    private static final int READ_TIMEOUT_MS = 30_000;

    public OPCUaPollingProtocolManager(EquipFeign equipFeign,
                                       WorkshopFeign workshopFeign,
                                       PlcEquipDataParser equipDataParser,
                                       PlcWorkshopDataParser workshopDataParser) {
        super(equipFeign, workshopFeign, equipDataParser, workshopDataParser, READ_TIMEOUT_MS, "opcua-polling-");
    }

    @Override
    public String protocol() {
        return "OPC UA";
    }

    @Override
    protected ConnectSpec buildConnectSpec(ProtocolConnect connect, Map<String, String> tags) {
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
        url.append("?").append(String.join("&", options));

        return new ConnectSpec(url.toString(), new LinkedHashMap<>(tags));
    }
}
