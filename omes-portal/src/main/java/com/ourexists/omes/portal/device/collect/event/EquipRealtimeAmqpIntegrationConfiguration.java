package com.ourexists.omes.portal.device.collect.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;

import java.util.Objects;

/**
 * 设备实时入站通道 + 与 RabbitMQ 的 Integration 出口（原 {@code FlinkStreamConfiguration} 中仅含通道，现合并于此）。
 * <p>
 * 协议层（MQTT 等）→ {@code equipRealtimeInputChannel} → JSON → 默认交换机投队列；独立服务 {@code omes-stream} 中 Flink {@code RMQSource} 从同队列消费。
 */
@Slf4j
@Configuration
public class EquipRealtimeAmqpIntegrationConfiguration {

    @Bean
    public MessageChannel equipRealtimeInputChannel() {
        return new DirectChannel();
    }

    @Bean
    IntegrationFlow equipRealtimeToRabbitFlow(
            @Qualifier("equipRealtimeInputChannel") MessageChannel equipRealtimeInputChannel,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${omes.device.rabbitmq.equip-realtime-queue:omes.equip.realtime}") String queueName) {
        return IntegrationFlow.from(equipRealtimeInputChannel)
                .filter(EquipRealtime.class, Objects::nonNull)
                .transform(EquipRealtime.class, r -> toJson(objectMapper, r))
                .handle(Amqp.outboundAdapter(rabbitTemplate)
                        .exchangeName("")
                        .routingKey(queueName))
                .get();
    }

    private static String toJson(ObjectMapper objectMapper, EquipRealtime r) {
        try {
            return objectMapper.writeValueAsString(r);
        } catch (JsonProcessingException e) {
            log.error("Serialize equip realtime for RabbitMQ failed, equip={}", r.getSelfCode(), e);
            throw new IllegalStateException("equip realtime JSON serialization failed", e);
        }
    }
}
