package com.ourexists.omes.portal.device.collect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ourexists.omes.device.core.equip.cache.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class TcMqttEquipDataParser implements EquipDataParser {

    public static final String CODE_KEY = "code";

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Autowired
    private AlarmRuleProcessor alarmRuleProcessor;

    public List<EquipRealtime> parse(String gwId, String sourceData) {
        List<EquipRealtime> targets = new ArrayList<>();
        JSONObject jsonObject = JSON.parseObject(sourceData);
        String sn = jsonObject.getString("SN");
        JSONArray devArray = jsonObject.getJSONArray("dev");
        JSONArray ywArray = jsonObject.getJSONArray("ai");
        String timeStr = jsonObject.getString("time");
        Date time = Date.from(
                OffsetDateTime.parse(timeStr)
                        .toInstant()
        );

        if (devArray != null) {
            for (JSONObject object : devArray.toArray(JSONObject.class)) {
                String devSn = object.getString(CODE_KEY);
                String ssn = sn + "dev" + devSn;
                EquipRealtime equipRealtime = equipRealtimeManager.get(ssn);
                if (equipRealtime == null || equipRealtime.getEquipRealtimeConfig() == null ||
                        !gwId.equals(equipRealtime.getEquipRealtimeConfig().getGwId())) {
                    continue;
                }
                targets.add(doParse(equipRealtime, object, time));
            }

        }
        if (ywArray != null) {
            for (JSONObject object : ywArray.toArray(JSONObject.class)) {
                String ywSn = object.getString(CODE_KEY);
                String ssn = sn + "yw" + ywSn;
                EquipRealtime equipRealtime = equipRealtimeManager.get(ssn);
                if (equipRealtime == null || equipRealtime.getEquipRealtimeConfig() == null ||
                        !gwId.equals(equipRealtime.getEquipRealtimeConfig().getGwId())) {
                    continue;
                }
                targets.add(doParse(equipRealtime, object, time));
            }
        }
        return targets;
    }

    public EquipRealtime doParse(EquipRealtime equipRealtime, JSONObject parsedObj, Date time) {
        EquipRealtime target = new EquipRealtime();
        BeanUtils.copyProperties(equipRealtime, target);
        target.setTime(time);
        target.setOnlineState(1);
        target.setRunState(1);

        Integer runVal = parsedObj.getInteger(equipRealtime.getEquipRealtimeConfig().getRunMap());
        if (runVal == null || runVal == 1) {
            target.setRunState(1);
        } else {
            target.setRunState(0);
        }

        if (!CollectionUtils.isEmpty(target.getEquipAttrRealtimes())) {
            for (EquipAttrRealtime attr : target.getEquipAttrRealtimes()) {
                attr.setValue(parsedObj.getString(attr.getMap()));
            }
        }

        if (!CollectionUtils.isEmpty(target.getEquipControlRealtimes())) {
            for (EquipControlRealtime ctrl : target.getEquipControlRealtimes()) {
                String val = parsedObj.getString(ctrl.getMap());
                if (val != null) {
                    ctrl.setValue(val);
                }
            }
        }

        int alarm = 0;
        if (!CollectionUtils.isEmpty(equipRealtime.getEquipRealtimeConfig().getAlarms())) {
            List<String> alarms = new ArrayList<>();
            Integer level = -1;
            for (EquipAlarmRealtime alarmRealtime : equipRealtime.getEquipRealtimeConfig().getAlarms()) {
                Object raw = parsedObj.get(alarmRealtime.getMap());
                if (alarmRuleProcessor.match(raw, alarmRealtime)) {
                    alarm = 1;
                    level = alarmRealtime.getLevel() > level ? alarmRealtime.getLevel() : level;
                    alarms.add(alarmRealtime.getText());
                }
            }
            target.setAlarmTexts(alarms);
            target.setAlarmLevel(level);
        }
        target.setAlarmState(alarm);
        return target;
    }
}
