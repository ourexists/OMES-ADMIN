/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.line.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.line.enums.LineTypeEnum;
import com.ourexists.mesedge.line.model.LineDto;
import com.ourexists.mesedge.line.model.LineVo;
import com.ourexists.mesedge.line.model.query.LinePageQuery;
import com.ourexists.mesedge.line.feign.LineFeign;
import com.ourexists.mesedge.portal.line.service.LineFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "产线")
@RestController
@RequestMapping("/line")
public class LineController {

    @Autowired
    private LineFeign lineFeign;

    @Autowired
    private LineFlowService lineFlowService;

    @Operation(summary = "分页", description = "")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<LineVo>> selectByPage(@RequestBody LinePageQuery dto) {
        return lineFeign.selectByPage(dto);
    }

    @Operation(summary = "通过code查询所有", description = "")
    @GetMapping("selectByCode")
    public JsonResponseEntity<LineVo> selectByCode(@RequestParam String code) {
        return lineFeign.selectByCode(code);
    }


    @Operation(summary = "通过id查询所有", description = "")
    @GetMapping("selectById")
    public JsonResponseEntity<LineVo> selectById(@RequestParam String id) {
        return lineFeign.selectById(id);
    }

    @Operation(summary = "新增或修改根据id", description = "")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody LineDto dto) {
        return lineFeign.addOrUpdate(dto);
    }

    @Operation(summary = "删除", description = "")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return lineFeign.delete(idsDto);
    }

    @Operation(summary = "s7下载", description = "")
    @GetMapping("downloadS7")
    public JsonResponseEntity<Boolean> downloadS7(@RequestParam String lineId, @RequestParam String serverName) {
        lineFlowService.downloadS7(lineId, serverName);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "类型", description = "")
    @GetMapping("type")
    public JsonResponseEntity<List<MapDto>> type() {
        List<MapDto> r = new ArrayList<>();
        for (LineTypeEnum value : LineTypeEnum.values()) {
            r.add(new MapDto().setId(value.getCode().toString()).setName(value.getName()));
        }
        return JsonResponseEntity.success(r);
    }
}
