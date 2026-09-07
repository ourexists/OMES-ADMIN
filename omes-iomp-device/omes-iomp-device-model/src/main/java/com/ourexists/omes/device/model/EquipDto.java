/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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

    /** 所属产品编号（关联产品 code） */
    private String type;

    /** 所属产品名称（由后端按 type 查产品回填） */
    private String typeDesc;

    /** 所属产品型号 ID */
    private String modelId;

    /** 所属产品型号名称（由后端按 modelId 回填） */
    private String modelName;

    /** 所属产品图片地址（由后端按 type 查产品回填，用于设备列表/详情展示） */
    private String productImage;

    private String workshopCode;

    /** 启用日期，用于健康分使用年限计算 */
    private Date enableDate;

    /** 关联的健康规则模板ID，为空时使用默认模板 */
    private String healthTemplateId;

    /** 本能力方案下加工原料及容量（设备能力绑定回填，非设备档案字段） */
    private List<EquipMatDto> processMaterials;

    /** 运行：-1 未知，0 停止，1 运行 */
    private Integer runState = -1;

    /** 报警：-1 未知，0 正常，1 报警 */
    private Integer alarmState = -1;

    private Integer onlineState = 0;

    private Date onlineChangeTime;

    private Date runChangeTime;

    private Date alarmChangeTime;

    private String tenantId;

    private WorkshopTreeNode workshop;

    private GwBindingDto config;

    private List<EquipAttr> attrs;

    private List<EquipControl> controls;

    private List<String> alarmTexts;

}
