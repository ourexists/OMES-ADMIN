package com.ourexists.mesedge.device.core;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class EquipRealtime {

    private String id;

    private String name;

    private String selfCode;

    private Integer type;

    private String runMap;

    private String alarmMap;

    private String workshopCode;

    private BigDecimal lng;

    private BigDecimal lat;

    private String address;

    private Boolean online = false;

    private Integer runState;

    private Integer alarmState;

    private List<EquipAttrRealtime> equipAttrRealtimeList;

    private String tenantId;
}
