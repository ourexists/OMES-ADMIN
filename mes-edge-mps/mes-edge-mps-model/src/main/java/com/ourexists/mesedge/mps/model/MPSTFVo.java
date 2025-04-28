/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.model;

import com.ourexists.mesedge.mps.enums.MPSTFStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class MPSTFVo extends MPSTFDto {

    @Schema(description = "流程状态")
    protected String statusDesc;

    protected String createdBy;

    protected String createdId;

    protected Date createdTime;

    protected String updatedBy;

    protected String updatedId;

    protected Date updatedTime;

    public String getStatusDesc() {
        return MPSTFStatusEnum.valueOf(status).getName();
    }
}
