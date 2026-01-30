package com.ourexists.mesedge.portal.config;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.device.core.equip.collect.EquipRealtimeCollectSelector;
import com.ourexists.mesedge.device.core.equip.collect.EquipRealtimeCollector;
import com.ourexists.mesedge.sync.feign.ConnectFeign;
import com.ourexists.mesedge.sync.model.ConnectDto;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
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

@Slf4j
@Getter
@Setter
@Configuration
public class MqttConfiguration {

    @Value("${spring.application.name}")
    private String projectName;

    private Integer connectionTimeout = 50;

    private Integer keepAliveInterval = 3;

    public static final String CHANNEL_INPUT = "TC_Mqtt";

    private ConnectFeign connectFeign;

    private EquipRealtimeCollectSelector equipRealtimeCollectSelector;


    private ConnectDto connectDto;

    public MqttConfiguration(ConnectFeign connectFeign,
                             EquipRealtimeCollectSelector equipRealtimeCollectSelector) {
        this.connectFeign = connectFeign;
        this.equipRealtimeCollectSelector = equipRealtimeCollectSelector;
        UserContext.defaultTenant();
        try {
            connectDto = RemoteHandleUtils.getDataFormResponse(connectFeign.selectConnectByName(CHANNEL_INPUT));
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        if (connectDto == null) {
            return null;
        }
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(connectDto.getUsername());
        options.setPassword(connectDto.getPassword().toCharArray());
        options.setServerURIs(new String[]{connectDto.getHost() + ":" + connectDto.getPort()});
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
            UserContext.defaultTenant();
            String payload = (String) message.getPayload();
            EquipRealtimeCollector collector = equipRealtimeCollectSelector.getCollector(CHANNEL_INPUT);
            if (collector != null) {
                collector.doCollect(payload);
            }
        };
    }

    @Bean
    public MessageProducer inbound(MqttPahoClientFactory clientFactory,
                                   MessageChannel TC_Mqtt) {
        if (connectDto == null) {
            return null;
        }
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(projectName + "_listener", clientFactory, connectDto.getSuffix());
        adapter.setOutputChannel(TC_Mqtt);
        adapter.setQos(1);
        return adapter;
    }
}
