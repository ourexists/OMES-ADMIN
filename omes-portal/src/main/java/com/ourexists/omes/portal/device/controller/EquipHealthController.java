package com.ourexists.omes.portal.device.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.device.enums.EquipHealthLevelEnum;
import com.ourexists.omes.device.feign.EquipHealthFeign;
import com.ourexists.omes.device.model.*;
import com.ourexists.omes.inspection.feign.InspectRecordFeign;
import com.ourexists.omes.inspection.model.InspectRecordDto;
import com.ourexists.omes.inspection.model.InspectRecordListByEquipPeriodQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "设备健康指标")
@RestController
@RequestMapping("/equipHealth")
public class EquipHealthController {

    @Autowired
    private EquipHealthFeign equipHealthFeign;
    @Autowired
    private InspectRecordFeign inspectRecordFeign;

    @Operation(summary = "计算并保存设备健康指标")
    @PostMapping("computeAndSave")
    public JsonResponseEntity<EquipHealthIndicatorDto> computeAndSave(@Validated @RequestBody EquipHealthComputeQuery query) {
        return equipHealthFeign.computeAndSave(query);
    }

    @Operation(summary = "查询设备最新健康指标（含巡检维度聚合）")
    @GetMapping("getLatestBySn")
    public JsonResponseEntity<EquipHealthIndicatorDto> getLatestBySn(@RequestParam String sn) {
        JsonResponseEntity<EquipHealthIndicatorDto> res = equipHealthFeign.getLatestBySn(sn);
        if (res == null || res.getData() == null) return res;
        EquipHealthIndicatorDto dto = res.getData();
        applyInspectionAggregation(dto, getTemplateById(dto.getTemplateId()));
        return res;
    }

    @Operation(summary = "获取默认健康规则模板")
    @GetMapping("getDefaultTemplate")
    public JsonResponseEntity<EquipHealthRuleTemplateDto> getDefaultTemplate() {
        return equipHealthFeign.getDefaultTemplate();
    }

    @Operation(summary = "按统计时间查询健康指标列表（含巡检维度聚合），statTime 格式 yyyy-MM-dd HH:mm:ss")
    @PostMapping("listByStatTime")
    public JsonResponseEntity<List<EquipHealthIndicatorDto>> listByStatTime(
            @RequestBody EquipHealthIndicatorPageQuery pageQuery) {
        JsonResponseEntity<List<EquipHealthIndicatorDto>> res = equipHealthFeign.listByStatTime(pageQuery);
        if (res == null || CollectionUtils.isEmpty(res.getData())) return res;
        Map<String, EquipHealthRuleTemplateDto> templateMap = buildTemplateIdMap();
        for (EquipHealthIndicatorDto dto : res.getData()) {
            applyInspectionAggregation(dto, templateMap.get(dto != null ? dto.getTemplateId() : null));
        }
        return res;
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

    /**
     * 在 portal 层聚合巡检维度：按模板配置对设备健康分做巡检扣分并重算等级
     */
    private void applyInspectionAggregation(EquipHealthIndicatorDto dto, EquipHealthRuleTemplateDto template) {
        if (dto == null || dto.getEquipId() == null || dto.getPeriodStart() == null || dto.getPeriodEnd() == null) return;
        if (template == null || template.getInspectionWeight() == null || template.getInspectionWeight() <= 0) return;
        if (inspectRecordFeign == null) return;
        double deduction;
        try {
            InspectRecordListByEquipPeriodQuery periodQuery = new InspectRecordListByEquipPeriodQuery()
                    .setEquipId(dto.getEquipId())
                    .setPeriodStart(dto.getPeriodStart())
                    .setPeriodEnd(dto.getPeriodEnd());
            JsonResponseEntity<List<InspectRecordDto>> recRes = inspectRecordFeign.listByEquipIdAndPeriod(periodQuery);
            List<InspectRecordDto> list = (recRes != null && recRes.getData() != null) ? recRes.getData() : new ArrayList<>();
            if (list.isEmpty()) {
                int penalty = template.getInspectionNoRecordPenalty() != null ? template.getInspectionNoRecordPenalty() : 5;
                deduction = Math.min(template.getInspectionWeight(), penalty);
            } else {
                double sum = 0;
                int count = 0;
                for (InspectRecordDto r : list) {
                    if (r.getScore() != null) {
                        sum += r.getScore();
                        count++;
                    }
                }
                if (count == 0) {
                    int penalty = template.getInspectionNoRecordPenalty() != null ? template.getInspectionNoRecordPenalty() : 5;
                    deduction = Math.min(template.getInspectionWeight(), penalty);
                } else {
                    double avgScore = sum / count;
                    deduction = template.getInspectionWeight() * (1 - avgScore / 100.0);
                }
            }
        } catch (Exception e) {
            log.warn("equip health inspection aggregation failed, equipId={}", dto.getEquipId(), e);
            return;
        }
        int newScore = (int) Math.round(Math.max(0, Math.min(100, (dto.getScore() != null ? dto.getScore() : 0) - deduction)));
        dto.setScore(newScore);
        int healthy = template.getHealthyThreshold() != null ? template.getHealthyThreshold() : 85;
        int attention = template.getAttentionThreshold() != null ? template.getAttentionThreshold() : 70;
        int warning = template.getWarningThreshold() != null ? template.getWarningThreshold() : 50;
        dto.setHealthLevel(EquipHealthLevelEnum.fromScore(newScore, healthy, attention, warning).getCode());
    }

    private Map<String, EquipHealthRuleTemplateDto> buildTemplateIdMap() {
        JsonResponseEntity<List<EquipHealthRuleTemplateDto>> res = equipHealthFeign.listTemplates();
        if (res == null || CollectionUtils.isEmpty(res.getData())) return Map.of();
        return res.getData().stream()
                .filter(t -> t.getId() != null)
                .collect(Collectors.toMap(EquipHealthRuleTemplateDto::getId, t -> t, (a, b) -> a));
    }

    private EquipHealthRuleTemplateDto getTemplateById(String templateId) {
        if (templateId == null || templateId.isEmpty()) return null;
        return buildTemplateIdMap().get(templateId);
    }
}
