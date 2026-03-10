package com.ourexists.omes.portal.device.protocol;

import com.ourexists.omes.device.core.equip.protocol.ProtocolConnect;
import com.ourexists.omes.portal.device.collect.JSONEquipDataParser;
import com.ourexists.omes.portal.device.collect.JSONWorkshopDataParser;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonMqttProtocolManager extends AbstractMqttProtocolManager {

    @Autowired
    private JSONEquipDataParser equipDataParser;

    @Autowired
    private JSONWorkshopDataParser JSONWorkshopDataParser;

    @Override
    public String protocol() {
        return "MQTT";
    }

    @Override
    protected IMqttMessageListener getListener(ProtocolConnect gw) {
        return (topic, message) -> {
            String payload = decodePayload(message.getPayload());
            if (payload == null) {
                return;
            }
            equipDataParser.parse(gw.getId(), payload);
            JSONWorkshopDataParser.parse(gw.getId(), payload);
        };
    }
}
