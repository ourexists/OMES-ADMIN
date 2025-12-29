package com.ourexists.mesedge.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class EquipAttrRealtime {

    private String attrId;

    private String attrName;

    private String attrValue;
}
