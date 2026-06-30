/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.line.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Schema(description = "工序关联工装")
public class TfToolingRef {

    @Schema(description = "物料/工装 ID")
    private String toolingId;

    @Schema(description = "工装编号")
    private String toolingCode;

    @Schema(description = "工装名称")
    private String toolingName;
}
