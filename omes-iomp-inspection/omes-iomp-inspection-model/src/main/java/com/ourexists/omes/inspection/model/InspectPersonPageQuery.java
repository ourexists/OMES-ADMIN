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
 * 巡检人员分页查询
 */
@Schema
@Getter
@Setter
@Accessors(chain = true)
public class InspectPersonPageQuery extends PageQuery {

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "工号")
    private String jobNumber;

    @Schema(description = "关联账户ID")
    private String accountId;
}
