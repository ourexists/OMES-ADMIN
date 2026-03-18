/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import com.ourexists.omes.inspection.enums.InspectTaskStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 巡检任务 DTO
 */
@Schema(description = "巡检任务")
@Getter
@Setter
@Accessors(chain = true)
public class InspectTaskDto extends BaseDto {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "巡检计划ID")
    private String planId;

    @Schema(description = "关联巡检模板ID（来自计划）")
    private String templateId;

    @Schema(description = "关联巡检模板名称（展示用，后端返回）")
    private String templateName;

    @Schema(description = "计划名称")
    private String planName;

    @Schema(description = "计划执行时间")
    private Date scheduledTime;

    @Schema(description = "关联单个场景编号，为空表示按计划范围")
    private String workshopCode;

    @Schema(description = "关联场景名称（展示用，由后端根据 workshopCode 解析返回）")
    private String workshopName;

    @Schema(description = "状态：0待执行 1执行中 2已完成 3已逾期")
    private Integer status;

    @Schema(description = "指派的巡检人员ID")
    private String executorPersonId;

    @Schema(description = "执行人账户ID（来自巡检人员关联账户）")
    private String executorId;

    @Schema(description = "执行人姓名")
    private String executorName;

    @Schema(description = "实际开始时间")
    private Date actualStartTime;

    @Schema(description = "实际完成时间")
    private Date actualEndTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "租户ID")
    private String tenantId;

    public String getStatusDesc() {
        return status != null ? InspectTaskStatusEnum.valueOf(status).getDesc() : "";
    }
}
