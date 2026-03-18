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
 * 巡检记录分页查询
 */
@Schema
@Getter
@Setter
@Accessors(chain = true)
public class InspectRecordPageQuery extends PageQuery {

    @Schema(description = "巡检任务ID")
    private String taskId;

    @Schema(description = "设备ID")
    private String equipId;

    @Schema(description = "设备名称/编号 模糊")
    private String equipName;

    @Schema(description = "巡检结果")
    private Integer result;

    @Schema(description = "记录时间开始")
    private Date recordTimeStart;

    @Schema(description = "记录时间结束")
    private Date recordTimeEnd;

    @Schema(description = "任务ID列表")
    private List<String> taskIds;
}
