package com.ourexists.mesedge.device.core.equip.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class EquipRealtimeConfig {

    private String collectType;

    private String runMap;

    private String alarmMap;

    private List<EquipAttrRealtime> attrs;

    private List<EquipAlarmRealtime> alarms;
}
