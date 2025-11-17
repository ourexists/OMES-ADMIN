package com.ourexists.mesedge.portal.config;

import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mqtt")
public class MqttConfiguration {

    private List<String> serverUri;

    private String username;

    private String password;

    private String clientId;

    private Integer connectionTimeout = 50;

    private Integer keepAliveInterval = 3;

    public static final String CHANNEL_INPUT = "mqttInboundChannel";

    public static final String CHANNEL_OUTPUT = "mqttOutboundChannel";

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
//        options.setUserName(username);
//        options.setPassword(password.toCharArray());
        options.setServerURIs(serverUri.toArray(new String[0]));
        options.setConnectionTimeout(connectionTimeout);
        options.setKeepAliveInterval(keepAliveInterval);
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        return options;
    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory(MqttConnectOptions options) {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);
        return factory;
    }

    // 接收通道
    @Bean(name = CHANNEL_INPUT)
    public MessageChannel mqttInboundChannel() {
        return new DirectChannel();
    }

    // 消息处理器
    @Bean
    @ServiceActivator(inputChannel = CHANNEL_INPUT)
    public MessageHandler handler() {
        return message -> {
            MessageHeaders topic = message.getHeaders();
            String payload = (String) message.getPayload();
            System.out.println("接收到 MQTT 消息：topic = " + JSON.toJSONString(topic) + ", payload = " + payload);
        };
    }

    @Bean
    public MessageProducer inbound(MqttPahoClientFactory clientFactory,
                                   MessageChannel mqttInboundChannel) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId+"_input", clientFactory, "fxx/fb");
        adapter.setOutputChannel(mqttInboundChannel);
        adapter.setQos(1);
        return adapter;
    }

    @Bean(name = CHANNEL_OUTPUT)
    public MessageChannel mqttOutboundChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = CHANNEL_OUTPUT)
    public MessageHandler mqttOutbound(MqttPahoClientFactory factory) {
        MqttPahoMessageHandler handler =
                new MqttPahoMessageHandler(clientId+"_output", factory);
        handler.setAsync(true);
        handler.setDefaultTopic("fxx/fb");
        return handler;
    }

}
