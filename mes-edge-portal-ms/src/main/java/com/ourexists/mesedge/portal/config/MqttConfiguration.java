package com.ourexists.mesedge.portal.config;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ourexists.era.framework.core.constants.CommonConstant;
import com.ourexists.mesedge.device.core.EquipAttrRealtime;
import com.ourexists.mesedge.device.core.EquipRealtime;
import com.ourexists.mesedge.device.core.EquipRealtimeManager;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

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
            String payload = (String) message.getPayload();
            JSONObject jsonObject = JSON.parseObject(payload);
            String sn = jsonObject.getString("SN");
            JSONArray devArray = jsonObject.getJSONArray("dev");
            JSONArray ywArray = jsonObject.getJSONArray("ai");
            Map<String, EquipRealtime> realtimeMap = equipRealtimeManager.getAll(CommonConstant.SYSTEM_TENANT);
            for (EquipRealtime equipRealtime : realtimeMap.values()) {
                List<EquipAttrRealtime> attrs = equipRealtime.getEquipAttrRealtimes();
                JSONObject[] devArrays = devArray.toArray(JSONObject.class);
                JSONObject[] ywArrays = ywArray.toArray(JSONObject.class);

                //先将场景中的所有设备态重置
                if (equipRealtime.getWorkshopCode().equals(sn)) {
                    equipRealtime.setOnlineState(0);
                    equipRealtime.setAlarmState(0);
                    equipRealtime.setRunState(0);
                }

                for (JSONObject object : devArrays) {
                    String devSn = object.getString("code");
                    String ssn = sn + "dev" + devSn;
                    if (equipRealtime.getSelfCode().equals(ssn)) {
                        equipRealtime.setOnlineState(1);
                        equipRealtime.setTime(new Date());
                        equipRealtime.setAlarmState(object.getInteger(equipRealtime.getAlarmMap()));
                        equipRealtime.setRunState(object.getInteger(equipRealtime.getRunMap()));
                        if (!CollectionUtils.isEmpty(attrs)) {
                            for (EquipAttrRealtime attr : attrs) {
                                attr.setValue(object.getString(attr.getMap()));
                            }
                        }
                    }
                }
                for (JSONObject o : ywArrays) {
                    String ywSn = o.getString("code");
                    String ssnn = sn + ywSn;
                    if (equipRealtime.getSelfCode().equals(ssnn)) {
                        equipRealtime.setOnlineState(1);
                        int alarm = 0;
                        if (o.getInteger(equipRealtime.getAlarmMap()) == 1) {
                            alarm = 1;
                        }
                        if (o.getInteger("hhAlarm") == 1) {
                            alarm = 2;
                        }
                        equipRealtime.setAlarmState(alarm);
                        equipRealtime.setRunState(1);
                        if (!CollectionUtils.isEmpty(attrs)) {
                            for (EquipAttrRealtime attr : attrs) {
                                attr.setValue(o.getString(attr.getMap()));
                            }
                        }
                        equipRealtime.setTime(new Date());
                    }
                }
                equipRealtimeManager.reset(CommonConstant.SYSTEM_TENANT, realtimeMap);
            }
        };
    }

    @Bean
    public MessageProducer inbound(MqttPahoClientFactory clientFactory, MessageChannel mqttInboundChannel) {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(clientId + "_input", clientFactory, "fxx/fb");
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
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(clientId + "_output", factory);
        handler.setAsync(true);
        handler.setDefaultTopic("fxx/fb");
        return handler;
    }

}
