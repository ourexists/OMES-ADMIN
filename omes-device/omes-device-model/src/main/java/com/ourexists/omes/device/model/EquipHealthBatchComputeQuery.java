/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Schema(description = "设备健康指标批量计算请求（定时评分用）")
@Getter
@Setter
@Accessors(chain = true)
public class EquipHealthBatchComputeQuery {

    @NotNull
    @Schema(description = "统计周期开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date periodStart;

    @NotNull
    @Schema(description = "统计周期结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date periodEnd;

    @NotEmpty
    @Schema(description = "设备自编码SN列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> snList;
}
