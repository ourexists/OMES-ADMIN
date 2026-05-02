package com.ourexists.omes.portal.rabbitmq;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EquipNotifyCreateRabbitConfiguration {

    @Bean
    public Queue equipNotifyCreateQueue(
            @Value("${omes.device.rabbitmq.equip-notify-create-queue:omes.equip.notify.create}") String queueName) {
        return new Queue(queueName, true);
    }
}
