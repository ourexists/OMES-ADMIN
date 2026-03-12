/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 设备健康规则模板：id/name/enabled 存表字段，config 整体以 JSON 存库
 */
@Schema(description = "设备健康规则模板")
@Getter
@Setter
@Accessors(chain = true)
public class EquipHealthRuleTemplateDto {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "规则配置(JSON)，含周期、报警/运行/在线权重、等级阈值及按报警等级扣分")
    private EquipHealthRuleTemplateConfig config;

    /** 委托到 config，便于计算逻辑直接调用 */
    public Integer getPeriodHours() {
        return config != null && config.getPeriodHours() != null ? config.getPeriodHours() : 24;
    }

    public Integer getAlarmCountPenalty() {
        return config != null && config.getAlarmCountPenalty() != null ? config.getAlarmCountPenalty() : 5;
    }

    public Double getAlarmDurationPenaltyPerMin() {
        return config != null && config.getAlarmDurationPenaltyPerMin() != null ? config.getAlarmDurationPenaltyPerMin() : 0.5;
    }

    public Integer getAlarmMaxDeduction() {
        return config != null && config.getAlarmMaxDeduction() != null ? config.getAlarmMaxDeduction() : 40;
    }

    public Integer getRunRatioWeight() {
        return config != null && config.getRunRatioWeight() != null ? config.getRunRatioWeight() : 30;
    }

    public Integer getExpectedRunRatio() {
        return config != null && config.getExpectedRunRatio() != null ? config.getExpectedRunRatio() : 70;
    }

    public Integer getOnlineRatioWeight() {
        return config != null && config.getOnlineRatioWeight() != null ? config.getOnlineRatioWeight() : 20;
    }

    public Integer getHealthyThreshold() {
        return config != null && config.getHealthyThreshold() != null ? config.getHealthyThreshold() : 85;
    }

    public Integer getAttentionThreshold() {
        return config != null && config.getAttentionThreshold() != null ? config.getAttentionThreshold() : 70;
    }

    public Integer getWarningThreshold() {
        return config != null && config.getWarningThreshold() != null ? config.getWarningThreshold() : 50;
    }

    /** 按等级取扣分配置，key 为 AlarmLevelEnum.code 的字符串 */
    public Map<String, AlarmLevelPenaltyDto> getAlarmLevelPenalties() {
        return config != null && config.getAlarmLevelPenalties() != null
                ? config.getAlarmLevelPenalties()
                : null;
    }

    public Integer getLifecycleMaxDeduction() {
        return config != null && config.getLifecycleMaxDeduction() != null ? config.getLifecycleMaxDeduction() : 15;
    }

    public Integer getLifeYearThreshold() {
        return config != null && config.getLifeYearThreshold() != null ? config.getLifeYearThreshold() : 5;
    }

    public Double getLifeYearPenaltyPerYear() {
        return config != null && config.getLifeYearPenaltyPerYear() != null ? config.getLifeYearPenaltyPerYear() : 1.0;
    }

    public Integer getLifeYearMaxDeduction() {
        return config != null && config.getLifeYearMaxDeduction() != null ? config.getLifeYearMaxDeduction() : 5;
    }

    public Long getTotalRunHoursThreshold() {
        return config != null && config.getTotalRunHoursThreshold() != null ? config.getTotalRunHoursThreshold() : 10000L;
    }

    public Double getTotalRunHoursPenaltyPer1000() {
        return config != null && config.getTotalRunHoursPenaltyPer1000() != null ? config.getTotalRunHoursPenaltyPer1000() : 0.5;
    }

    public Integer getTotalRunHoursMaxDeduction() {
        return config != null && config.getTotalRunHoursMaxDeduction() != null ? config.getTotalRunHoursMaxDeduction() : 5;
    }

    public Long getStartStopCountThreshold() {
        return config != null && config.getStartStopCountThreshold() != null ? config.getStartStopCountThreshold() : 5000L;
    }

    public Double getStartStopCountPenaltyPer1000() {
        return config != null && config.getStartStopCountPenaltyPer1000() != null ? config.getStartStopCountPenaltyPer1000() : 0.2;
    }

    public Integer getStartStopCountMaxDeduction() {
        return config != null && config.getStartStopCountMaxDeduction() != null ? config.getStartStopCountMaxDeduction() : 5;
    }
}
