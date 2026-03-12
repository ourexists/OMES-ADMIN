/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service.impl;

import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipRealtimeManager;
import com.ourexists.omes.device.enums.AlarmLevelEnum;
import com.ourexists.omes.device.enums.EquipHealthLevelEnum;
import com.ourexists.omes.device.model.*;
import com.ourexists.omes.device.pojo.Equip;
import com.ourexists.omes.device.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 基于报警记录、运行记录、在线记录及规则模板，计算设备健康得分并保存
 */
@Service
public class EquipHealthComputeServiceImpl implements EquipHealthComputeService {

    @Autowired
    private EquipRecordAlarmService equipRecordAlarmService;
    @Autowired
    private EquipRecordRunService equipRecordRunService;
    @Autowired
    private EquipRecordOnlineService equipRecordOnlineService;
    @Autowired
    private EquipRealtimeManager realtimeManager;
    @Autowired
    private EquipHealthRuleTemplateService ruleTemplateService;
    @Autowired
    private EquipHealthIndicatorService healthIndicatorService;
    @Autowired
    private EquipService equipService;

    @Override
    public EquipHealthIndicatorDto computeAndSave(EquipHealthComputeQuery query, String equipId) {
        String templateId = query.getTemplateId();
        if (templateId == null || templateId.isEmpty()) {
            Equip equip = equipService.getBySelfCode(query.getSn());
            if (equip != null && equip.getHealthTemplateId() != null && !equip.getHealthTemplateId().isEmpty()) {
                templateId = equip.getHealthTemplateId();
            }
        }
        EquipHealthRuleTemplateDto template = (templateId != null && !templateId.isEmpty())
                ? ruleTemplateService.getByIdOrDefault(templateId)
                : ruleTemplateService.getDefaultTemplate();
        Date periodStart = query.getPeriodStart();
        Date periodEnd = query.getPeriodEnd();
        String sn = query.getSn();

        EquipRecordCountQuery countQuery = new EquipRecordCountQuery();
        countQuery.setSn(sn);
        countQuery.setStartDate(periodStart);
        countQuery.setEndDate(periodEnd);

        // 报警：合并段，按等级统计并应用对应分值扣分
        List<EquipRecordAlarmVo> alarmList = equipRecordAlarmService.countMerging(countQuery);
        double alarmDeduction = 0;
        int alarmCount = 0;
        long alarmDurationMinutes = 0L;
        for (EquipRecordAlarmVo vo : alarmList) {
            if (vo.getState() != null && vo.getState() == 1) {
                alarmCount++;
                long dur = vo.getDuration() != null ? vo.getDuration() : 0L;
                alarmDurationMinutes += dur;
                int level = vo.getLevel() != null ? vo.getLevel() : AlarmLevelEnum.general.getCode();
                int countPenalty = getAlarmCountPenaltyForLevel(template, level);
                double perMinPenalty = getAlarmPerMinPenaltyForLevel(template, level);
                alarmDeduction += countPenalty + dur * perMinPenalty;
            }
        }
        int alarmMax = template.getAlarmMaxDeduction() != null ? template.getAlarmMaxDeduction() : 40;
        alarmDeduction = Math.min(alarmDeduction, alarmMax);

        // 运行：合并段，统计 state=1 的时长
        List<EquipRecordRunVo> runList = equipRecordRunService.countMerging(countQuery);
        long runDurationMinutes = 0L;
        for (EquipRecordRunVo vo : runList) {
            if (vo.getState() != null && vo.getState() == 1) {
                runDurationMinutes += (vo.getDuration() != null ? vo.getDuration() : 0L);
            }
        }

        // 在线：合并段，统计 state=1 的时长
        EquipRealtime realtime = realtimeManager.get(sn);
        List<EquipRecordOnlineVo> onlineList = equipRecordOnlineService.countMerging(realtime, countQuery);
        long onlineDurationMinutes = 0L;
        for (EquipRecordOnlineVo vo : onlineList) {
            if (vo.getState() != null && vo.getState() == 1) {
                onlineDurationMinutes += (vo.getDuration() != null ? vo.getDuration() : 0L);
            }
        }

        long periodMinutes = (periodEnd.getTime() - periodStart.getTime()) / (60 * 1000);
        if (periodMinutes <= 0) periodMinutes = 1;

        // 生命周期维度扣分（使用年限、累计运行小时、启停总次数，考虑设备老化）
        double lifecycleDeduction = computeLifecycleDeduction(template, sn);

        // 从实时缓存补全 equipId、tenantId
        if (realtime != null) {
            if (equipId == null) equipId = realtime.getId();
        }

        // 计算得分
        int score = computeScore(template, alarmDeduction, lifecycleDeduction,
                runDurationMinutes, onlineDurationMinutes, periodMinutes);

        EquipHealthLevelEnum level = EquipHealthLevelEnum.fromScore(
                score,
                template.getHealthyThreshold() != null ? template.getHealthyThreshold() : 85,
                template.getAttentionThreshold() != null ? template.getAttentionThreshold() : 70,
                template.getWarningThreshold() != null ? template.getWarningThreshold() : 50);

        EquipHealthIndicatorDto dto = new EquipHealthIndicatorDto();
        dto.setEquipId(equipId);
        dto.setSn(sn);
        dto.setTenantId(realtime != null ? realtime.getTenantId() : null);
        dto.setStatTime(periodEnd);
        dto.setPeriodStart(periodStart);
        dto.setPeriodEnd(periodEnd);
        dto.setScore(score);
        dto.setHealthLevel(level.getCode());
        dto.setAlarmCount(alarmCount);
        dto.setAlarmDurationMinutes(alarmDurationMinutes);
        dto.setRunDurationMinutes(runDurationMinutes);
        dto.setOnlineDurationMinutes(onlineDurationMinutes);
        dto.setPeriodMinutes(periodMinutes);
        dto.setTemplateId(template.getId());

        healthIndicatorService.saveOrUpdate(dto);
        return dto;
    }

    /**
     * 按报警等级取次数扣分（依据模板 config.alarmLevelPenalties，无则用通用扣分）
     */
    private int getAlarmCountPenaltyForLevel(EquipHealthRuleTemplateDto t, int level) {
        AlarmLevelPenaltyDto p = getLevelPenalty(t, level);
        return p != null && p.getCount() != null ? p.getCount() : (t.getAlarmCountPenalty() != null ? t.getAlarmCountPenalty() : 5);
    }

    /**
     * 按报警等级取时长扣分/分钟
     */
    private double getAlarmPerMinPenaltyForLevel(EquipHealthRuleTemplateDto t, int level) {
        AlarmLevelPenaltyDto p = getLevelPenalty(t, level);
        return p != null && p.getPerMin() != null ? p.getPerMin() : (t.getAlarmDurationPenaltyPerMin() != null ? t.getAlarmDurationPenaltyPerMin() : 0.5);
    }

    private AlarmLevelPenaltyDto getLevelPenalty(EquipHealthRuleTemplateDto t, int level) {
        if (t.getAlarmLevelPenalties() == null) return null;
        return t.getAlarmLevelPenalties().get(String.valueOf(level));
    }

    /**
     * 生命周期维度扣分：使用年限、累计运行小时、启停总次数（设备老化）
     */
    private double computeLifecycleDeduction(EquipHealthRuleTemplateDto t, String sn) {
        int cap = t.getLifecycleMaxDeduction() != null ? t.getLifecycleMaxDeduction() : 15;
        double total = 0;

        // 使用年限：超过 threshold 年后，每多一年扣 penalty，单项上限 lifeYearMaxDeduction
        // 优先用启用日期 enableDate，无则用创建时间 createTime
        Equip equip = equipService.getBySelfCode(sn);
        if (equip != null) {
            Date refDate = equip.getEnableDate() != null ? equip.getEnableDate() : equip.getCreatedTime();
            if (refDate != null) {
                long yearsMillis = System.currentTimeMillis() - refDate.getTime();
                double years = yearsMillis / (365.25 * 24 * 60 * 60 * 1000.0);
                int yThresh = t.getLifeYearThreshold() != null ? t.getLifeYearThreshold() : 5;
                double yPenalty = t.getLifeYearPenaltyPerYear() != null ? t.getLifeYearPenaltyPerYear() : 1.0;
                int yCap = t.getLifeYearMaxDeduction() != null ? t.getLifeYearMaxDeduction() : 5;
                if (years > yThresh) {
                    double d = (years - yThresh) * yPenalty;
                    total += Math.min(d, yCap);
                }
            }
        }

        // 累计运行小时：超过 threshold 小时后，每 1000 小时扣 penalty，单项上限
        long runMinutes = equipRecordRunService.sumRunMinutesBySn(sn);
        long runHours = runMinutes / 60;
        long runThresh = t.getTotalRunHoursThreshold() != null ? t.getTotalRunHoursThreshold() : 10000L;
        double runPenalty = t.getTotalRunHoursPenaltyPer1000() != null ? t.getTotalRunHoursPenaltyPer1000() : 0.5;
        int runCap = t.getTotalRunHoursMaxDeduction() != null ? t.getTotalRunHoursMaxDeduction() : 5;
        if (runHours > runThresh) {
            double d = ((runHours - runThresh) / 1000.0) * runPenalty;
            total += Math.min(d, runCap);
        }

        // 启停总次数：超过 threshold 次后，每 1000 次扣 penalty，单项上限
        long segments = equipRecordRunService.countRunSegmentsBySn(sn);
        long segThresh = t.getStartStopCountThreshold() != null ? t.getStartStopCountThreshold() : 5000L;
        double segPenalty = t.getStartStopCountPenaltyPer1000() != null ? t.getStartStopCountPenaltyPer1000() : 0.2;
        int segCap = t.getStartStopCountMaxDeduction() != null ? t.getStartStopCountMaxDeduction() : 5;
        if (segments > segThresh) {
            double d = ((segments - segThresh) / 1000.0) * segPenalty;
            total += Math.min(d, segCap);
        }

        return Math.min(total, cap);
    }

    /**
     * 得分 = 100 - 报警扣分 - 生命周期扣分 - 运行率扣分 - 在线率扣分，再限制在 [0,100]
     */
    private int computeScore(EquipHealthRuleTemplateDto t,
                             double alarmDeduction,
                             double lifecycleDeduction,
                             long runDurationMinutes, long onlineDurationMinutes, long periodMinutes) {
        int runWeight = t.getRunRatioWeight() != null ? t.getRunRatioWeight() : 30;
        int onlineWeight = t.getOnlineRatioWeight() != null ? t.getOnlineRatioWeight() : 20;

        double runRatio = (onlineDurationMinutes > 0)
                ? (double) runDurationMinutes / onlineDurationMinutes
                : 1.0;
        double onlineRatio = periodMinutes > 0 ? (double) onlineDurationMinutes / periodMinutes : 0;

        double runDeduction = runWeight * (1 - runRatio);
        double onlineDeduction = onlineWeight * (1 - onlineRatio);

        int score = (int) Math.round(100 - alarmDeduction - lifecycleDeduction - runDeduction - onlineDeduction);
        return Math.max(0, Math.min(100, score));
    }
}
