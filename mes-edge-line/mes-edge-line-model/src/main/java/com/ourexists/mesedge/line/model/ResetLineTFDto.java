/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.line.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ResetLineTFDto {

    @NotBlank
    @Schema(description = "工艺编号")
    private String lineCode;

    @NotEmpty
    @Schema(description = "工艺流程")
    private List<TFDto> tfs;
}
