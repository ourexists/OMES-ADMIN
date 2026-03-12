/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Schema(description = "设备健康指标计算请求")
@Getter
@Setter
@Accessors(chain = true)
public class EquipHealthComputeQuery {

    @NotBlank
    @Schema(description = "设备自编码SN", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sn;

    @NotNull
    @Schema(description = "统计周期开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date periodStart;

    @NotNull
    @Schema(description = "统计周期结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date periodEnd;

    @Schema(description = "规则模板ID，不传则使用默认模板")
    private String templateId;
}
