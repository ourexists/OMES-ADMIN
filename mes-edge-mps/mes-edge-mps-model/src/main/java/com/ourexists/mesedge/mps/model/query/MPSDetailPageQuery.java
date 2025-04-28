/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.model.query;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Schema
@Getter
@Setter
@Accessors(chain = true)
public class MPSDetailPageQuery extends PageQuery {

    @Schema(description = "成分名称")
    private String name;

    @Schema(description = "成分编号")
    private String selfCode;

    @Schema(description = "属性")
    private Integer attribute;

    @Schema(description = "主体编号")
    private String mcode;
}
