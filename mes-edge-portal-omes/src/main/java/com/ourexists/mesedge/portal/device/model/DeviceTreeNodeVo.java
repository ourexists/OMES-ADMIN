package com.ourexists.mesedge.portal.device.model;

import com.ourexists.era.framework.core.utils.tree.TreeNode;
import com.ourexists.mesedge.device.enums.DeviceLocalizationEnum;
import com.ourexists.mesedge.device.enums.DeviceStatusEnum;
import com.ourexists.mesedge.device.enums.DeviceTypeEnum;
import com.ourexists.mesedge.mat.model.MaterialDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DeviceTreeNodeVo extends TreeNode<DeviceTreeNodeVo> {

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

    private MaterialDto materialDto;
}
