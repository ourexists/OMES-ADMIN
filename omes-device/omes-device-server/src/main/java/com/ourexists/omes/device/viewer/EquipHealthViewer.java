/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.viewer;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.device.enums.AlarmLevelEnum;
import com.ourexists.omes.device.feign.EquipHealthFeign;
import com.ourexists.omes.device.model.*;
import com.ourexists.omes.device.pojo.Equip;
import com.ourexists.omes.device.service.EquipHealthComputeService;
import com.ourexists.omes.device.service.EquipHealthIndicatorService;
import com.ourexists.omes.device.service.EquipHealthRuleTemplateService;
import com.ourexists.omes.device.service.EquipService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class EquipHealthViewer implements EquipHealthFeign {

    @Autowired
    private EquipHealthComputeService equipHealthComputeService;
    @Autowired
    private EquipHealthIndicatorService equipHealthIndicatorService;
    @Autowired
    private EquipHealthRuleTemplateService ruleTemplateService;
    @Autowired
    private EquipService equipService;

    @Override
    @Operation(summary = "计算并保存设备健康指标")
    @PostMapping("computeAndSave")
    public JsonResponseEntity<EquipHealthIndicatorDto> computeAndSave(@Validated @RequestBody EquipHealthComputeQuery query) {
        EquipHealthIndicatorDto dto = equipHealthComputeService.computeAndSave(query, null);
        return JsonResponseEntity.success(dto);
    }

    @Override
    @Operation(summary = "查询设备最新健康指标")
    @PostMapping("getLatestBySn")
    public JsonResponseEntity<EquipHealthIndicatorDto> getLatestBySn(@RequestParam String sn) {
        EquipHealthIndicatorDto dto = equipHealthIndicatorService.getLatestBySn(sn);
        return JsonResponseEntity.success(dto);
    }

    @Override
    @Operation(summary = "获取默认健康规则模板")
    @PostMapping("getDefaultTemplate")
    public JsonResponseEntity<EquipHealthRuleTemplateDto> getDefaultTemplate() {
        return JsonResponseEntity.success(ruleTemplateService.getDefaultTemplate());
    }

    @Override
    @Operation(summary = "按租户与统计时间查询健康指标列表")
    @PostMapping("listByStatTime")
    public JsonResponseEntity<List<EquipHealthIndicatorDto>> listByStatTime(
            @RequestBody EquipHealthIndicatorPageQuery pageQuery) {
        List<EquipHealthIndicatorDto> list = equipHealthIndicatorService.listByStatTime(pageQuery);
        return JsonResponseEntity.success(list);
    }

    @Override
    @Operation(summary = "查询所有健康规则模板")
    @PostMapping("listTemplates")
    public JsonResponseEntity<List<EquipHealthRuleTemplateDto>> listTemplates() {
        return JsonResponseEntity.success(ruleTemplateService.list());
    }

    @Override
    @Operation(summary = "保存健康规则模板")
    @PostMapping("saveTemplate")
    public JsonResponseEntity<Boolean> saveTemplate(@Validated @RequestBody EquipHealthRuleTemplateDto dto) {
        ruleTemplateService.saveOrUpdate(dto);
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "报警等级枚举列表")
    @org.springframework.web.bind.annotation.GetMapping("getAlarmLevels")
    public JsonResponseEntity<List<AlarmLevelItem>> getAlarmLevels() {
        List<AlarmLevelItem> list = Arrays.stream(AlarmLevelEnum.values())
                .map(e -> new AlarmLevelItem(e.getCode(), e.getDesc()))
                .collect(Collectors.toList());
        return JsonResponseEntity.success(list);
    }

    @Override
    @Operation(summary = "对关联该模板的所有设备进行健康评分")
    @GetMapping("computeByTemplate")
    public JsonResponseEntity<Map<String, Object>> computeByTemplate(@RequestParam String templateId) {
        EquipHealthRuleTemplateDto template = ruleTemplateService.getByIdOrDefault(templateId);
        int periodHours = template.getPeriodHours() != null ? template.getPeriodHours() : 24;
        List<Equip> equips = equipService.listByHealthTemplateId(templateId);
        Date periodEnd = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(periodEnd);
        cal.add(Calendar.HOUR_OF_DAY, -periodHours);
        Date periodStart = cal.getTime();

        int count = 0;
        for (Equip equip : equips) {
            String sn = equip.getSelfCode();
            if (sn == null || sn.isEmpty()) continue;
            EquipHealthComputeQuery query = new EquipHealthComputeQuery();
            query.setSn(sn);
            query.setPeriodStart(periodStart);
            query.setPeriodEnd(periodEnd);
            query.setTemplateId(templateId);
            equipHealthComputeService.computeAndSave(query, equip.getId());
            count++;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("message", "已对 " + count + " 台关联设备完成评分");
        return JsonResponseEntity.success(result);
    }
}
