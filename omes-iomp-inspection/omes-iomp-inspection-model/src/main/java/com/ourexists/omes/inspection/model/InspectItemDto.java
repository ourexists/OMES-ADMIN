/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import com.ourexists.omes.inspection.enums.InspectItemTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 巡检项 DTO（模板下的单条：巡检项名称、类型、单位）
 */
@Schema(description = "巡检项")
@Getter
@Setter
@Accessors(chain = true)
public class InspectItemDto extends BaseDto {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "所属模板ID")
    private String templateId;

    @Schema(description = "模板名称（展示用，后端返回）")
    private String templateName;

    @Schema(description = "巡检项名称，如：运行状态、运行电流、出口压力")
    private String itemName;

    /** 类型：1选择 2数值 3是否 */
    @Schema(description = "类型：1选择 2数值 3是否")
    private Integer itemType;

    /** 单位：数值型填 A、MPa、m³/h、°C 等；选择型可填选项如 正常/不足、松动/正常；是否型可空 */
    @Schema(description = "单位或选项：数值填 A/MPa/m³/h/°C；选择可填 正常/不足、松动/正常；是否可空")
    private String unit;

    @Schema(description = "最小值（仅数值型有效）")
    private Double minValue;

    @Schema(description = "最大值（仅数值型有效）")
    private Double maxValue;

    @Schema(description = "是否必填：0否 1是")
    private Boolean requiredFlag;

    @Schema(description = "关联的巡检项ID（从巡检项载入时绑定，类型、单位不可改）")
    private String referenceItemId;

    @Schema(description = "权重（模板内该项的权重）")
    private Integer weight;

    @Schema(description = "创建时间")
    private Date createdTime;

    @Schema(description = "更新时间")
    private Date updatedTime;

    @Schema(description = "租户ID")
    private String tenantId;

    public String getItemTypeDesc() {
        return itemType != null ? InspectItemTypeEnum.valueOf(itemType).getDesc() : "";
    }
}
