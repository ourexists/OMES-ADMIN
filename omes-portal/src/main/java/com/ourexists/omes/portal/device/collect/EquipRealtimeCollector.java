package com.ourexists.omes.portal.device.collect;

import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipRealtimeConfig;
import com.ourexists.omes.device.core.equip.cache.EquipRealtimeManager;
import com.ourexists.omes.device.core.equip.collect.DataCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class EquipRealtimeCollector implements DataCollector {

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Autowired
    private List<EquipDataParser> equipDataParsers;

    @Override
    public void doCollect(String gwId, Object sourceData) {
        Map<String, EquipRealtime> realtimeMap = equipRealtimeManager.getAll();
        List<EquipRealtime> targets = new ArrayList<>();
        for (EquipRealtime equipRealtime : realtimeMap.values()) {
            EquipRealtimeConfig equipRealtimeConfig = equipRealtime.getEquipRealtimeConfig();
            if (equipRealtimeConfig == null) {
                continue;
            }
            //采集方式不匹配
            if (!gwId.equals(equipRealtimeConfig.getGwId())) {
                continue;
            }
            EquipDataParser equipDataParser = getDataParser(equipRealtimeConfig.getCollectType());
            if (equipDataParser != null) {
                EquipRealtime target = equipDataParser.doParse(equipRealtime, sourceData);
                if (target != null) {
                    targets.add(target);
                }
            }
        }
        if (!CollectionUtils.isEmpty(targets)) {
            equipRealtimeManager.realtimeHandle(targets);
        }
    }

    private EquipDataParser getDataParser(String name) {
        for (EquipDataParser equipDataParser : equipDataParsers) {
            if (equipDataParser.name().equals(name)) {
                return equipDataParser;
            }
        }
        return null;
    }

}
