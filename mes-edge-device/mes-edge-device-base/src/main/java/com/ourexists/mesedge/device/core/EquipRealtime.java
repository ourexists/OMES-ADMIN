package com.ourexists.mesedge.device.core;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class EquipRealtime {

    private String id;

    private String name;

    private String selfCode;

    private Integer onlineState = 0;

    private Integer runState = 0;

    private Integer alarmState = 0;

    private List<EquipAttrRealtime> equipAttrRealtimeList;

    private String tenantId;

    private String workshopCode;
}
