/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.device.protocol;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.core.equip.protocol.ProtocolManager;
import com.ourexists.omes.device.feign.GatewayFeign;
import com.ourexists.omes.device.model.GatewayDto;
import com.ourexists.omes.device.model.GatewayPageQuery;
import com.ourexists.omes.portal.device.gateway.GatewayRuntimeService;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 应用启动时自动拉起已启用的设备网关协议连接。
 */
@Slf4j
@Component
@Order(100)
public class ProtocolManagerRunner implements ApplicationRunner {

    @Autowired
    private GatewayFeign gatewayFeign;

    @Autowired
    private GatewayRuntimeService gatewayRuntimeService;

    @Getter
    private volatile boolean running = false;

    @Autowired
    private List<ProtocolManager> protocolManagers;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Auto-starting enabled device gateway connections");
        if (running) {
            log.warn("Device gateway protocol runner already running");
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
            log.error("Failed to load enabled gateways: {}", e.getMessage(), e);
            return;
        }
        if (CollectionUtils.isEmpty(gateways)) {
            log.info("No enabled gateway config found, skip starting");
            return;
        }
        for (GatewayDto gw : gateways) {
            if (gatewayRuntimeService.protocolManager(gw.getProtocol()) == null) {
                continue;
            }
            gatewayRuntimeService.start(gw);
        }
        running = true;
    }

    @PreDestroy
    public void destroy() {
        if (!running) {
            log.warn("Device gateway protocol runner not running");
            return;
        }
        for (ProtocolManager protocolManager : protocolManagers) {
            protocolManager.stopAll();
        }
        running = false;
        log.info("Device gateway protocol runner stopped");
    }

    public ProtocolManager getProtocolManager(String protocol) {
        return gatewayRuntimeService.protocolManager(protocol);
    }
}
