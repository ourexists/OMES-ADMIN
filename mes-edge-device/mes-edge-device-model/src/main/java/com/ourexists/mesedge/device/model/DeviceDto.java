/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import com.ourexists.mesedge.device.enums.DeviceLocalizationEnum;
import com.ourexists.mesedge.device.enums.DeviceStatusEnum;
import com.ourexists.mesedge.device.enums.DeviceTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 配方分类
 */
@Getter
@Setter
@Accessors(chain = true)
public class DeviceDto extends BaseDto {

    private String id;

    private String selfCode;

    //级联编号
    private String code;

    private String pcode;


    private String name;

    private String dgId;

    private Integer type;

    private String typeDesc;

    private Integer localization;

    private String localizationDesc;

    private BigDecimal maxCapacity;

    private BigDecimal availableCapacity;

    private Integer status;

    private String statusDesc;

    private String matCode;

    public String getTypeDesc() {
        return DeviceTypeEnum.valueof(this.type).getDesc();
    }


    public String getLocalizationDesc() {
        return DeviceLocalizationEnum.valueof(this.localization).getDesc();
    }

    public String getStatusDesc() {
        return DeviceStatusEnum.valueof(this.status).getDesc();
    }

}
