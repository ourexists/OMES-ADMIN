package com.ourexists.omes.device.core.equip.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class EquipAttrRealtime {

    private String name;

    private String map;

    private String unit;

    private Boolean needCollect = false;

    private String value;
}
