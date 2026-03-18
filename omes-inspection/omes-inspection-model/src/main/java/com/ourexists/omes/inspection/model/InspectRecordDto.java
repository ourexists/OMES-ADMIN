/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 巡检记录 DTO
 */
@Schema(description = "巡检记录")
@Getter
@Setter
@Accessors(chain = true)
public class InspectRecordDto extends BaseDto {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "巡检任务ID")
    private String taskId;

    @Schema(description = "设备ID")
    private String equipId;

    @Schema(description = "设备编号")
    private String equipSelfCode;

    @Schema(description = "设备名称")
    private String equipName;

    @Schema(description = "巡检分值")
    private Integer score;

    @Schema(description = "记录时间")
    private Date recordTime;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "巡检记录明细列表（附表）")
    private List<InspectRecordItemDto> items;
}
