package com.ourexists.omes.portal.device.collect.event;

import com.ourexists.omes.device.core.equip.cache.EquipRealtime;

public class EquipRealtimeHandleEvent {

    private final EquipRealtime target;

    public EquipRealtimeHandleEvent(EquipRealtime target) {
        this.target = target;
    }

    public EquipRealtime getTarget() {
        return target;
    }
}
