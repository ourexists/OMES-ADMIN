/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.task.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Schema
@Accessors(chain = true)
public class TaskDto extends BaseDto {

    @Schema(description = "id")
    protected String id;

    @Schema(description = "名称")
    protected String name;

    @Schema(description = "任务类型")
    protected String type;

    @Schema(description = "cron表达式")
    protected String cron;


}
