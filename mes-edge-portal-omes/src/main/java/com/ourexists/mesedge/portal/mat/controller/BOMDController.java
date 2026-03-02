/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.mat.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.mat.enums.IngredientAttributeEnum;
import com.ourexists.mesedge.mat.feign.BOMDFeign;
import com.ourexists.mesedge.mat.model.BOMDDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "配方详情")
@RestController
@RequestMapping("/BOMD")
public class BOMDController {

    @Autowired
    private BOMDFeign feign;

    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody BOMDDto dto) {
        return feign.addOrUpdate(dto);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }

    @Operation(summary = "成分属性", description = "成分属性")
    @GetMapping("attribute")
    public JsonResponseEntity<List<MapDto>> attribute() {
        List<MapDto> r = new ArrayList<>();
        for (IngredientAttributeEnum value : IngredientAttributeEnum.values()) {
            if (value.equals(IngredientAttributeEnum.UNKNOWN)) {
                continue;
            }
            r.add(new MapDto().setId(value.getCode().toString()).setName(value.getName()));
        }
        return JsonResponseEntity.success(r);
    }
}
