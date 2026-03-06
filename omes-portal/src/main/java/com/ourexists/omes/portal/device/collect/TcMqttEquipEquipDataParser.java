package com.ourexists.omes.portal.device.collect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ourexists.omes.device.core.equip.cache.EquipAlarmRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipAttrRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipRealtimeConfig;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class TcMqttEquipEquipDataParser implements EquipDataParser {


    @Override
    public EquipRealtime doParse(EquipRealtime equipRealtime, Object realtimeData) {
        String sourceJsonData = (String) realtimeData;


        JSONObject jsonObject = JSON.parseObject(sourceJsonData);
        String sn = jsonObject.getString("SN");
        JSONArray devArray = jsonObject.getJSONArray("dev");
        JSONArray ywArray = jsonObject.getJSONArray("ai");


        EquipRealtimeConfig equipRealtimeConfig = equipRealtime.getEquipRealtimeConfig();
        if (devArray != null) {
            JSONObject[] devArrays = devArray.toArray(JSONObject.class);
            for (JSONObject object : devArrays) {
                String devSn = object.getString("code");
                String ssn = sn + "dev" + devSn;
                if (equipRealtime.getSelfCode().equals(ssn)) {
                    EquipRealtime target = new EquipRealtime();
                    BeanUtils.copyProperties(equipRealtime, target);


                    target.setTime(new Date());
                    target.online();
                    Integer alarmVal = object.getInteger(equipRealtimeConfig.getAlarmMap());
                    if (alarmVal != null && alarmVal == 1) {
                        target.alarm();
                    } else {
                        target.resetAlarm();
                    }
                    Integer runVal = object.getInteger(equipRealtimeConfig.getRunMap());
                    if (runVal != null && runVal == 1) {
                        target.run();
                    } else {
                        target.stop();
                    }
                    if (!CollectionUtils.isEmpty(target.getEquipAttrRealtimes())) {
                        for (EquipAttrRealtime attr : target.getEquipAttrRealtimes()) {
                            attr.setValue(object.getString(attr.getMap()));
                        }
                    }
                    return target;
                }
            }
        }

        if (ywArray != null) {
            JSONObject[] ywArrays = ywArray.toArray(JSONObject.class);
            for (JSONObject o : ywArrays) {
                String ywSn = o.getString("code");
                String ssnn = sn + ywSn;
                if (equipRealtime.getSelfCode().equals(ssnn)) {
                    EquipRealtime target = new EquipRealtime();
                    BeanUtils.copyProperties(equipRealtime, target);

                    target.setTime(new Date());
                    target.online();
                    target.run();

                    int alarm = 0;
                    if (!CollectionUtils.isEmpty(equipRealtimeConfig.getAlarms())) {
                        List<String> alarms = new ArrayList<>();
                        for (EquipAlarmRealtime alarmRealtime : equipRealtimeConfig.getAlarms()) {
                            if (matchAlarm(o, alarmRealtime)) {
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
                    if (!CollectionUtils.isEmpty(target.getEquipAttrRealtimes())) {
                        for (EquipAttrRealtime attr : target.getEquipAttrRealtimes()) {
                            attr.setValue(o.getString(attr.getMap()));
                        }
                    }
                    return target;
                }
            }
        }
        return null;
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

    @Override
    public String name() {
        return "TC_Mqtt";
    }
}
