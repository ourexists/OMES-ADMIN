/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import com.ourexists.omes.device.enums.EquipHealthLevelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 设备健康指标：某统计周期内的健康得分与明细
 */
@Schema(description = "设备健康指标")
@Getter
@Setter
@Accessors(chain = true)
public class EquipHealthIndicatorDto {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "设备ID")
    private String equipId;

    @Schema(description = "设备自编码SN")
    private String sn;

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "统计时间点(周期结束时间)")
    private Date statTime;

    @Schema(description = "周期开始时间")
    private Date periodStart;

    @Schema(description = "周期结束时间")
    private Date periodEnd;

    @Schema(description = "健康得分 0-100")
    private Integer score;

    @Schema(description = "健康等级: 0=健康, 1=关注, 2=预警, 3=故障")
    private Integer healthLevel;

    /** ========== 统计明细 ========== */
    @Schema(description = "周期内报警次数")
    private Integer alarmCount = 0;

    @Schema(description = "周期内报警总时长(分钟)")
    private Long alarmDurationMinutes = 0L;

    @Schema(description = "周期内运行总时长(分钟)")
    private Long runDurationMinutes = 0L;

    @Schema(description = "周期内在线总时长(分钟)")
    private Long onlineDurationMinutes = 0L;

    @Schema(description = "统计周期总分钟数")
    private Long periodMinutes = 0L;

    @Schema(description = "使用的规则模板ID")
    private String templateId;

    public String getHealthLevelDesc() {
        return healthLevel != null ? EquipHealthLevelEnum.valueOf(healthLevel).getDesc() : null;
    }
}
