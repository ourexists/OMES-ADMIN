/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.line.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.line.enums.TFTypeEnum;
import com.ourexists.mesedge.line.feign.TFFeign;
import com.ourexists.mesedge.line.model.ChangePriorityDto;
import com.ourexists.mesedge.line.model.TFDto;
import com.ourexists.mesedge.line.model.TFVo;
import com.ourexists.mesedge.sync.enums.StructTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "工艺流程")
@RestController
@RequestMapping("/tf")
public class TFController {

    @Autowired
    private TFFeign tfFeign;

    @Operation(summary = "根据产线id查询", description = "")
    @GetMapping("selectByLineId")
    public JsonResponseEntity<List<TFVo>> selectByLineId(@RequestParam String lineId) {
        return tfFeign.selectByLineId(lineId);
    }

    @Operation(summary = "通过id查询", description = "")
    @GetMapping("selectById")
    public JsonResponseEntity<TFVo> selectById(@RequestParam String id) {
        return tfFeign.selectById(id);
    }

    @Operation(summary = "新增或修改根据id", description = "")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody TFDto dto) {
        return tfFeign.addOrUpdate(dto);
    }


    @Operation(summary = "删除", description = "")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return tfFeign.delete(idsDto);
    }

    @Operation(summary = "改变优先级", description = "改变优先级")
    @PostMapping("changePriority")
    public JsonResponseEntity<Boolean> changePriority(@Validated @RequestBody ChangePriorityDto dto) {
        return tfFeign.changePriority(dto);
    }

    @Operation(summary = "类型", description = "")
    @GetMapping("type")
    public JsonResponseEntity<List<MapDto>> type() {
        List<MapDto> r = new ArrayList<>();
        for (TFTypeEnum value : TFTypeEnum.values()) {
            r.add(new MapDto().setId(value.getCode().toString()).setName(value.getName()));
        }
        return JsonResponseEntity.success(r);
    }

    @Operation(summary = "结构类型", description = "")
    @GetMapping("structType")
    public JsonResponseEntity<List<MapDto>> structType() {
        List<MapDto> r = new ArrayList<>();
        for (StructTypeEnum value : StructTypeEnum.values()) {
            r.add(new MapDto().setId(value.name()).setName(value.name()));
        }
        return JsonResponseEntity.success(r);
    }
}
