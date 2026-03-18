/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.device.mapper.EquipHealthRuleTemplateMapper;
import com.ourexists.omes.device.model.AlarmLevelPenaltyDto;
import com.ourexists.omes.device.model.EquipHealthRuleTemplateConfig;
import com.ourexists.omes.device.model.EquipHealthRuleTemplateDto;
import com.ourexists.omes.device.pojo.EquipHealthRuleTemplate;
import com.ourexists.omes.device.service.EquipHealthRuleTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EquipHealthRuleTemplateServiceImpl
        extends AbstractMyBatisPlusService<EquipHealthRuleTemplateMapper, EquipHealthRuleTemplate>
        implements EquipHealthRuleTemplateService {

    @Override
    public EquipHealthRuleTemplateDto getDefaultTemplate() {
        EquipHealthRuleTemplateDto dto = new EquipHealthRuleTemplateDto();
        dto.setName("工业设备通用健康模板");
        dto.setConfig(buildIndustrialPresetConfig());
        return dto;
    }

    /** 工业常用评分预设：统计周期24h，报警/运行/在线/生命周期及等级阈值 */
    private static EquipHealthRuleTemplateConfig buildIndustrialPresetConfig() {
        EquipHealthRuleTemplateConfig c = new EquipHealthRuleTemplateConfig();
        c.setPeriodHours(24);
        c.setAlarmCountPenalty(4);
        c.setAlarmDurationPenaltyPerMin(0.4);
        c.setAlarmMaxDeduction(38);
        Map<String, AlarmLevelPenaltyDto> levelPenalties = new LinkedHashMap<>();
        levelPenalties.put("0", new AlarmLevelPenaltyDto().setCount(2).setPerMin(0.2));
        levelPenalties.put("1", new AlarmLevelPenaltyDto().setCount(5).setPerMin(0.5));
        levelPenalties.put("2", new AlarmLevelPenaltyDto().setCount(10).setPerMin(1.0));
        levelPenalties.put("3", new AlarmLevelPenaltyDto().setCount(15).setPerMin(1.5));
        c.setAlarmLevelPenalties(levelPenalties);
        c.setRunRatioWeight(30);
        c.setExpectedRunRatio(75);
        c.setOnlineRatioWeight(20);
        c.setLifecycleMaxDeduction(12);
        c.setLifeYearThreshold(5);
        c.setLifeYearPenaltyPerYear(1.0);
        c.setLifeYearMaxDeduction(5);
        c.setTotalRunHoursThreshold(15000L);
        c.setTotalRunHoursPenaltyPer1000(0.5);
        c.setTotalRunHoursMaxDeduction(5);
        c.setStartStopCountThreshold(8000L);
        c.setStartStopCountPenaltyPer1000(0.2);
        c.setStartStopCountMaxDeduction(5);
        c.setInspectionWeight(10);
        c.setInspectionNoRecordPenalty(5);
        c.setHealthyThreshold(85);
        c.setAttentionThreshold(70);
        c.setWarningThreshold(50);
        return c;
    }

    @Override
    public EquipHealthRuleTemplateDto getByIdOrDefault(String templateId) {
        if (templateId == null || templateId.isEmpty()) {
            return getDefaultTemplate();
        }
        EquipHealthRuleTemplate entity = this.getById(templateId);
        if (entity == null) {
            return getDefaultTemplate();
        }
        EquipHealthRuleTemplateDto dto = EquipHealthRuleTemplate.toDto(entity);
        if (dto.getConfig() != null) {
            if (dto.getConfig().getHealthyThreshold() == null) dto.getConfig().setHealthyThreshold(85);
            if (dto.getConfig().getAttentionThreshold() == null) dto.getConfig().setAttentionThreshold(70);
            if (dto.getConfig().getWarningThreshold() == null) dto.getConfig().setWarningThreshold(50);
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(EquipHealthRuleTemplateDto dto) {
        EquipHealthRuleTemplate entity = EquipHealthRuleTemplate.fromDto(dto);
        this.saveOrUpdate(entity);
    }

    @Override
    public List<EquipHealthRuleTemplateDto> getAll() {
        List<EquipHealthRuleTemplate> r = this.list(new LambdaQueryWrapper<>());
        return r.stream()
                .map(EquipHealthRuleTemplate::toDto)
                .collect(Collectors.toList());
    }
}
