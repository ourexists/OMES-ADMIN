package com.ourexists.mesedge.portal.config;

import jakarta.annotation.Resource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

@Component
public class MqttSender {

    @Resource(name = "mqttOutboundChannel")
    private MessageChannel messageChannel;

    public void send(String topic, String payload) {
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader("mqtt_topic", topic)
                .build();
        messageChannel.send(message);
    }

}
