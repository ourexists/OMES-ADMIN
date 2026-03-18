/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.inspection.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.inspection.feign.InspectTemplateFeign;
import com.ourexists.omes.inspection.model.InspectTemplateDto;
import com.ourexists.omes.inspection.model.InspectTemplatePageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 巡检模板
 */
@Tag(name = "巡检模板")
@RestController
@RequestMapping("/inspection/template")
public class InspectTemplateController {

    @Autowired
    private InspectTemplateFeign feign;

    @Operation(summary = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<InspectTemplateDto>> selectByPage(@RequestBody InspectTemplatePageQuery query) {
        return feign.selectByPage(query);
    }

    @Operation(summary = "新增或修改（可带巡检项）")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectTemplateDto dto) {
        return feign.addOrUpdate(dto);
    }

    @Operation(summary = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }

    @Operation(summary = "根据ID查询")
    @GetMapping("selectById")
    public JsonResponseEntity<InspectTemplateDto> selectById(@RequestParam String id) {
        return feign.selectById(id);
    }

    @Operation(summary = "查询模板及其巡检项")
    @GetMapping("selectWithItems")
    public JsonResponseEntity<InspectTemplateDto> selectWithItems(@RequestParam String templateId) {
        return feign.selectWithItems(templateId);
    }

    @Operation(summary = "模板列表（下拉）")
    @GetMapping("selectList")
    public JsonResponseEntity<List<InspectTemplateDto>> selectList() {
        return feign.selectList();
    }

    @Operation(summary = "巡检项类型枚举")
    @GetMapping("itemTypes")
    public JsonResponseEntity<Map<Integer, String>> itemTypes() {
        return JsonResponseEntity.success(Map.of(
                1, "选择",
                2, "数值",
                3, "是否"
        ));
    }
}
