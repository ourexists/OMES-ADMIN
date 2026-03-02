/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.mat.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.mat.enums.BOMTypeEnum;
import com.ourexists.mesedge.mat.feign.BOMFeign;
import com.ourexists.mesedge.mat.model.BOMDto;
import com.ourexists.mesedge.mat.model.query.BOMPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "配方管理")
@RestController
@RequestMapping("/BOM")
public class BOMController {

    @Autowired
    private BOMFeign bomFeign;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<BOMDto>> selectByPage(@RequestBody BOMPageQuery dto) {
        return bomFeign.selectByPage(dto);
    }

    @Operation(summary = "通过id查询所有", description = "")
    @GetMapping("selectById")
    public JsonResponseEntity<BOMDto> selectById(@RequestParam String id) {
        return bomFeign.selectById(id);
    }


    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody BOMDto dto) {
        return bomFeign.addOrUpdate(dto);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return bomFeign.delete(idsDto);
    }

    @Operation(summary = "类型", description = "类型")
    @GetMapping("type")
    public JsonResponseEntity<List<MapDto>> type() {
        List<MapDto> r = new ArrayList<>();
        for (BOMTypeEnum value : BOMTypeEnum.values()) {
            r.add(new MapDto().setId(value.getCode().toString()).setName(value.getName()));
        }
        return JsonResponseEntity.success(r);
    }
}
