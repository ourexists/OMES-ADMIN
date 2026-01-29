package com.ourexists.mesedge.device.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;


@Getter
@Setter
@Accessors(chain = true)
public class WorkshopConfigCollectDetail {

    private String collectType;

    private List<WorkshopConfigCollectAttr> attrs;

}
