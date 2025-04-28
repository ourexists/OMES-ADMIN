/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.model.query;

import com.ourexists.era.framework.core.model.dto.PageQuery;
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
public class MPSPageQuery extends PageQuery {

    @Schema(description = "订单编号")
    private String moCode;

    @Schema(description = "订单编号(批量)")
    private List<String> moCodes;

    @Schema(description = "配方名称")
    private String productName;

    @Schema(description = "配方编号")
    private String productCode;

    private Date execStartTime;

    private Date execEndTime;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "是否查询详情", defaultValue = "false")
    private Boolean queryDetail = false;

    @Schema(description = "是否根据优先级排序", defaultValue = "false")
    private Boolean prioritySort = false;
}
