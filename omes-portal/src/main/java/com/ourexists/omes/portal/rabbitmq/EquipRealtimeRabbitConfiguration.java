package com.ourexists.omes.portal.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EquipRealtimeRabbitConfiguration {

    @Bean
    public Queue equipRealtimeRabbitQueue(
            @Value("${omes.device.rabbitmq.equip-realtime-queue:omes.equip.realtime}") String queueName) {
        return new Queue(queueName, true);
    }
}
