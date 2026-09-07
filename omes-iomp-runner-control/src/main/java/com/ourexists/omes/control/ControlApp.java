package com.ourexists.omes.control;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 设备控制消费者：从 RabbitMQ 按设备顺序执行下发。
 */
@SpringBootApplication
@EnableScheduling
@EnableRabbit
public class ControlApp {

    public static void main(String[] args) {
        SpringApplication.run(ControlApp.class, args);
    }
}
