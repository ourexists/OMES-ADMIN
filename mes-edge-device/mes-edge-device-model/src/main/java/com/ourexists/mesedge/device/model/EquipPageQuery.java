/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.model;

import com.ourexists.era.framework.core.model.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Schema
@Getter
@Setter
@Accessors(chain = true)
public class EquipPageQuery extends PageQuery {

    @Schema(description = "场景编号")
    private String workshopCode;

    @Schema(description = "设备名")
    private String name;

    @Schema(description = "设备编号")
    private String selfCode;

    @Schema(description = "设备类型")
    private Integer type;

    @Schema(description = "是否同时获取设备实时信息")
    private Boolean needRealtime = false;

    @Schema(description = "是否需要场景级联查询")
    private Boolean needWorkshopCascade = false;
}
