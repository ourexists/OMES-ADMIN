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

    public void doCollect(String name, String sourceJSONData) {
//        EquipRealtime equipRealtime = equipRealtimeManager.get(tenantId, sn);
//        String collectType = equipRealtime.getEquipRealtimeConfig().getCollectType();
//        if (!StringUtils.hasText(collectType)) {
//            return;
//        }
        EquipRealtimeCollector collector = getCollector(name);
        if (collector == null) {
            return;
        }
        collector.doCollect(sourceJSONData);
    }

    private EquipRealtimeCollector getCollector(String name) {
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
