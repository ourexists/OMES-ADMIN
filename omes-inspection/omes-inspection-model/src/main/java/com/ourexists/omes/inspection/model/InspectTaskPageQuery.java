/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.model;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 巡检任务分页查询
 */
@Schema
@Getter
@Setter
@Accessors(chain = true)
public class InspectTaskPageQuery extends PageQuery {

    @Schema(description = "计划ID")
    private String planId;

    @Schema(description = "计划名称")
    private String planName;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "执行人ID")
    private String executorId;

    @Schema(description = "计划执行开始时间")
    private Date scheduledTimeStart;

    @Schema(description = "计划执行结束时间")
    private Date scheduledTimeEnd;

    @Schema(description = "场景编号列表")
    private List<String> workshopCodes;

    @Schema(description = "是否只查未指派：true=仅未指派")
    private Boolean unassigned;
}
