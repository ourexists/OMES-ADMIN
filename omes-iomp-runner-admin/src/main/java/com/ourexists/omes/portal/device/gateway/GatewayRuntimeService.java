/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.device.gateway;

import com.ourexists.omes.device.core.equip.protocol.ProtocolConnect;
import com.ourexists.omes.device.core.equip.protocol.ProtocolExecutor;
import com.ourexists.omes.device.core.equip.protocol.ProtocolManager;
import com.ourexists.omes.device.model.GatewayDto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Device gateway protocol lifecycle (MQTT / PLC polling, etc.).
 * Runs in Admin where {@link ProtocolManager} implementations are registered.
 */
@Service
public class GatewayRuntimeService {

    @Autowired
    private ProtocolExecutor protocolExecutor;

    public boolean start(GatewayDto gateway) {
        if (gateway == null) {
            return false;
        }
        ProtocolConnect connect = toConnect(gateway);
        return protocolExecutor.start(connect);
    }

    public boolean stop(GatewayDto gateway) {
        if (gateway == null || gateway.getId() == null) {
            return false;
        }
        return protocolExecutor.stop(gateway.getProtocol(), gateway.getId());
    }

    public ProtocolManager protocolManager(String protocol) {
        return protocolExecutor.protocolManager(protocol);
    }

    public static ProtocolConnect toConnect(GatewayDto gateway) {
        ProtocolConnect connect = new ProtocolConnect();
        BeanUtils.copyProperties(gateway, connect);
        return connect;
    }
}
