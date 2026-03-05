/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.device.protocol;

import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.omes.device.core.equip.collect.DataCollectChain;
import com.ourexists.omes.device.core.equip.protocol.ProtocolConnect;
import com.ourexists.omes.device.core.equip.protocol.ProtocolManager;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 统一 MQTT 订阅管理器：根据网关配置动态创建/销毁订阅连接，支持启停控制。
 * 使用 Eclipse Paho 原生 MqttClient，避免 Spring Integration 适配器导致的“一连接/一收消息就断”问题。
 */
@Slf4j
@Component
public class MqttSubscriptionManager implements ProtocolManager {

    private static final int CONNECTION_TIMEOUT = 30;
    private static final int KEEP_ALIVE_INTERVAL = 30;
    private static final String DEFAULT_CONNECTION_KEY = "mqtt_default";


    private final DataCollectChain dataCollectChain;

    private static final Executor MQTT_HANDLER_EXECUTOR = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "mqtt-handler");
        t.setDaemon(true);
        return t;
    });

    /** key: gatewayId，value: 该网关的 MqttClient */
    private final Map<String, MqttClient> connectionMap = new ConcurrentHashMap<>();

    public MqttSubscriptionManager(DataCollectChain dataCollectChain) {
        this.dataCollectChain = dataCollectChain;
    }

    @Override
    public synchronized void stopAll() {
        List<String> keys = new ArrayList<>(connectionMap.keySet());
        for (String key : keys) {
            stop(key);
        }
    }

    @Override
    public String protocol() {
        return "MQTT";
    }

    @Override
    public synchronized boolean start(ProtocolConnect gw) {
        String key = gw.getId();
        if (connectionMap.containsKey(key)) {
            log.debug("Connection already exists for gateway: {}", key);
            return false;
        }
        try {
            UserContext.defaultTenant();
            MqttClient client = createAndConnect(gw);
            connectionMap.put(key, client);
            log.info("MQTT subscription started for gateway: {} topic: {}", gw.getServerName(), gw.getTopic());
            return true;
        } catch (Exception e) {
            log.error("Failed to start MQTT for gateway {}: {}", gw.getServerName(), e.getMessage(), e);
            return false;
        }
    }

    public synchronized boolean stop(String gatewayId) {
        if (gatewayId == null || gatewayId.isEmpty()) {
            return false;
        }
        MqttClient client = connectionMap.remove(gatewayId);
        if (client == null) {
            log.debug("No connection found for gateway: {}", gatewayId);
            return false;
        }
        try {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();
            log.info("MQTT subscription stopped for gateway: {}", gatewayId);
            return true;
        } catch (MqttException e) {
            log.error("Error stopping MQTT client {}: {}", gatewayId, e.getMessage(), e);
            return true;
        }
    }

    private MqttClient createAndConnect(ProtocolConnect gw) throws MqttException {
        String serverName = gw.getServerName() != null ? gw.getServerName() : DEFAULT_CONNECTION_KEY;
        String baseId = StringUtils.hasText(gw.getId()) ? gw.getId() : serverName;
        String clientId = "omes_mqtt_" + baseId;

        MqttClient client = new MqttClient(gw.getUri(), clientId, null);

        MqttConnectOptions options = new MqttConnectOptions();
        if (StringUtils.hasText(gw.getUsername()) && StringUtils.hasText(gw.getPassword())) {
            options.setUserName(gw.getUsername());
            options.setPassword(gw.getPassword().toCharArray());
        }
        options.setConnectionTimeout(CONNECTION_TIMEOUT);
        options.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
        options.setAutomaticReconnect(true);
        options.setMaxReconnectDelay(10_000);
        options.setCleanSession(true);

        client.connect(options);

        IMqttMessageListener listener = (topic, message) -> {
            String payload = decodePayload(message.getPayload());
            if (payload == null) {
                return;
            }
            MQTT_HANDLER_EXECUTOR.execute(() -> {
                UserContext.defaultTenant();
                try {
                    dataCollectChain.doCollect(gw.getId(), payload);
                } catch (Exception e) {
                    log.error("MQTT message handle error, payload length: {}", payload.length(), e);
                }
            });
        };

        client.subscribe(gw.getTopic(), 1, listener);
        return client;
    }

    private static String decodePayload(byte[] payload) {
        if (payload == null || payload.length == 0) {
            return null;
        }
        try {
            return new String(payload, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return new String(payload, StandardCharsets.ISO_8859_1);
        }
    }
}
