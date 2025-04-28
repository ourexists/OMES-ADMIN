/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class MOTFDto {

    protected String id;

    @Schema(description = "流程编号")
    protected String selfCode;

    @Schema(description = "流程名称")
    protected String name;

    @Schema(description = "映射数据内存")
    protected String mapDb;

    @Schema(description = "流程工艺属性")
    protected String property;

    @Schema(description = "流程工艺时长")
    protected Long duration;

    @Schema(description = "上一流程编号")
    protected String pre;

    @Schema(description = "下一流程编号")
    protected String nex;

    @Schema(description = "对应生产订单编号")
    protected String moCode;

}
