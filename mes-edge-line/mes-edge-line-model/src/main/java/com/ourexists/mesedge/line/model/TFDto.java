/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.line.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
public class TFDto extends BaseDto {

    @Schema(hidden = true)
    private String id;

    @Schema(description = "编号")
    @NotEmpty(message = "${valid.selfcode.empty}")
    private String selfCode;

    @Schema(description = "名称")
    @NotEmpty(message = "${valid.name.empty}")
    private String name;

    @Schema(description = "工艺属性")
    private String property;

    @Schema(description = "映射内存")
    private Integer mapDb;

    @Schema(description = "映射数据偏移量")
    protected String mapOffset;

    @Schema(description = "默认时长（秒）")
    private Long duration;

    @Schema(description = "上一流程编号", hidden = true)
    private String pre;

    @Schema(description = "下一流程编号", hidden = true)
    private String nex;

    @Schema(description = "产线id", hidden = true)
    private String lineId;

    @Schema(description = "流程类型(0:程序自动 1:人工质检)")
    protected Integer type;

    @Schema(description = "设定温度")
    private Double temperature;

    @Schema(description = "优先级(从小到大)")
    private BigDecimal priority = BigDecimal.ONE;
}
