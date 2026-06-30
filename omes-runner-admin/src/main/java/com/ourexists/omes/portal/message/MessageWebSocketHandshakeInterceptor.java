/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.message;

import com.ourexists.era.framework.core.EraSystemHeader;
import com.ourexists.era.oauth2.core.EraUser;
import com.ourexists.era.oauth2.core.gateway.GatewayIdentitySupport;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
public class MessageWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_ACC_ID = "accId";
    public static final String ATTR_PLATFORM = "platform";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }
        HttpServletRequest httpRequest = servletRequest.getServletRequest();
        EraUser eraUser = GatewayIdentitySupport.parseEraUser(httpRequest);
        if (eraUser == null || eraUser.getUserInfo() == null || !StringUtils.hasText(eraUser.getUserInfo().getId())) {
            log.warn("message ws handshake rejected: missing gateway identity");
            return false;
        }
        String platform = EraSystemHeader.extractPlatform(httpRequest);
        if (!StringUtils.hasText(platform)) {
            log.warn("message ws handshake rejected: missing platform");
            return false;
        }
        attributes.put(ATTR_ACC_ID, eraUser.getUserInfo().getId());
        attributes.put(ATTR_PLATFORM, platform);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // no-op
    }
}
