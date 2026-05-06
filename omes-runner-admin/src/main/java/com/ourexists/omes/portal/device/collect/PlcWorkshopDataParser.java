/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.device.collect;

import com.alibaba.fastjson2.JSONObject;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtime;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtimeCollect;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtimeConfig;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtimeManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class PlcWorkshopDataParser implements WorkshopDataParser {

    protected WorkshopRealtimeManager workshopRealtimeManager;

    public PlcWorkshopDataParser(WorkshopRealtimeManager workshopRealtimeManager) {
        this.workshopRealtimeManager = workshopRealtimeManager;
    }

    @Override
    public List<WorkshopRealtime> parse(String gwId, String sourceData) {
        List<WorkshopRealtime> realtimeList = workshopRealtimeManager.listByGwId(gwId);
        List<WorkshopRealtime> targets = new ArrayList<>();
        JSONObject jo = JSONObject.parseObject(sourceData);
        for (WorkshopRealtime realtime : realtimeList) {
            WorkshopRealtimeConfig config = realtime.getConfig();
            if (config == null) {
                continue;
            }
            targets.add(doParse(realtime, jo));
        }
        if (!CollectionUtils.isEmpty(targets)) {
            workshopRealtimeManager.realtimeHandle(targets);
        }
        return targets;
    }

    protected WorkshopRealtime doParse(WorkshopRealtime equipRealtime,
                                       JSONObject parsedObj) {
        WorkshopRealtime target = new WorkshopRealtime();
        BeanUtils.copyProperties(equipRealtime, target);
        target.setTime(new Date());
        if (!CollectionUtils.isEmpty(target.getAttrsRealtime())) {
            for (WorkshopRealtimeCollect attr : target.getAttrsRealtime()) {
                String val = parsedObj.getString(attr.getMap());
                if (val != null) {
                    attr.setValue(val);
                }
            }
        }
        return target;
    }
}
