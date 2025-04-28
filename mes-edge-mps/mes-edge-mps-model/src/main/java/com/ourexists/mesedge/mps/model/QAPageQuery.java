/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.model;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class QAPageQuery extends PageQuery {

    @Schema(description = "订单编号")
    private String moCode;

    @Schema(description = "是否查询对应计划")
    private Boolean queryMPS = false;

    @Schema(description = "是否查询对应计划流程点")
    private Boolean queryMPSTF = false;
}
