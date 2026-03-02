/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.mps.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.mat.enums.IngredientAttributeEnum;
import com.ourexists.mesedge.mps.feign.MPSDetailFeign;
import com.ourexists.mesedge.mps.model.MPSDetailDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "成分")
@RestController
@RequestMapping("/mps_detail")
public class MPSDetailController {

    @Autowired
    private MPSDetailFeign feign;

    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody MPSDetailDto dto) {
        return feign.addOrUpdate(dto);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }

    @Operation(summary = "成分性质", description = "成分性质")
    @GetMapping("ingredientAttribute")
    public JsonResponseEntity<List<MapDto>> ingredientAttribute() {
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
