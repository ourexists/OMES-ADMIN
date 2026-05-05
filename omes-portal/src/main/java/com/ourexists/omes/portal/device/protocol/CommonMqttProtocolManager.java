package com.ourexists.omes.portal.device.protocol;

import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.device.core.equip.protocol.ProtocolConnect;
import com.ourexists.omes.portal.device.collect.JSONEquipDataParser;
import com.ourexists.omes.portal.device.collect.JSONWorkshopDataParser;
import com.ourexists.omes.portal.mq.EquipRealtimeStreamOutbound;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class CommonMqttProtocolManager extends AbstractMqttProtocolManager {

    @Autowired
    private JSONEquipDataParser equipDataParser;

    @Autowired
    private JSONWorkshopDataParser dataParser;

    @Autowired
    private EquipRealtimeStreamOutbound equipRealtimeStreamOutbound;

    @Override
    public String protocol() {
        return "MQTT";
    }

    @Override
    protected IMqttMessageListener getListener(ProtocolConnect gw) {
        return (topic, message) -> {
            UserContext.defaultTenant();
            String payload = decodePayload(message.getPayload());
            if (payload == null) {
                return;
            }
            List<EquipRealtime> realtimes = equipDataParser.parse(gw.getId(), payload);
            if (!CollectionUtils.isEmpty(realtimes)) {
                for (EquipRealtime target : realtimes) {
                    equipRealtimeStreamOutbound.send(target);
                }
            }
            dataParser.parse(gw.getId(), payload);
        };
    }
}
