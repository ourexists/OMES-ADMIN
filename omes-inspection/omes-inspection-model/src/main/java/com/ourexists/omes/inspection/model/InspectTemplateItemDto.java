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
 * 模板-巡检项关联 DTO（某模板下某产品块的一条巡检项）
 */
@Schema(description = "模板巡检项")
@Getter
@Setter
@Accessors(chain = true)
public class InspectTemplateItemDto extends BaseDto {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "所属模板ID")
    private String templateId;

    @Schema(description = "所属产品编号（模板中的产品块）")
    private String productCode;

    @Schema(description = "引用的巡检项ID（从巡检项池载入时绑定）")
    private String referenceItemId;

    @Schema(description = "巡检项名称（从巡检项主表映射，仅返回）")
    private String itemName;

    @Schema(description = "类型：1选择 2数值 3是否（从巡检项主表映射，仅返回）")
    private Integer itemType;

    @Schema(description = "单位或选项（从巡检项主表映射，仅返回）")
    private String unit;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "权重（模板内该项的权重，0-100 整数或前端 0~1 时需换算）")
    private Integer weight;

    @Schema(description = "百分比权重（该项权重/同产品块权重总值，0~1 小数如 0.3，用于后续计算）")
    private java.math.BigDecimal weightRate;

    @Schema(description = "规则配置 JSON，如数值区间/是否/选项与分值")
    private String ruleConfig;

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

