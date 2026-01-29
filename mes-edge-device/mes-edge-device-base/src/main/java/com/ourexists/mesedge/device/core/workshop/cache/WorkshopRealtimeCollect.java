package com.ourexists.mesedge.device.core.workshop.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class WorkshopRealtimeCollect {

    private String name;

    private String map;

    private Boolean needCollect = false;
    
    private String value;
}
