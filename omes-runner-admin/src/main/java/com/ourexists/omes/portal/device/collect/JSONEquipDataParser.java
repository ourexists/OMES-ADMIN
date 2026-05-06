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
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class JSONEquipDataParser implements EquipDataParser {

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Autowired
    private AlarmRuleProcessor alarmRuleProcessor;

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
                String ssn = getStringByPath(object, equipRealtimeConfig.getDeviceIdMap());
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

        Integer runVal = getIntegerByPath(parsedObj, equipRealtime.getEquipRealtimeConfig().getRunMap());
        if (runVal != null && runVal == 1) {
            target.run();
        } else {
            target.stop();
        }
        if (!CollectionUtils.isEmpty(target.getEquipAttrRealtimes())) {
            for (EquipAttrRealtime attr : target.getEquipAttrRealtimes()) {
                attr.setValue(getStringByPath(parsedObj, attr.getMap()));
            }
        }
        if (!CollectionUtils.isEmpty(target.getEquipControlRealtimes())) {
            for (EquipControlRealtime ctrl : target.getEquipControlRealtimes()) {
                String val = getStringByPath(parsedObj, ctrl.getMap());
                if (val != null) {
                    ctrl.setValue(val);
                }
            }
        }
        int alarm = 0;
        if (!CollectionUtils.isEmpty(equipRealtime.getEquipRealtimeConfig().getAlarms())) {
            List<String> alarms = new ArrayList<>();
            for (EquipAlarmRealtime alarmRealtime : equipRealtime.getEquipRealtimeConfig().getAlarms()) {
                Object raw = getByPath(parsedObj, alarmRealtime.getMap());
                if (alarmRuleProcessor.match(raw, alarmRealtime)) {
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

    /** 按路径获取 JSON 值，支持 data.status、data.items[0].value 等嵌套模式 */
    private static Object getByPath(Object root, String path) {
        if (root == null || !StringUtils.hasText(path)) return null;
        String trimmed = path.trim();
        if (!trimmed.contains(".") && !trimmed.contains("[")) return getDirect(root, trimmed);
        String[] parts = trimmed.split("\\.", -1);
        Object current = root;
        for (String part : parts) {
            if (current == null) return null;
            part = part.trim();
            if (part.isEmpty()) continue;
            int bracket = part.indexOf('[');
            if (bracket >= 0) {
                String key = bracket == 0 ? "" : part.substring(0, bracket);
                if (key.length() > 0) current = getDirect(current, key);
                int end = part.indexOf(']', bracket);
                while (bracket >= 0 && end >= 0) {
                    try {
                        int idx = Integer.parseInt(part.substring(bracket + 1, end).trim());
                        if (current instanceof JSONArray) current = ((JSONArray) current).get(idx);
                        else return null;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                    bracket = part.indexOf('[', end + 1);
                    end = bracket >= 0 ? part.indexOf(']', bracket) : -1;
                }
            } else {
                current = getDirect(current, part);
            }
        }
        return current;
    }

    private static Object getDirect(Object obj, String key) {
        if (obj == null || key == null) return null;
        if (obj instanceof JSONObject) return ((JSONObject) obj).get(key);
        if (obj instanceof Map) return ((Map<?, ?>) obj).get(key);
        return null;
    }

    private static String getStringByPath(Object root, String path) {
        Object v = getByPath(root, path);
        return v != null ? v.toString() : null;
    }

    private static Integer getIntegerByPath(Object root, String path) {
        Object v = getByPath(root, path);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return Integer.parseInt(v.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
