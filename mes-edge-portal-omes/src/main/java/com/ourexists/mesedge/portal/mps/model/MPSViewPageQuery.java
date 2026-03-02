/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.mps.model;

import com.ourexists.mesedge.mps.model.query.MPSPageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Schema
@Getter
@Setter
@Accessors(chain = true)
public class MPSViewPageQuery extends MPSPageQuery {


    @Schema(description = "是否查询关联生产订单", defaultValue = "false")
    private Boolean queryMO = false;

    @Schema(description = "是否查询产线", defaultValue = "false")
    private Boolean queryLine = false;
}
