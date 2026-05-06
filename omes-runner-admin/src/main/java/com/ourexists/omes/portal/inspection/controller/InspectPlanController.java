/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.inspection.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.inspection.feign.InspectPlanFeign;
import com.ourexists.omes.inspection.model.InspectPlanDto;
import com.ourexists.omes.inspection.model.InspectPlanPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 巡检计划
 */
@Tag(name = "巡检计划")
@RestController
@RequestMapping("/inspection/plan")
public class InspectPlanController {

    @Autowired
    private InspectPlanFeign feign;

    @Operation(summary = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<InspectPlanDto>> selectByPage(@RequestBody InspectPlanPageQuery query) {
        return feign.selectByPage(query);
    }

    @Operation(summary = "新增或修改")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectPlanDto dto) {
        return feign.addOrUpdate(dto);
    }

    @Operation(summary = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }

    @Operation(summary = "根据ID查询")
    @GetMapping("selectById")
    public JsonResponseEntity<InspectPlanDto> selectById(@RequestParam String id) {
        return feign.selectById(id);
    }

    @Operation(summary = "启用计划")
    @GetMapping("enable")
    public JsonResponseEntity<Boolean> enable(@RequestParam String planId) {
        return feign.enable(planId);
    }

    @Operation(summary = "停用计划")
    @GetMapping("disable")
    public JsonResponseEntity<Boolean> disable(@RequestParam String planId) {
        return feign.disable(planId);
    }

    @Operation(summary = "手动生成巡检任务")
    @GetMapping("generateTasks")
    public JsonResponseEntity<Boolean> generateTasks(@RequestParam String planId) {
        return feign.generateTasks(planId);
    }

    @Operation(summary = "周期类型枚举")
    @GetMapping("cycleTypes")
    public JsonResponseEntity<Map<Integer, String>> cycleTypes() {
        return JsonResponseEntity.success(Map.of(
                1, "每日",
                2, "每周",
                3, "每月"
        ));
    }
}
