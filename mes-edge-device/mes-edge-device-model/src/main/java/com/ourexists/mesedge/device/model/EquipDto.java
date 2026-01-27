/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import com.ourexists.mesedge.device.enums.EquipTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 设备
 */
@Getter
@Setter
@Accessors(chain = true)
public class EquipDto extends BaseDto {

    private String id;

    private String name;

    private String selfCode;

    private Integer type;

    private String typeDesc;

    private String workshopCode;

    private BigDecimal lng;

    private BigDecimal lat;

    private String address;

    private Integer runState = 0;

    private Integer alarmState = 0;

    private Integer onlineState = 0;

    private Date onlineChangeTime;

    private Date runChangeTime;

    private Date alarmChangeTime;

    private String tenantId;

    private WorkshopTreeNode workshop;

    private EquipConfigDto config;

    private List<EquipAttr> attrs;

    private List<String> alarmTexts;

    public String getTypeDesc() {
        return EquipTypeEnum.valueof(this.type).getDesc();
    }
}
