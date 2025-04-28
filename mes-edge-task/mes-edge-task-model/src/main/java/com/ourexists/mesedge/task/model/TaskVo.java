/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.task.model;

import com.ourexists.mesedge.task.enums.TaskStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Schema
@Accessors(chain = true)
public class TaskVo extends TaskDto {

    private String createdBy;

    private String createdId;

    private Date createdTime;

    private String updatedBy;

    private String updatedId;

    private Date updatedTime;

    @Schema(description = "任务状态")
    protected Integer status;

    @Schema(description = "任务状态")
    private String statusDesc;

    public String getStatusDesc() {
        return TaskStatusEnum.valueOf(this.status).getName();
    }
}

