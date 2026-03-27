/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 按设备ID与统计周期查询巡检记录（POST 请求体，用于设备健康分巡检维度）
 */
@Schema(description = "按设备与周期查询巡检记录")
@Getter
@Setter
@Accessors(chain = true)
public class InspectRecordListByEquipPeriodQuery {

    @Schema(description = "设备ID", required = true)
    private String equipId;

    @Schema(description = "周期开始时间")
    private Date periodStart;

    @Schema(description = "周期结束时间")
    private Date periodEnd;
}
