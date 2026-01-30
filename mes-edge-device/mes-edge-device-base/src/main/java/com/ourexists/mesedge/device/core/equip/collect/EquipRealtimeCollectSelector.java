package com.ourexists.mesedge.device.core.equip.collect;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备实时采集器
 */
@Component
public class EquipRealtimeCollectSelector {


    private List<EquipRealtimeCollector> collectors;

    public EquipRealtimeCollectSelector(
            List<EquipRealtimeCollector> collectors) {
        this.collectors = collectors;
    }

    public EquipRealtimeCollector getCollector(String name) {
        EquipRealtimeCollector collector = null;
        for (EquipRealtimeCollector r : collectors) {
            if (r.name().equals(name)) {
                collector = r;
            }
        }
        return collector;
    }

    public List<String> getAllNames() {
        List<String> names = new ArrayList<String>();
        for (EquipRealtimeCollector r : collectors) {
            names.add(r.name());
        }
        return names;
    }
}
