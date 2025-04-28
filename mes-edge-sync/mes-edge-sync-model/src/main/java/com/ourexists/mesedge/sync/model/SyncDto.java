/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@Schema
@Accessors(chain = true)
public class SyncDto extends BaseDto {

    @Schema(description = "id")
    private String id;

    @Schema(description = "同步事务")
    private String syncTx;

    @Schema(description = "分片开始时间戳")
    private Date partStartTimestamp;

    @Schema(description = "分片结束时间戳")
    private Date partEndTimestamp;

    @Schema(description = "同步状态")
    private String status;

    @Schema(description = "分片最小值")
    private String partMin;

    @Schema(description = "分片最大值")
    private String partMax;
}
