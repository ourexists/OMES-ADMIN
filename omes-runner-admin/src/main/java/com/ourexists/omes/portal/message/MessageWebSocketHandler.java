/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private final MessageRealtimeHub messageRealtimeHub;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String accId = (String) session.getAttributes().get(MessageWebSocketHandshakeInterceptor.ATTR_ACC_ID);
        String platform = (String) session.getAttributes().get(MessageWebSocketHandshakeInterceptor.ATTR_PLATFORM);
        messageRealtimeHub.register(platform, accId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        messageRealtimeHub.unregister(session);
    }
}
