/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Schema(description = "产品分页查询")
@Getter
@Setter
@Accessors(chain = true)
public class ProductPageQuery extends PageQuery {

    @Schema(description = "产品名称")
    private String name;

    @Schema(description = "产品编号")
    private String code;
}
