package com.ourexists.mesedge.device.core.workshop.collect;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备实时采集器
 */
@Component
public class WorkshopRealtimeCollectSelector {

    private List<WorkshopRealtimeCollector> collectors;

    public WorkshopRealtimeCollectSelector(
            List<WorkshopRealtimeCollector> collectors) {
        this.collectors = collectors;
    }

    public WorkshopRealtimeCollector getCollector(String name) {
        WorkshopRealtimeCollector collector = null;
        for (WorkshopRealtimeCollector r : collectors) {
            if (r.name().equals(name)) {
                collector = r;
            }
        }
        return collector;
    }

    public List<String> getAllNames() {
        List<String> names = new ArrayList<>();
        for (WorkshopRealtimeCollector r : collectors) {
            names.add(r.name());
        }
        return names;
    }
}
