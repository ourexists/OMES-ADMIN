/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.device.collect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ourexists.omes.device.core.equip.cache.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * WinCC 数据解析器。
 * WinCC tagManagement/Values 返回 variableName -> value 映射，与 S7 解析逻辑一致（keys 为 runMap/attrs[].map 等）。
 */
@Component
public class WinccEquipDataParser implements EquipDataParser {

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Autowired
    private AlarmRuleProcessor alarmRuleProcessor;

    @Override
    public List<EquipRealtime> parse(String gwId, String sourceData) {
        Map<String, EquipRealtime> realtimeMap = equipRealtimeManager.getAll();
        List<EquipRealtime> targets = new ArrayList<>();
        Map<String, Object> map = parseWinccResponse(sourceData);
        for (EquipRealtime equipRealtime : realtimeMap.values()) {
            EquipRealtimeConfig config = equipRealtime.getEquipRealtimeConfig();
            if (config == null || !gwId.equals(config.getGwId())) {
                continue;
            }
            targets.add(doParse(equipRealtime, map));
        }
        if (!CollectionUtils.isEmpty(targets)) {
            equipRealtimeManager.realtimeHandle(targets);
        }
        return targets;
    }

    private Map<String, Object> parseWinccResponse(String msg) {
        JSONArray arr = JSON.parseArray(msg);
        if (arr == null || arr.isEmpty()) return null;
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < arr.size(); i++) {
            JSONObject jo = arr.getJSONObject(i);
            if (StringUtils.hasText(jo.getString("error"))) continue;
            String field = jo.getString("variableName");
            Integer dataType = jo.getInteger("dataType");
            if (dataType == null) continue;
            Object value = parseWinccValue(jo);
            if (value != null) result.put(field, value);
        }
        return result.isEmpty() ? null : result;
    }

    private static Object parseWinccValue(JSONObject jo) {
        Integer dataType = jo.getInteger("dataType");
        if (dataType == null) return null;
        if (dataType == 1) return jo.getBooleanValue("value") ? 1 : 0;
        if (dataType == 4) return jo.getFloatValue("value");
        return null;
    }

    protected EquipRealtime doParse(EquipRealtime equipRealtime, Map<String, Object> parsedObj) {
        EquipRealtime target = new EquipRealtime();
        BeanUtils.copyProperties(equipRealtime, target);
        target.setTime(new Date());
        target.online();

        Integer runVal = (Integer) parsedObj.get(equipRealtime.getEquipRealtimeConfig().getRunMap());
        if (runVal != null && runVal == 1) {
            target.run();
        } else {
            target.stop();
        }

        if (!CollectionUtils.isEmpty(target.getEquipAttrRealtimes())) {
            for (EquipAttrRealtime attr : target.getEquipAttrRealtimes()) {
                attr.setValue((String) parsedObj.get(attr.getMap()));
            }
        }

        int alarm = 0;
        if (!CollectionUtils.isEmpty(equipRealtime.getEquipRealtimeConfig().getAlarms())) {
            var alarms = new ArrayList<String>();
            for (EquipAlarmRealtime ar : equipRealtime.getEquipRealtimeConfig().getAlarms()) {
                Object raw = parsedObj.get(ar.getMap());
                if (alarmRuleProcessor.match(raw, ar)) {
                    alarm = 1;
                    alarms.add(ar.getText());
                }
            }
            target.setAlarmTexts(alarms);
        }
        if (alarm == 1) {
            target.alarm();
        } else {
            target.resetAlarm();
        }
        return target;
    }

}
