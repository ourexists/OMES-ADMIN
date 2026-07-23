/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.sas.message;

import com.ourexists.era.framework.core.EraSystemHeader;
import com.ourexists.era.oauth2.core.EraUser;
import com.ourexists.era.oauth2.core.gateway.GatewayIdentityHeaders;
import com.ourexists.era.oauth2.core.gateway.GatewayIdentitySupport;
import com.ourexists.era.oauth2.core.interceptor.EraUserResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class MessageWebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_PROPAGATED_HEADERS = "propagatedHeaders";

    private final JwtDecoder jwtDecoder;
    private final EraUserResolver eraUserResolver;
    private final String internalServiceKey;

    public MessageWebSocketAuthHandshakeInterceptor(JwtDecoder jwtDecoder,
                                                    EraUserResolver eraUserResolver,
                                                    @Value("${omes.internal.service-key:}") String internalServiceKey) {
        this.jwtDecoder = jwtDecoder;
        this.eraUserResolver = eraUserResolver;
        this.internalServiceKey = internalServiceKey;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("message ws relay rejected: missing token");
            return false;
        }
        try {
            Jwt jwt = jwtDecoder.decode(token);
            EraUser eraUser = eraUserResolver.resolve(new JwtAuthenticationToken(jwt, Collections.emptyList()));
            if (eraUser == null) {
                log.warn("message ws relay rejected: user not found");
                return false;
            }
            String tenantId = queryParam(request, "tenant");
            if (!StringUtils.hasText(tenantId)) {
                tenantId = request.getHeaders().getFirst(EraSystemHeader.TENANT_ROUTE);
            }
            String platform = queryParam(request, "platform");
            if (!StringUtils.hasText(platform)) {
                platform = request.getHeaders().getFirst(EraSystemHeader.PLATFORM_HEADER);
            }
            Map<String, String> propagated = new LinkedHashMap<>(
                    GatewayIdentitySupport.buildPropagationHeaders(eraUser, tenantId));
            if (StringUtils.hasText(internalServiceKey)) {
                propagated.put(GatewayIdentityHeaders.INTERNAL_SERVICE_KEY, internalServiceKey);
            }
            if (StringUtils.hasText(platform)) {
                propagated.put(EraSystemHeader.PLATFORM_HEADER, platform);
            }
            if (StringUtils.hasText(tenantId)) {
                propagated.put(EraSystemHeader.TENANT_ROUTE, tenantId);
            }
            attributes.put(ATTR_PROPAGATED_HEADERS, propagated);
            return true;
        } catch (JwtException ex) {
            log.warn("message ws relay rejected: invalid token");
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // no-op
    }

    private static String resolveToken(ServerHttpRequest request) {
        String token = queryParam(request, "access_token");
        if (!StringUtils.hasText(token)) {
            token = queryParam(request, "token");
        }
        if (!StringUtils.hasText(token)) {
            String authorization = request.getHeaders().getFirst(EraSystemHeader.AUTH_HEADER);
            if (StringUtils.hasText(authorization) && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
                token = authorization.substring(7).trim();
            }
        }
        return stripBearerPrefix(token);
    }

    private static String stripBearerPrefix(String token) {
        if (!StringUtils.hasText(token)) {
            return token;
        }
        String trimmed = token.trim();
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return trimmed.substring(7).trim();
        }
        return trimmed;
    }

    private static String queryParam(ServerHttpRequest request, String name) {
        return UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst(name);
    }
}
