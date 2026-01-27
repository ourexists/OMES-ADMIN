package com.ourexists.mesedge.device.core;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class EquipRealtimeConfig {

    private String runMap;

    private String alarmMap;

    private List<EquipAttrRealtime> attrs;

    private List<EquipAlarmRealtime> alarms;
}
