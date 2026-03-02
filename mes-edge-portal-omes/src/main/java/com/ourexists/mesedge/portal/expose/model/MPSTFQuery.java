/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.expose.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Schema
@Accessors(chain = true)
public class MPSTFQuery {

    @Schema(description = "清单计划编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "")
    private String mpsCode;
}
