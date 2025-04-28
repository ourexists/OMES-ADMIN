/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class MaterialClassifyDto {

    private String id;

    @Schema(description = "编号")
    private String selfCode;

    @Schema(description = "名称")
    private String name;
}
