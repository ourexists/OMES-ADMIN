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
public class EquipMatDto {

    @Schema(description = "原料编号")
    private String matCode;

    @Schema(description = "原料名称（查询时回填）")
    private String matName;

    @Schema(description = "该原料在本能力方案下的容量")
    private BigDecimal maxCapacity;
}
