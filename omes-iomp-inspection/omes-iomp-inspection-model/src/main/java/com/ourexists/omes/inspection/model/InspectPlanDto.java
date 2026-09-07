/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import com.ourexists.omes.inspection.enums.InspectPlanCycleTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 巡检计划 DTO
 */
@Schema(description = "巡检计划")
@Getter
@Setter
@Accessors(chain = true)
public class InspectPlanDto extends BaseDto {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "计划名称")
    private String name;

    @Schema(description = "关联巡检模板ID")
    private String templateId;

    @Schema(description = "关联巡检模板名称（展示用，后端返回）")
    private String templateName;

    @Schema(description = "周期类型：1每日 2每周 3每月")
    private Integer cycleType;

    @Schema(description = "周期配置：每日为具体时间HH:mm；每周为星期几(1-7)+时间；每月为日期+时间")
    private String cycleConfig;

    @Schema(description = "场景编号，为空表示全部")
    private String workshopCode;

    @Schema(description = "关联设备ID列表，为空表示场景下全部设备")
    private List<String> equipIds;

    @Schema(description = "状态：0禁用 1启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Date createdTime;

    @Schema(description = "更新时间")
    private Date updatedTime;

    @Schema(description = "租户ID")
    private String tenantId;

    public String getCycleTypeDesc() {
        return cycleType != null ? InspectPlanCycleTypeEnum.valueOf(cycleType).getDesc() : "";
    }
}
