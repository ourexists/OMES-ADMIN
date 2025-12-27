/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.device.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.device.enums.EquipTypeEnum;
import com.ourexists.mesedge.device.feign.EquipFeign;
import com.ourexists.mesedge.device.model.EquipDto;
import com.ourexists.mesedge.device.model.EquipPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "设备")
@RestController
@Slf4j
@RequestMapping("/equip")
public class EquipController {

    @Autowired
    private EquipFeign feign;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EquipDto>> selectByPage(@RequestBody EquipPageQuery dto) {
        return feign.selectByPage(dto);
    }

    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody EquipDto dto) {
        return feign.addOrUpdate(dto);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }

    @Operation(summary = "通过id查询", description = "通过id查询")
    @GetMapping("selectById")
    public JsonResponseEntity<EquipDto> selectById(@RequestParam String id) {
        return feign.selectById(id);
    }

    @Operation(summary = "设备类型", description = "设备类型")
    @GetMapping("equipType")
    public JsonResponseEntity<List<MapDto>> equipType() {
        List<MapDto> r = new ArrayList<>();
        for (EquipTypeEnum value : EquipTypeEnum.values()) {
            r.add(new MapDto(value.getCode().toString(), value.getDesc()));
        }
        return JsonResponseEntity.success(r);
    }
}
