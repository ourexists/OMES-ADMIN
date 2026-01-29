package com.ourexists.mesedge.device.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Getter
@Setter
@Accessors(chain = true)
public class WorkshopConfigCollectAttr {

    private String name;

    private String map;

    private String value;

    private Boolean needCollect = false;

}
