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
public class MPSFlowDetailDto {

    @Schema(description = "流转时的对象id")
    private String id;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "设备编号")
    private String devNo;
}
