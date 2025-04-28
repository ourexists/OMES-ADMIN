/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.model.query;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Schema
@Getter
@Setter
@Accessors(chain = true)
public class BOMPageQuery extends PageQuery {

    @Schema(description = "配方名称")
    private String name;

    @Schema(description = "配方编号")
    private String selfCode;

    @Schema(description = "所属分类编号")
    private String classifyCode;

    @Schema(description = "类别")
    private Integer type;

    @Schema(description = "成分名")
    private String detailName;

    @Schema(description = "是否查询成分", defaultValue = "false")
    private Boolean queryDetail = false;
}
