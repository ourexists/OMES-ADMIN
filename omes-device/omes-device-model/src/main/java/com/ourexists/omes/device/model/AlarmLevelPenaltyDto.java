/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 按报警等级的扣分配置（次数扣分 + 时长扣分/分钟）
 */
@Schema(description = "报警等级扣分配置")
@Getter
@Setter
@Accessors(chain = true)
public class AlarmLevelPenaltyDto {

    @Schema(description = "次数扣分")
    private Integer count = 5;

    @Schema(description = "时长扣分/分钟")
    private Double perMin = 0.5;
}
