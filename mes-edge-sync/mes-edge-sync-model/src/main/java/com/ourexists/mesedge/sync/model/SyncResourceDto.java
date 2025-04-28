/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Schema
@Accessors(chain = true)
public class SyncResourceDto extends BaseDto {

    @Schema(description = "id")
    private String id;

    @Schema(description = "同步事件id")
    private String syncId;

    @Schema(description = "同步点位")
    private String point;

    @Schema(description = "点位状态")
    private String status;

    @Schema(description = "点位入参数据")
    private String reqData;

    @Schema(description = "点位出参数据")
    private String respData;

    @Schema(description = "异常信息")
    private String excep;
}
