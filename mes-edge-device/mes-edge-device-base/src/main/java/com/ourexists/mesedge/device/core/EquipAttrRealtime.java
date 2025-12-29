package com.ourexists.mesedge.device.core;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class EquipAttrRealtime {

    private String attrId;

    private String attrName;

    private String attrValue;
}
