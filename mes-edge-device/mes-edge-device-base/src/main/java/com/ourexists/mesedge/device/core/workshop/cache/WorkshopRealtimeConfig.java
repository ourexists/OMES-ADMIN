package com.ourexists.mesedge.device.core.workshop.cache;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class WorkshopRealtimeConfig {

    private String collectType;

    private List<WorkshopRealtimeCollect> attrs;
}
