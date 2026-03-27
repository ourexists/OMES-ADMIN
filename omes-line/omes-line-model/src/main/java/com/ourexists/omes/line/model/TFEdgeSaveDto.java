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

import java.util.List;

/**
 * 保存 TF 边关系
 */
@Getter
@Setter
@Accessors(chain = true)
public class TFEdgeSaveDto extends BaseDto {

    @Schema(hidden = true)
    private String lineId;

    @Schema(description = "边集合")
    @NotEmpty(message = "${valid.tf_edge.edges.empty}")
    private List<TFEdgeDto> edges;
}

