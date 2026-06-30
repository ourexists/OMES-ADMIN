/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.message;

import com.alibaba.fastjson2.JSON;
import com.ourexists.omes.message.model.MessageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class MessageRealtimeHub {

    private static final String EVENT_MESSAGE = "message";

    private final Map<String, Set<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();
    private final Map<String, String> sessionKeys = new ConcurrentHashMap<>();

    public void register(String platform, String accId, WebSocketSession session) {
        if (!StringUtils.hasText(platform) || !StringUtils.hasText(accId) || session == null) {
            return;
        }
        String key = sessionKey(platform, accId);
        sessionsByUser.computeIfAbsent(key, ignored -> ConcurrentHashMap.newKeySet()).add(session);
        sessionKeys.put(session.getId(), key);
        log.info("message ws connected. platform[{}] accId[{}] session[{}]", platform, accId, session.getId());
    }

    public void unregister(WebSocketSession session) {
        if (session == null) {
            return;
        }
        String key = sessionKeys.remove(session.getId());
        if (!StringUtils.hasText(key)) {
            return;
        }
        Set<WebSocketSession> sessions = sessionsByUser.get(key);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionsByUser.remove(key);
            }
        }
        log.info("message ws disconnected. session[{}]", session.getId());
    }

    public void pushNewMessage(String platform, List<String> accIds, MessageVo message) {
        if (message == null || CollectionUtils.isEmpty(accIds) || !StringUtils.hasText(platform)) {
            return;
        }
        String payload = JSON.toJSONString(Map.of(
                "event", EVENT_MESSAGE,
                "data", message
        ));
        for (String accId : accIds) {
            pushRaw(platform, accId, payload);
        }
    }

    private void pushRaw(String platform, String accId, String payload) {
        Set<WebSocketSession> sessions = sessionsByUser.get(sessionKey(platform, accId));
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        TextMessage textMessage = new TextMessage(payload);
        for (WebSocketSession session : sessions) {
            if (session == null || !session.isOpen()) {
                continue;
            }
            try {
                synchronized (session) {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                    }
                }
            } catch (IOException ex) {
                log.warn("message ws push failed. session[{}] {}", session.getId(), ex.getMessage());
            }
        }
    }

    private static String sessionKey(String platform, String accId) {
        return platform + ":" + accId;
    }
}
