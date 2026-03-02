package com.ourexists.omes.portal.device.model;

import com.ourexists.era.framework.core.utils.tree.TreeNode;
import com.ourexists.omes.device.enums.DeviceLocalizationEnum;
import com.ourexists.omes.device.enums.DeviceStatusEnum;
import com.ourexists.omes.device.enums.DeviceTypeEnum;
import com.ourexists.omes.mat.model.MaterialDto;
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
