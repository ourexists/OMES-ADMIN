package com.ourexists.omes.portal.device.collect;

import com.alibaba.fastjson2.JSONObject;
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
public class S7EquipDataParser implements EquipDataParser {

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Autowired
    private AlarmRuleProcessor alarmRuleProcessor;

    @Override
    public List<EquipRealtime> parse(String gwId, String sourceData) {
        Map<String, EquipRealtime> realtimeMap = equipRealtimeManager.getAll();
        List<EquipRealtime> targets = new ArrayList<>();
        JSONObject jo = JSONObject.parseObject(sourceData);
        for (EquipRealtime equipRealtime : realtimeMap.values()) {
            EquipRealtimeConfig equipRealtimeConfig = equipRealtime.getEquipRealtimeConfig();
            if (equipRealtimeConfig == null) {
                continue;
            }
            //采集方式不匹配
            if (!gwId.equals(equipRealtimeConfig.getGwId())) {
                continue;
            }
            targets.add(doParse(equipRealtime, jo));
        }
        if (!CollectionUtils.isEmpty(targets)) {
            equipRealtimeManager.realtimeHandle(targets);
        }
        return targets;
    }

    protected EquipRealtime doParse(EquipRealtime equipRealtime, JSONObject parsedObj) {
        EquipRealtime target = new EquipRealtime();
        BeanUtils.copyProperties(equipRealtime, target);
        target.setTime(new Date());
        target.online();

        if (StringUtils.hasText(equipRealtime.getEquipRealtimeConfig().getRunMap())) {
            Integer runVal = parsedObj.getInteger(equipRealtime.getEquipRealtimeConfig().getRunMap());
            if (runVal != null && runVal == 1) {
                target.run();
            } else {
                target.stop();
            }
        } else {
            target.run();
        }

        if (!CollectionUtils.isEmpty(target.getEquipAttrRealtimes())) {
            for (EquipAttrRealtime attr : target.getEquipAttrRealtimes()) {
                String val = parsedObj.getString(attr.getMap());
                if (val != null) {
                    attr.setValue(val);
                }
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
            for (EquipAlarmRealtime alarmRealtime : equipRealtime.getEquipRealtimeConfig().getAlarms()) {
                Object raw = parsedObj.get(alarmRealtime.getMap());
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

}
