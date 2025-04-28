/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
public class MPSTFDto {

    protected String id;

    @Schema(description = "执行计划id")
    protected String mpsId;

    @Schema(description = "流程编号")
    protected String selfCode;

    @Schema(description = "流程名称")
    protected String name;

    @Schema(description = "映射数据内存")
    protected String mapDb;

    @Schema(description = "映射数据偏移量")
    protected String mapOffset;

    @Schema(description = "流程工艺属性")
    protected String property;

    @Schema(description = "流程工艺时长")
    protected Long duration;

    @Schema(description = "流程状态")
    protected Integer status;

    @Schema(description = "上一流程编号")
    protected String pre;

    @Schema(description = "下一流程编号")
    protected String nex;

    @Schema(description = "工艺流程开始时间")
    protected Date startTime;

    @Schema(description = "工艺流程结束时间")
    protected Date endTime;

    @Schema(description = "工艺流程开始温度")
    protected Double startTemperature;

    @Schema(description = "工艺流程结束温度")
    protected Double endTemperature;

    @Schema(description = "对应生产订单编号")
    protected String moCode;

    @Schema(description = "流程类型")
    protected Integer type;

    @Schema(description = "设定温度")
    private Double temperature;

}
