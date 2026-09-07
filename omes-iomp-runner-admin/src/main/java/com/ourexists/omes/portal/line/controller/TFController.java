/*

 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists

 */



package com.ourexists.omes.portal.line.controller;



import com.ourexists.era.framework.core.model.dto.IdsDto;

import com.ourexists.era.framework.core.model.dto.MapDto;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;

import com.ourexists.omes.line.feign.TFFeign;

import com.ourexists.omes.line.model.TFDto;

import com.ourexists.omes.line.model.TFVo;

import com.ourexists.omes.portal.line.service.TfStepEngineService;

import com.ourexists.omes.sync.enums.StructTypeEnum;

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



    @Autowired

    private TfStepEngineService tfStepEngineService;



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

        tfStepEngineService.applyEngineConfig(dto);

        return tfFeign.addOrUpdate(dto);

    }



    @Operation(summary = "删除", description = "")

    @PostMapping("delete")

    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {

        return tfFeign.delete(idsDto);

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

