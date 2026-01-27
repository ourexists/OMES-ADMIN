package com.ourexists.mesedge.device.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class EquipAlarm {

    private String map;

    private String val;

    private String text;
}
