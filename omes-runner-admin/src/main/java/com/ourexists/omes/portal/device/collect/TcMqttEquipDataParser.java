package com.ourexists.omes.portal.device.collect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ourexists.omes.device.core.equip.cache.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class TcMqttEquipDataParser implements EquipDataParser {

    public static final String CODE_KEY = "code";

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Autowired
    private AlarmRuleProcessor alarmRuleProcessor;

    public List<EquipRealtime> parse(String gwId, String sourceData) {
        List<EquipRealtime> targets = new ArrayList<>();
        final JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(sourceData);
        } catch (Exception e) {
            log.warn("TC MQTT equip JSON parse failed gwId={} payloadChars={}", gwId, sourceData.length(), e);
            return targets;
        }
        if (jsonObject == null) {
            log.warn("TC MQTT equip JSON root null gwId={} payloadChars={}", gwId, sourceData.length());
            return targets;
        }
        String sn = jsonObject.getString("SN");
        JSONArray devArray = jsonObject.getJSONArray("dev");
        JSONArray ywArray = jsonObject.getJSONArray("ai");
        String timeStr = jsonObject.getString("time");
        if (!StringUtils.hasText(timeStr)) {
            log.warn("TC MQTT equip missing time gwId={} sn={}", gwId, sn);
            return Collections.emptyList();
        }
        final Date time;
        try {
            time = Date.from(OffsetDateTime.parse(timeStr.trim()).toInstant());
        } catch (Exception e) {
            log.warn("TC MQTT equip time parse failed gwId={} sn={} timeStr={}", gwId, sn, timeStr, e);
            return Collections.emptyList();
        }

        int devLen = devArray == null ? 0 : devArray.size();
        int ywLen = ywArray == null ? 0 : ywArray.size();

        if (devArray != null) {
            for (JSONObject object : devArray.toArray(JSONObject.class)) {
                String devSn = object.getString(CODE_KEY);
                String ssn = sn + "dev" + devSn;
                EquipRealtime equipRealtime = equipRealtimeManager.get(ssn);
                if (equipRealtime == null || equipRealtime.getEquipRealtimeConfig() == null
                        || !gwId.equals(equipRealtime.getEquipRealtimeConfig().getGwId())) {
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
                if (equipRealtime == null || equipRealtime.getEquipRealtimeConfig() == null
                        || !gwId.equals(equipRealtime.getEquipRealtimeConfig().getGwId())) {
                    continue;
                }
                targets.add(doParse(equipRealtime, object, time));
            }
        }
        if (targets.isEmpty() && (devLen > 0 || ywLen > 0)) {
            log.warn(
                    "TC MQTT equip parse produced 0 rows (cache miss or gw mismatch) gwId={} sn={} devCount={} ywCount={}",
                    gwId,
                    sn,
                    devLen,
                    ywLen);
        }
        return targets;
    }

    public EquipRealtime doParse(EquipRealtime equipRealtime, JSONObject parsedObj, Date time) {
        EquipRealtime target = new EquipRealtime();
        BeanUtils.copyProperties(equipRealtime, target);
        target.setTime(time);
        target.setOnlineState(1);
        target.setRunState(1);

        if (equipRealtime.getOnlineState() == 0) {
            target.setOnlineChangeTime(new Date());
            target.setOnlineChange(true);
        }

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
