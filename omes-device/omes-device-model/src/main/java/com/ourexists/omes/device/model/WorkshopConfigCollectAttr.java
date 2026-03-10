package com.ourexists.omes.device.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Getter
@Setter
@Accessors(chain = true)
public class WorkshopConfigCollectAttr {

    private String name;

    private String map;

    /** 关联的网关ID（来自设备绑定） */
    private String gwId;

    private String value;

    private String unit;

    private Boolean needCollect = false;

}
