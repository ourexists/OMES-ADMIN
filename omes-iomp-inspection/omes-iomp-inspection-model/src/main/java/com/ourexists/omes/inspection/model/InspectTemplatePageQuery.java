/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.model;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 巡检模板分页查询
 */
@Schema
@Getter
@Setter
@Accessors(chain = true)
public class InspectTemplatePageQuery extends PageQuery {

    @Schema(description = "模板名称")
    private String name;
}
