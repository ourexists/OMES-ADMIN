/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.sas.message;

import com.ourexists.omes.sas.gateway.OmesGatewayProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class MessageWebSocketRelayHandler extends TextWebSocketHandler {

    private static final String ATTR_UPSTREAM = "upstreamSession";
    private static final String ATTR_UPSTREAM_FUTURE = "upstreamFuture";

    private final OmesGatewayProperties gatewayProperties;

    public MessageWebSocketRelayHandler(OmesGatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession clientSession) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, String> propagated = (Map<String, String>) clientSession.getAttributes()
                .get(MessageWebSocketAuthHandshakeInterceptor.ATTR_PROPAGATED_HEADERS);
        if (propagated == null || propagated.isEmpty()) {
            clientSession.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        propagated.forEach(headers::add);

        URI upstreamUri = URI.create(resolveAdminWsUrl());
        CompletableFuture<WebSocketSession> upstreamFuture = new StandardWebSocketClient()
                .execute(new TextWebSocketHandler() {
                    @Override
                    protected void handleTextMessage(WebSocketSession upstreamSession, TextMessage message) throws Exception {
                        if (clientSession.isOpen()) {
                            clientSession.sendMessage(message);
                        }
                    }

                    @Override
                    public void afterConnectionClosed(WebSocketSession upstreamSession, CloseStatus status) {
                        closeQuietly(clientSession, status);
                    }
                }, headers, upstreamUri);

        clientSession.getAttributes().put(ATTR_UPSTREAM_FUTURE, upstreamFuture);
        upstreamFuture.whenComplete((upstreamSession, error) -> {
            if (error != null) {
                log.warn("message ws upstream connect failed: {}", error.getMessage());
                closeQuietly(clientSession, CloseStatus.SERVER_ERROR);
                return;
            }
            clientSession.getAttributes().put(ATTR_UPSTREAM, upstreamSession);
        });
    }

    @Override
    protected void handleTextMessage(WebSocketSession clientSession, TextMessage message) throws Exception {
        WebSocketSession upstream = resolveUpstream(clientSession);
        if (upstream != null && upstream.isOpen()) {
            upstream.sendMessage(message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession clientSession, CloseStatus status) {
        WebSocketSession upstream = (WebSocketSession) clientSession.getAttributes().remove(ATTR_UPSTREAM);
        closeQuietly(upstream, status);
    }

    private WebSocketSession resolveUpstream(WebSocketSession clientSession) throws Exception {
        WebSocketSession upstream = (WebSocketSession) clientSession.getAttributes().get(ATTR_UPSTREAM);
        if (upstream != null) {
            return upstream;
        }
        CompletableFuture<WebSocketSession> future =
                (CompletableFuture<WebSocketSession>) clientSession.getAttributes().get(ATTR_UPSTREAM_FUTURE);
        if (future == null) {
            return null;
        }
        return future.get();
    }

    private String resolveAdminWsUrl() {
        String adminBase = gatewayProperties.getAdminUrl().replaceAll("/+$", "");
        String wsBase = adminBase.replaceFirst("^http", "ws");
        return wsBase + "/message/ws";
    }

    private static void closeQuietly(WebSocketSession session, CloseStatus status) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            session.close(status);
        } catch (Exception ignored) {
            // ignore
        }
    }
}
