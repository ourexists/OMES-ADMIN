package com.ourexists.omes.portal.device.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.device.feign.EquipHealthFeign;
import com.ourexists.omes.device.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "设备健康指标")
@RestController
@RequestMapping("/equipHealth")
public class EquipHealthController {

    @Autowired
    private EquipHealthFeign equipHealthFeign;

    @Operation(summary = "计算并保存设备健康指标")
    @PostMapping("computeAndSave")
    public JsonResponseEntity<EquipHealthIndicatorDto> computeAndSave(@Validated @RequestBody EquipHealthComputeQuery query) {
        return equipHealthFeign.computeAndSave(query);
    }

    @Operation(summary = "查询设备最新健康指标")
    @GetMapping("getLatestBySn")
    public JsonResponseEntity<EquipHealthIndicatorDto> getLatestBySn(@RequestParam String sn) {
        return equipHealthFeign.getLatestBySn(sn);
    }

    @Operation(summary = "获取默认健康规则模板")
    @GetMapping("getDefaultTemplate")
    public JsonResponseEntity<EquipHealthRuleTemplateDto> getDefaultTemplate() {
        return equipHealthFeign.getDefaultTemplate();
    }

    @Operation(summary = "按统计时间查询健康指标列表，statTime 格式 yyyy-MM-dd HH:mm:ss")
    @PostMapping("listByStatTime")
    public JsonResponseEntity<List<EquipHealthIndicatorDto>> listByStatTime(
            @RequestBody EquipHealthIndicatorPageQuery pageQuery) {
        return equipHealthFeign.listByStatTime(pageQuery);
    }

    @Operation(summary = "查询所有健康规则模板")
    @GetMapping("listTemplates")
    public JsonResponseEntity<List<EquipHealthRuleTemplateDto>> listTemplates() {
        return equipHealthFeign.listTemplates();
    }

    @Operation(summary = "保存健康规则模板")
    @PostMapping("saveTemplate")
    public JsonResponseEntity<Boolean> saveTemplate(@Validated @RequestBody EquipHealthRuleTemplateDto dto) {
        return equipHealthFeign.saveTemplate(dto);
    }

    @Operation(summary = "报警等级枚举列表")
    @GetMapping("getAlarmLevels")
    public JsonResponseEntity<List<AlarmLevelItem>> getAlarmLevels() {
        return equipHealthFeign.getAlarmLevels();
    }

    @Operation(summary = "对关联该模板的所有设备进行健康评分")
    @GetMapping("computeByTemplate")
    public JsonResponseEntity<Map<String, Object>> computeByTemplate(@RequestParam String templateId) {
        return equipHealthFeign.computeByTemplate(templateId);
    }
}
