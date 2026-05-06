/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.device.protocol;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.core.equip.protocol.ProtocolConnect;
import com.ourexists.omes.device.core.equip.protocol.ProtocolManager;
import com.ourexists.omes.device.feign.GatewayFeign;
import com.ourexists.omes.device.model.GatewayDto;
import com.ourexists.omes.device.model.GatewayPageQuery;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 应用启动时自动启动所有的协议管理器
 */
@Slf4j
@Component
@Order(100)
public class ProtocolManagerRunner implements ApplicationRunner {

    @Autowired
    private GatewayFeign gatewayFeign;

    @Getter
    private volatile boolean running = false;

    @Autowired
    private List<ProtocolManager> protocolManagers;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Auto-starting MQTT subscription manager");
        if (running) {
            log.warn("MQTT subscription manager already running");
            return;
        }
        UserContext.defaultTenant();
        List<GatewayDto> gateways;
        try {
            GatewayPageQuery query = new GatewayPageQuery();
            query.setRequirePage(false);
            query.setEnabled(true);
            gateways = RemoteHandleUtils.getDataFormResponse(gatewayFeign.selectByPage(query));
        } catch (EraCommonException e) {
            log.error("Failed to load MQTT gateways: {}", e.getMessage(), e);
            return;
        }
        if (CollectionUtils.isEmpty(gateways)) {
            log.info("No MQTT gateway config found, skip starting");
            return;
        }
        for (GatewayDto gw : gateways) {
            ProtocolManager protocolManager = getProtocolManager(gw.getProtocol());
            if (protocolManager == null) {
                continue;
            }
            ProtocolConnect connect = new ProtocolConnect();
            BeanUtils.copyProperties(gw, connect);
            protocolManager.start(connect);
        }
        running = true;
    }

    @PreDestroy
    public void destroy() {
        if (!running) {
            log.warn("MQTT subscription manager not running");
            return;
        }
        for (ProtocolManager protocolManager : protocolManagers) {
            protocolManager.stopAll();
        }
        running = false;
        log.info("MQTT subscription manager stopped");
    }

    public ProtocolManager getProtocolManager(String protocol) {
        for (ProtocolManager protocolManager : protocolManagers) {
            if (protocolManager.protocol().equals(protocol)) {
                return protocolManager;
            }
        }
        return null;
    }
}
