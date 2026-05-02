package com.ourexists.omes.stream.equip.sink.bridge;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.springframework.util.StringUtils;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * stream 侧将持久化载荷写入 RabbitMQ，由门户消费后调 device Feign 入库（桥接）。
 */
@Slf4j
public abstract class AbstractEquipStreamPersistRmqSink<T> extends RichSinkFunction<T> {

    protected final RMQConnectionConfig rmqConnectionConfig;
    protected final String queueName;

    protected transient ObjectMapper objectMapper;
    protected transient Connection connection;
    protected transient Channel channel;

    protected AbstractEquipStreamPersistRmqSink(RMQConnectionConfig rmqConnectionConfig, String queueName) {
        this.rmqConnectionConfig = rmqConnectionConfig;
        this.queueName = queueName;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        objectMapper = new ObjectMapper();
        if (!StringUtils.hasText(queueName) || rmqConnectionConfig == null) {
            log.warn("{} disabled: queue empty or RMQ config null", getClass().getSimpleName());
            return;
        }
        try {
            com.rabbitmq.client.ConnectionFactory cf = rmqConnectionConfig.getConnectionFactory();
            connection = cf.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(queueName, true, false, false, null);
        } catch (Exception e) {
            log.error("{}: RabbitMQ open failed", getClass().getSimpleName(), e);
            closeQuietly();
        }
    }

    protected void publish(byte[] body) {
        if (!StringUtils.hasText(queueName) || channel == null || !channel.isOpen()) {
            return;
        }
        try {
            channel.basicPublish("", queueName, null, body);
        } catch (IOException e) {
            log.error("{}: publish failed queue={}", getClass().getSimpleName(), queueName, e);
        }
    }

    @Override
    public void close() throws Exception {
        closeQuietly();
        super.close();
    }

    private void closeQuietly() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (Exception e) {
            log.warn("{}: channel close", getClass().getSimpleName(), e);
        }
        channel = null;
        try {
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (Exception e) {
            log.warn("{}: connection close", getClass().getSimpleName(), e);
        }
        connection = null;
    }
}
