/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 设备健康规则模板配置（存库 JSON），key 与 {@link com.ourexists.omes.device.enums.AlarmLevelEnum} 的 code 一致
 */
@Schema(description = "健康规则模板配置(JSON)")
@Getter
@Setter
@Accessors(chain = true)
public class EquipHealthRuleTemplateConfig {

    @Schema(description = "统计周期(小时)")
    private Integer periodHours = 24;

    @Schema(description = "报警通用-次数扣分(未配置等级时使用)")
    private Integer alarmCountPenalty = 5;

    @Schema(description = "报警通用-时长扣分/分钟")
    private Double alarmDurationPenaltyPerMin = 0.5;

    @Schema(description = "报警最大扣分上限")
    private Integer alarmMaxDeduction = 40;

    /** key: AlarmLevelEnum.code 的字符串 "0","1","2","3" */
    @Schema(description = "按报警等级扣分")
    private Map<String, AlarmLevelPenaltyDto> alarmLevelPenalties = defaultAlarmLevelPenalties();

    @Schema(description = "运行率权重")
    private Integer runRatioWeight = 30;

    @Schema(description = "期望运行率")
    private Integer expectedRunRatio = 70;

    @Schema(description = "在线率权重")
    private Integer onlineRatioWeight = 20;

    /** ========== 生命周期维度（设备老化） ========== */
    @Schema(description = "生命周期维度最大扣分上限")
    private Integer lifecycleMaxDeduction = 15;
    @Schema(description = "使用年限-超过该年数开始扣分")
    private Integer lifeYearThreshold = 5;
    @Schema(description = "使用年限-每超过1年扣分")
    private Double lifeYearPenaltyPerYear = 1.0;
    @Schema(description = "使用年限-单项扣分上限")
    private Integer lifeYearMaxDeduction = 5;
    @Schema(description = "累计运行小时-超过该小时数开始扣分")
    private Long totalRunHoursThreshold = 10000L;
    @Schema(description = "累计运行小时-每超1000小时扣分")
    private Double totalRunHoursPenaltyPer1000 = 0.5;
    @Schema(description = "累计运行小时-单项扣分上限")
    private Integer totalRunHoursMaxDeduction = 5;
    @Schema(description = "启停总次数-超过该次数开始扣分")
    private Long startStopCountThreshold = 5000L;
    @Schema(description = "启停总次数-每超1000次扣分")
    private Double startStopCountPenaltyPer1000 = 0.2;
    @Schema(description = "启停总次数-单项扣分上限")
    private Integer startStopCountMaxDeduction = 5;

    /** ========== 巡检维度 ========== */
    @Schema(description = "巡检维度权重（满分100中占比，0表示不参与评分）")
    private Integer inspectionWeight = 10;
    @Schema(description = "周期内无巡检记录时的扣分（扣分上限不超过 inspectionWeight）")
    private Integer inspectionNoRecordPenalty = 5;

    @Schema(description = "健康线")
    private Integer healthyThreshold = 85;

    @Schema(description = "关注线")
    private Integer attentionThreshold = 70;

    @Schema(description = "预警线")
    private Integer warningThreshold = 50;

    private static Map<String, AlarmLevelPenaltyDto> defaultAlarmLevelPenalties() {
        Map<String, AlarmLevelPenaltyDto> m = new LinkedHashMap<>();
        m.put("0", new AlarmLevelPenaltyDto().setCount(2).setPerMin(0.2));
        m.put("1", new AlarmLevelPenaltyDto().setCount(5).setPerMin(0.5));
        m.put("2", new AlarmLevelPenaltyDto().setCount(10).setPerMin(1.0));
        m.put("3", new AlarmLevelPenaltyDto().setCount(15).setPerMin(1.5));
        return m;
    }
}
