/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.mps.model.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class MPSBoardQuery {

    @Schema(description = "订单编号")
    private String moCode;

    @Schema(description = "订单编号(批量)")
    private List<String> moCodes;

    private Date execStartTime;

    private Date execEndTime;

    @Schema(description = "每列最大条数", defaultValue = "300")
    private Integer limitPerColumn = 300;
}
