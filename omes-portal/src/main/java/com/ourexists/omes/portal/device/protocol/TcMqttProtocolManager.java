/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.device.protocol;

import com.ourexists.omes.device.core.equip.protocol.ProtocolConnect;
import com.ourexists.omes.portal.device.collect.TcMqttEquipDataParser;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 统一 MQTT 订阅管理器：根据网关配置动态创建/销毁订阅连接，支持启停控制。
 * 使用 Eclipse Paho 原生 MqttClient，避免 Spring Integration 适配器导致的“一连接/一收消息就断”问题。
 */
@Slf4j
@Component
public class TcMqttProtocolManager extends AbstractMqttProtocolManager {

    @Autowired
    private TcMqttEquipDataParser tcMqttEquipDataParser;

    @Override
    public String protocol() {
        return "TC_MQTT";
    }

    @Override
    protected IMqttMessageListener getListener(ProtocolConnect gw) {
        return (topic, message) -> {
            String payload = decodePayload(message.getPayload());
            if (payload == null) {
                return;
            }
            tcMqttEquipDataParser.parse(gw.getId(), payload);
        };
    }
}
