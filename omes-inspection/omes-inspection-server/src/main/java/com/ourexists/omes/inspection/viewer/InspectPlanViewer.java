/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.omes.inspection.feign.InspectPlanFeign;
import com.ourexists.omes.inspection.model.InspectPlanDto;
import com.ourexists.omes.inspection.model.InspectPlanPageQuery;
import com.ourexists.omes.inspection.pojo.InspectPlan;
import com.ourexists.omes.inspection.pojo.InspectTemplate;
import com.ourexists.omes.inspection.service.InspectPlanService;
import com.ourexists.omes.inspection.service.InspectTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InspectPlanViewer implements InspectPlanFeign {

    @Autowired
    private InspectPlanService service;
    @Autowired
    private InspectTemplateService templateService;

    @Override
    @Operation(summary = "分页查询巡检计划")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<InspectPlanDto>> selectByPage(@RequestBody InspectPlanPageQuery query) {
        Page<InspectPlan> page = service.selectByPage(query);
        List<InspectPlanDto> list = InspectPlan.covert(page.getRecords());
        fillTemplateNames(list);
        return JsonResponseEntity.success(list, OrmUtils.extraPagination(page));
    }

    private void fillTemplateNames(List<InspectPlanDto> list) {
        if (list == null || list.isEmpty()) return;
        List<String> templateIds = list.stream()
                .map(InspectPlanDto::getTemplateId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (templateIds.isEmpty()) return;
        Map<String, String> idToName = templateService.listByIds(templateIds).stream()
                .collect(Collectors.toMap(InspectTemplate::getId, t -> t.getName() != null ? t.getName() : "", (a, b) -> a));
        for (InspectPlanDto dto : list) {
            if (dto.getTemplateId() != null) {
                dto.setTemplateName(idToName.getOrDefault(dto.getTemplateId(), ""));
            }
        }
    }

    @Override
    @Operation(summary = "新增或修改巡检计划")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectPlanDto dto) {
        if (dto.getId() != null) {
            InspectPlan existing = service.getById(dto.getId());
            if (existing != null && existing.getStatus() != null && existing.getStatus() == 1) {
                throw new IllegalStateException("启用中的计划不可编辑，请先停用");
            }
        }
        InspectPlan plan = InspectPlan.wrap(dto);
        if (plan.getTemplateId() != null && plan.getTemplateId().trim().isEmpty()) {
            plan.setTemplateId(null);
        }
        if (dto.getStatus() == null) {
            if (dto.getId() != null) {
                InspectPlan existing = service.getById(dto.getId());
                if (existing != null) plan.setStatus(existing.getStatus());
            } else {
                plan.setStatus(0); // 新增默认禁用
            }
        }
        service.saveOrUpdate(plan);
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "删除巡检计划")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "根据ID查询巡检计划")
    @GetMapping("selectById")
    public JsonResponseEntity<InspectPlanDto> selectById(@RequestParam String id) {
        InspectPlanDto dto = InspectPlan.covert(service.getById(id));
        if (dto != null && dto.getTemplateId() != null) {
            InspectTemplate t = templateService.getById(dto.getTemplateId());
            if (t != null) dto.setTemplateName(t.getName());
        }
        return JsonResponseEntity.success(dto);
    }

    @Override
    @Operation(summary = "启用巡检计划")
    @GetMapping("enable")
    public JsonResponseEntity<Boolean> enable(@RequestParam String planId) {
        service.enable(planId);
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "停用巡检计划")
    @GetMapping("disable")
    public JsonResponseEntity<Boolean> disable(@RequestParam String planId) {
        service.disable(planId);
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "手动生成巡检任务")
    @GetMapping("generateTasks")
    public JsonResponseEntity<Boolean> generateTasks(@RequestParam String planId) {
        service.generateTasks(planId);
        return JsonResponseEntity.success(true);
    }
}
