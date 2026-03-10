/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.device.collect;

import com.alibaba.fastjson2.JSONObject;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtime;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtimeCollect;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtimeConfig;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtimeManager;
import com.ourexists.omes.portal.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 场景数据解析器：在设备数据解析完成后，从同一份网关 payload 中解析场景采集配置关联的映射并更新场景实时缓存。
 * 与设备解析共用同一 payload（如 S7/WinCC/Modbus/OPC UA 返回的 JSON），按场景配置中该网关的 attr.map 取数。
 */
@Slf4j
@Component
public class JSONWorkshopDataParser implements WorkshopDataParser {

    protected WorkshopRealtimeManager workshopRealtimeManager;

    public JSONWorkshopDataParser(WorkshopRealtimeManager workshopRealtimeManager) {
        this.workshopRealtimeManager = workshopRealtimeManager;
    }

    @Override
    public List<WorkshopRealtime> parse(String gwId, String sourceData) {
        Map<String, WorkshopRealtime> realtimeMap = workshopRealtimeManager.getAll();
        List<WorkshopRealtime> targets = new ArrayList<>();
        JSONObject jo = JSONObject.parseObject(sourceData);
        for (WorkshopRealtime realtime : realtimeMap.values()) {
            WorkshopRealtimeConfig config = realtime.getConfig();
            if (config == null) {
                continue;
            }
            boolean isMatch = false;
            for (WorkshopRealtimeCollect attr : config.getAttrs()) {
                if (attr.getGwId().equals(gwId)) {
                    isMatch = true;
                    break;
                }
            }
            //采集方式不匹配
            if (!isMatch) {
                continue;
            }
            targets.add(doParse(realtime, jo));
        }
        if (!CollectionUtils.isEmpty(targets)) {
            workshopRealtimeManager.realtimeHandle(targets);
        }
        return targets;
    }

    private WorkshopRealtime doParse(WorkshopRealtime realtime, JSONObject parsedObj) {
        WorkshopRealtime target = new WorkshopRealtime();
        BeanUtils.copyProperties(realtime, target);
        target.setTime(new Date());

        if (!CollectionUtils.isEmpty(target.getAttrsRealtime())) {
            for (WorkshopRealtimeCollect attr : target.getAttrsRealtime()) {
                attr.setValue(JSONUtils.getStringByPath(parsedObj, attr.getMap()));
            }
        }
        return target;
    }
}
