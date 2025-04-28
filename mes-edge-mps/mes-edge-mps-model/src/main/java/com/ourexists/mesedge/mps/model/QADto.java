/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Schema
@Accessors(chain = true)
public class QADto extends BaseDto {

    protected String id;

    @Schema(description = "执行计划id")
    protected String mpsId;

    @Schema(description = "计划流程id")
    private String mpsTfId;

    @Schema(description = "描述")
    protected String msg;

    @Schema(description = "质检结果")
    protected Integer result;

    @Schema(description = "目标合格量")
    protected BigDecimal pass;
}
