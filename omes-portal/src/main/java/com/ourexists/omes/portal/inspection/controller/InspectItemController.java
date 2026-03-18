/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.inspection.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.inspection.feign.InspectItemFeign;
import com.ourexists.omes.inspection.model.InspectItemDto;
import com.ourexists.omes.inspection.model.InspectItemPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 巡检项（独立模块）
 */
@Tag(name = "巡检项")
@RestController
@RequestMapping("/inspection/item")
public class InspectItemController {

    @Autowired
    private InspectItemFeign feign;

    @Operation(summary = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<InspectItemDto>> selectByPage(@RequestBody InspectItemPageQuery query) {
        return feign.selectByPage(query);
    }

    @Operation(summary = "新增或修改")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody InspectItemDto dto) {
        return feign.addOrUpdate(dto);
    }

    @Operation(summary = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }

    @Operation(summary = "根据ID查询")
    @GetMapping("selectById")
    public JsonResponseEntity<InspectItemDto> selectById(@RequestParam String id) {
        return feign.selectById(id);
    }

//    @Operation(summary = "根据模板ID查询列表")
//    @GetMapping("listByTemplateId")
//    public JsonResponseEntity<List<InspectItemDto>> listByTemplateId(@RequestParam String templateId) {
//        return feign.listByTemplateId(templateId);
//    }

    @Operation(summary = "公共库巡检项列表（供模板「从巡检项载入」使用）")
    @GetMapping("listAllPool")
    public JsonResponseEntity<List<InspectItemDto>> listAllPool() {
        return feign.listAllPool();
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
