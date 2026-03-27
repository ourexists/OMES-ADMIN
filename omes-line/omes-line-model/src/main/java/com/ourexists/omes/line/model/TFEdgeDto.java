/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.line.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * TF 前后置关系（边）
 */
@Getter
@Setter
@Accessors(chain = true)
public class TFEdgeDto extends BaseDto {

    @Schema(hidden = true)
    private String id;

    @Schema(description = "起点TF id")
    @NotEmpty(message = "${valid.tf_edge.from.empty}")
    private String fromTfId;

    @Schema(description = "终点TF id")
    @NotEmpty(message = "${valid.tf_edge.to.empty}")
    private String toTfId;
}

