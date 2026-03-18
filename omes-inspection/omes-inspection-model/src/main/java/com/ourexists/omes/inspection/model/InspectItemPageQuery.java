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
 * 巡检项分页/列表查询
 */
@Schema
@Getter
@Setter
@Accessors(chain = true)
public class InspectItemPageQuery extends PageQuery {

    @Schema(description = "巡检项名称")
    private String itemName;

    @Schema(description = "类型：1选择 2数值 3是否")
    private Integer itemType;
}
