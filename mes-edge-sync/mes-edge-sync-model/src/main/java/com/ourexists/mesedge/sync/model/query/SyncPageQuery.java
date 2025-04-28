/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.model.query;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Schema
@Accessors(chain = true)
public class SyncPageQuery extends PageQuery {

    @Schema(description = "是否查询资源详情")
    private Boolean queryResource = false;

    @Schema(description = "同步事务")
    private String syncTx;

    @Schema(description = "同步状态")
    private String status;

    @Schema(description = "同步创建区间开始")
    private Date createStartDate;

    @Schema(description = "同步创建区间结束")
    private Date createEndDate;
}
