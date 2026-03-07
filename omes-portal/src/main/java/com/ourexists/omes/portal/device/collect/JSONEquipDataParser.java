package com.ourexists.omes.portal.device.collect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.omes.device.core.equip.cache.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JSONEquipDataParser implements EquipDataParser {

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Override
    public List<EquipRealtime> parse(String gwId, String sourceData) {
        JSONArray devArray = new JSONArray();
        if (JSON.isValidArray(sourceData)) {
            devArray = JSON.parseArray(sourceData);
        } else {
            devArray.add(JSON.parseObject(sourceData));
        }
        if (CollectionUtil.isBlank(devArray)) {
            return null;
        }
        JSONObject[] devArrays = devArray.toArray(JSONObject.class);
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

            for (JSONObject object : devArrays) {
                String ssn = object.getString(equipRealtimeConfig.getDeviceIdMap());
                if (!equipRealtime.getSelfCode().equals(ssn)) {
                    continue;
                }
                targets.add(doParse(equipRealtime, object));
            }
        }
        return targets;
    }

    protected EquipRealtime doParse(EquipRealtime equipRealtime, JSONObject parsedObj) {
        EquipRealtime target = new EquipRealtime();
        BeanUtils.copyProperties(equipRealtime, target);
        target.setTime(new Date());
        target.online();

        Integer runVal = parsedObj.getInteger(equipRealtime.getEquipRealtimeConfig().getRunMap());
        if (runVal != null && runVal == 1) {
            target.run();
        } else {
            target.stop();
        }
        if (!CollectionUtils.isEmpty(target.getEquipAttrRealtimes())) {
            for (EquipAttrRealtime attr : target.getEquipAttrRealtimes()) {
                attr.setValue(parsedObj.getString(attr.getMap()));
            }
        }
        int alarm = 0;
        if (!CollectionUtils.isEmpty(equipRealtime.getEquipRealtimeConfig().getAlarms())) {
            List<String> alarms = new ArrayList<>();
            for (EquipAlarmRealtime alarmRealtime : equipRealtime.getEquipRealtimeConfig().getAlarms()) {
                if (matchAlarm(parsedObj, alarmRealtime)) {
                    alarm = 1;
                    alarms.add(alarmRealtime.getText());
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

    /** 判断单条报警条件是否命中。类型: 0=相等, 1=大于, 2=大于等于, 3=小于, 4=小于等于, 5=范围(超出[min,max]报警) */
    private boolean matchAlarm(JSONObject o, EquipAlarmRealtime alarmRealtime) {
        Integer type = alarmRealtime.getType();
        if (type == null) {
            type = 0;
        }
        Object raw = o.get(alarmRealtime.getMap());
        String strVal = raw != null ? raw.toString() : null;
        if (strVal == null) {
            return false;
        }
        if (type == 0) {
            return strVal.equals(alarmRealtime.getVal());
        }
        Double numVal = parseDouble(strVal);
        if (numVal == null) {
            return false;
        }
        switch (type) {
            case 1: // 大于
                return numVal > parseDouble(alarmRealtime.getVal(), 0);
            case 2: // 大于等于
                return numVal >= parseDouble(alarmRealtime.getVal(), 0);
            case 3: // 小于
                return numVal < parseDouble(alarmRealtime.getVal(), 0);
            case 4: // 小于等于
                return numVal <= parseDouble(alarmRealtime.getVal(), 0);
            case 5: { // 范围：超出 [min,max] 报警
                Double min = parseDouble(alarmRealtime.getMin(), Double.NEGATIVE_INFINITY);
                Double max = parseDouble(alarmRealtime.getMax(), Double.POSITIVE_INFINITY);
                return numVal < min || numVal > max;
            }
            default:
                return strVal.equals(alarmRealtime.getVal());
        }
    }

    private static Double parseDouble(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static double parseDouble(String s, double defaultValue) {
        Double d = parseDouble(s);
        return d != null ? d : defaultValue;
    }
}
