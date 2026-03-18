/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.omes.inspection.feign.InspectTaskFeign;
import com.ourexists.omes.inspection.model.InspectTaskDto;
import com.ourexists.omes.inspection.model.InspectTaskPageQuery;
import com.ourexists.omes.inspection.pojo.InspectPlan;
import com.ourexists.omes.inspection.pojo.InspectTask;
import com.ourexists.omes.inspection.pojo.InspectTemplate;
import com.ourexists.omes.inspection.service.InspectPlanService;
import com.ourexists.omes.inspection.service.InspectTaskService;
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
public class InspectTaskViewer implements InspectTaskFeign {

    @Autowired
    private InspectTaskService service;
    @Autowired
    private InspectPlanService inspectPlanService;
    @Autowired
    private InspectTemplateService templateService;

    @Override
    @Operation(summary = "分页查询巡检任务")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<InspectTaskDto>> selectByPage(@RequestBody InspectTaskPageQuery query) {
        Page<InspectTask> page = service.selectByPage(query);
        List<InspectTaskDto> list = InspectTask.covert(page.getRecords());
        if (!list.isEmpty()) {
            List<String> planIds = list.stream().map(InspectTaskDto::getPlanId).filter(id -> id != null && !id.isBlank()).distinct().toList();
            if (!planIds.isEmpty()) {
                Map<String, String> planIdToName = inspectPlanService.listByIds(planIds).stream()
                        .collect(Collectors.toMap(InspectPlan::getId, p -> p.getName() != null ? p.getName() : "", (a, b) -> a));
                for (InspectTaskDto dto : list) {
                    if (dto.getPlanId() != null) {
                        dto.setPlanName(planIdToName.getOrDefault(dto.getPlanId(), ""));
                    }
                }
            }
            List<String> templateIds = list.stream().map(InspectTaskDto::getTemplateId).filter(id -> id != null && !id.isBlank()).distinct().toList();
            if (!templateIds.isEmpty()) {
                Map<String, String> templateIdToName = templateService.listByIds(templateIds).stream()
                        .collect(Collectors.toMap(InspectTemplate::getId, t -> t.getName() != null ? t.getName() : "", (a, b) -> a));
                for (InspectTaskDto dto : list) {
                    if (dto.getTemplateId() != null) {
                        dto.setTemplateName(templateIdToName.getOrDefault(dto.getTemplateId(), ""));
                    }
                }
            }
        }
        return JsonResponseEntity.success(list, OrmUtils.extraPagination(page));
    }

    @Override
    @Operation(summary = "新增或修改巡检任务")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectTaskDto dto) {
        service.saveOrUpdate(InspectTask.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "删除巡检任务")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "根据ID查询巡检任务")
    @GetMapping("selectById")
    public JsonResponseEntity<InspectTaskDto> selectById(@RequestParam String id) {
        InspectTask task = service.getById(id);
        InspectTaskDto dto = InspectTask.covert(task);
        if (dto != null) {
            if (dto.getPlanId() != null) {
                InspectPlan plan = inspectPlanService.getById(dto.getPlanId());
                if (plan != null) dto.setPlanName(plan.getName());
            }
            if (dto.getTemplateId() != null) {
                InspectTemplate t = templateService.getById(dto.getTemplateId());
                if (t != null) dto.setTemplateName(t.getName());
            }
        }
        return JsonResponseEntity.success(dto);
    }

    @Override
    @Operation(summary = "按计划ID查询任务列表")
    @GetMapping("listByPlanId")
    public JsonResponseEntity<List<InspectTaskDto>> listByPlanId(@RequestParam String planId) {
        return JsonResponseEntity.success(InspectTask.covert(service.listByPlanId(planId)));
    }

    @Override
    @Operation(summary = "重新开始：将已逾期的任务重置为待执行")
    @PostMapping("restartOverdue")
    public JsonResponseEntity<Boolean> restartOverdue(@Validated @RequestBody IdsDto idsDto) {
        int n = service.restartOverdue(idsDto != null ? idsDto.getIds() : null);
        return JsonResponseEntity.success(n > 0);
    }
}
