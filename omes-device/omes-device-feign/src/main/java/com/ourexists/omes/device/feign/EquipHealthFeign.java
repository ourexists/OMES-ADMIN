/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.feign;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.device.model.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 设备健康指标 Feign 接口
 */
public interface EquipHealthFeign {

    /**
     * 计算并保存指定设备在周期内的健康指标
     */
    JsonResponseEntity<EquipHealthIndicatorDto> computeAndSave(@Validated @RequestBody EquipHealthComputeQuery query);

    /**
     * 查询设备最新健康指标
     */
    JsonResponseEntity<EquipHealthIndicatorDto> getLatestBySn(@RequestParam String sn);

    /**
     * 获取默认健康规则模板
     */
    JsonResponseEntity<EquipHealthRuleTemplateDto> getDefaultTemplate();

    /**
     * 按租户与统计时间查询健康指标列表，statTime 格式 yyyy-MM-dd HH:mm:ss
     */
    JsonResponseEntity<List<EquipHealthIndicatorDto>> listByStatTime(
            @RequestBody EquipHealthIndicatorPageQuery pageQuery);

    /**
     * 查询所有健康规则模板
     */
    JsonResponseEntity<List<EquipHealthRuleTemplateDto>> listTemplates();

    /**
     * 保存健康规则模板
     */
    JsonResponseEntity<Boolean> saveTemplate(@Validated @RequestBody EquipHealthRuleTemplateDto dto);

    /**
     * 报警等级枚举列表（用于模板按等级配置扣分）
     */
    @GetMapping("getAlarmLevels")
    JsonResponseEntity<List<AlarmLevelItem>> getAlarmLevels();

    /**
     * 对关联指定模板的所有设备进行健康评分
     */
    @PostMapping("computeByTemplate")
    JsonResponseEntity<Map<String, Object>> computeByTemplate(@RequestParam String templateId);

    /**
     * 批量计算并保存设备健康指标（ForkJoin 并行计算后一次性写入数据库，供定时任务调用）
     */
    @PostMapping("computeBatchAndSave")
    JsonResponseEntity<Map<String, Object>> computeBatchAndSave(@Validated @RequestBody EquipHealthBatchComputeQuery query);
}
