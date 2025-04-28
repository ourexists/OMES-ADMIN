/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class MPSFlowDto {

    @Schema(description = "id")
    private String id;

    @Schema(description = "订单编号")
    private String moCode;

    @Schema(description = "产线编号")
    private String line;

    @Schema(description = "生产执行序列号")
    private Integer sequence = 0;

    @Schema(description = "执行时间")
    private Date execTime;

    @Schema(description = "产生方式")
    private Integer execType;

    @Schema(description = "每单生产数量")
    private Integer execNum;

    @Schema(description = "清单详情")
    private List<MPSFlowDetailDto> details;

    @Schema(description = "工艺详情")
    private List<MPSTFDto> tfs;
}
