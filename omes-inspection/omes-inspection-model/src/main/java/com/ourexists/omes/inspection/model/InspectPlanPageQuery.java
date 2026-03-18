/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.model;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 巡检计划分页查询
 */
@Schema
@Getter
@Setter
@Accessors(chain = true)
public class InspectPlanPageQuery extends PageQuery {

    @Schema(description = "计划名称")
    private String name;

    @Schema(description = "巡检模板ID")
    private String templateId;

    @Schema(description = "周期类型")
    private Integer cycleType;

    @Schema(description = "场景编号")
    private String workshopCode;

    @Schema(description = "状态：0禁用 1启用")
    private Integer status;

    @Schema(description = "场景编号列表")
    private List<String> workshopCodes;
}
