package com.ourexists.omes.device.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class EquipConfigDetail {

    private String gwId;

    private String collectType;

    private String deviceIdMap;

    private String runMap;

    private String alarmMap;

    private List<EquipAttr> attrs;

    private List<EquipAlarm> alarms;
}
