/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class MPSQueueOperateDto {

    @Schema(description = "计划清单id")
    private String id;

    @Schema(description = "加入方式")
    private Integer type = 0;
}
