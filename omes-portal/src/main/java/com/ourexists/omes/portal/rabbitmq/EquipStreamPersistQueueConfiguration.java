package com.ourexists.omes.portal.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EquipStreamPersistQueueConfiguration {

    @Bean
    public Queue equipStreamPersistQueue(
            @Value("${omes.device.rabbitmq.equip-stream-persist-queue:omes.equip.stream.persist}") String queueName) {
        return new Queue(queueName, true);
    }
}
