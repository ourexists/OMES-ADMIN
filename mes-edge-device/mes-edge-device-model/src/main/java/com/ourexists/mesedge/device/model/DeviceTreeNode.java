/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.model;

import com.ourexists.era.framework.core.utils.tree.TreeNode;
import com.ourexists.mesedge.device.enums.DeviceLocalizationEnum;
import com.ourexists.mesedge.device.enums.DeviceStatusEnum;
import com.ourexists.mesedge.device.enums.DeviceTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class DeviceTreeNode extends TreeNode<DeviceTreeNode> {

    private String id;

    private String name;

    private String selfCode;

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
