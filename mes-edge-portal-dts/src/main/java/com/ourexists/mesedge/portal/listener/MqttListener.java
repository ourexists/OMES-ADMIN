package com.ourexists.mesedge.portal.listener;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mqtt")
public class MqttListener {

    private List<String> serverUri;

    private String username;

    private String password;

    private String clientId;

    private Integer connectionTimeout = 50;

    private Integer keepAliveInterval = 3;

    private String inputTopic;

    public static final String CHANNEL_INPUT = "mqttInboundChannel";

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
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
                new MqttPahoMessageDrivenChannelAdapter(clientId, clientFactory, inputTopic);
        adapter.setOutputChannel(mqttInboundChannel);
        adapter.setQos(1);
        return adapter;
    }

}
