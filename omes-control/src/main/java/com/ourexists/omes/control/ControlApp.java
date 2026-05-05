package com.ourexists.omes.control;

import org.springframework.boot.SpringApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 设备控制消费者：从 RabbitMQ 按设备顺序执行下发。
 */
@SpringBootApplication(scanBasePackages = {"com.ourexists.omes.control", "com.ourexists.omes.device.gateway"})
@EnableScheduling
@EnableFeignClients(basePackages = {"com.ourexists.omes.device.feign", "com.ourexists.omes.ucenter.feign"})
@PropertySource(value = {"file:config/config.properties"}, ignoreResourceNotFound = true)
@EnableRabbit
public class ControlApp {

    public static void main(String[] args) {
        SpringApplication.run(ControlApp.class, args);
    }
}
