/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.sync.model.query;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Schema(description = "网关分页查询")
public class GatewayPageQuery extends PageQuery {

    @Schema(description = "名称")
    private String name;

    @Schema(description = "编号")
    private String code;
}
