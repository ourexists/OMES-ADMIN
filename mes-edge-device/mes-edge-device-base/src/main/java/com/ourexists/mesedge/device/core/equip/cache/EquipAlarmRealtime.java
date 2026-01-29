package com.ourexists.mesedge.device.core.equip.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class EquipAlarmRealtime {

    private String map;

    private String text;

    private String val;
}
