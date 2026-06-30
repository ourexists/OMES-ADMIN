/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.mps.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class MPSBoardViewQuery {

    @Schema(description = "生产订单编号")
    private String moCode;

    @Schema(description = "配方名称")
    private String productName;

    @Schema(description = "配方编号")
    private String productCode;

    @Schema(description = "是否查询关联生产订单", defaultValue = "true")
    private Boolean queryMO = true;

    @Schema(description = "是否查询产线", defaultValue = "true")
    private Boolean queryLine = true;

    @Schema(description = "每列最大条数", defaultValue = "300")
    private Integer limitPerColumn = 300;
}
