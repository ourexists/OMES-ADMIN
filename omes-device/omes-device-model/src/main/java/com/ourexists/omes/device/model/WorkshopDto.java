/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class WorkshopDto {

    private String id;

    private String name;

    @Schema(description = "编号")
    private String selfCode;

    private String code;

    private String pcode;

    @Schema(description = "经度，建议标注在最后一级场景")
    private BigDecimal lng;

    @Schema(description = "纬度，建议标注在最后一级场景")
    private BigDecimal lat;

    @Schema(description = "地址")
    private String address;
}
