/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@Accessors(chain = true)
public class QACheckDto {

    @NotBlank
    protected String id;

    @Schema(description = "描述")
    protected String msg;

    @NotNull
    @Schema(description = "质检结果")
    protected Integer result;

    @Schema(description = "目标合格量")
    protected BigDecimal pass;

}
