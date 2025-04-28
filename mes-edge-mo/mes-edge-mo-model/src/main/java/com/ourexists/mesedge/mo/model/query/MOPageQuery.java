/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mo.model.query;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Schema
@Accessors(chain = true)
public class MOPageQuery extends PageQuery {

    @Schema(description = "产品名称")
    private String productName;

    @Schema(description = "产品编号")
    private String productCode;

    @Schema(description = "编号")
    private String selfCode;

    @Schema(description = "编号s")
    private List<String> selfCodes;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "是否查询清单详情(默认false)")
    private Boolean queryDetail = false;
}
