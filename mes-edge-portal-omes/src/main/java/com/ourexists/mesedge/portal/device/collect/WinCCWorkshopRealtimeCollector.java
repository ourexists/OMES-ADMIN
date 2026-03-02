package com.ourexists.mesedge.portal.device.collect;

import com.ourexists.mesedge.device.core.workshop.cache.WorkshopRealtime;
import com.ourexists.mesedge.device.core.workshop.cache.WorkshopRealtimeCollect;
import com.ourexists.mesedge.device.core.workshop.cache.WorkshopRealtimeManager;
import com.ourexists.mesedge.device.core.workshop.collect.WorkshopRealtimeCollector;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class WinCCWorkshopRealtimeCollector implements WorkshopRealtimeCollector {

    @Autowired
    private WorkshopRealtimeManager manager;

    @Override
    public String name() {
        return "WINCC";
    }

    @Override
    public void doCollect(Object source) {
        if (source == null) {
            return;
        }
        Map<String, Object> m = (Map<String, Object>) source;
        Map<String, WorkshopRealtime> map = manager.getAll();

        for (Map.Entry<String, WorkshopRealtime> entry : map.entrySet()) {
            List<WorkshopRealtimeCollect> attrsRealtime = new ArrayList<>();
            for (WorkshopRealtimeCollect attr : entry.getValue().getConfig().getAttrs()) {
                Object d = m.get(attr.getMap());
                if (d == null) {
                    continue;
                }
                WorkshopRealtimeCollect target = new WorkshopRealtimeCollect();
                BeanUtils.copyProperties(attr, target);
                target.setValue(d.toString());
                attrsRealtime.add(target);
            }
            entry.getValue().setAttrsRealtime(attrsRealtime);
        }
    }

    @Override
    public List<String> collectVariables() {
        Map<String, WorkshopRealtime> map = manager.getAll();
        List<String> variables = new ArrayList<>();
        for (WorkshopRealtime value : map.values()) {
            for (WorkshopRealtimeCollect attr : value.getConfig().getAttrs()) {
                variables.add(attr.getMap());
            }
        }
        return variables;
    }
}
