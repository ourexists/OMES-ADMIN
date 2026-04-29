package com.ourexists.omes.portal.device.collect.event;

import com.ourexists.omes.device.core.equip.cache.EquipRealtimeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class EquipRealtimeHandleEventSubscriber {

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @EventListener
    public void onEquipRealtimeHandleEvent(EquipRealtimeHandleEvent event) {
        if (event == null || event.getTarget() == null) {
            return;
        }
        equipRealtimeManager.realtimeHandle(Collections.singletonList(event.getTarget()));
    }
}
