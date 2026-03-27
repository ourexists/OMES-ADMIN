/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.line.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.line.feign.TFEdgeFeign;
import com.ourexists.omes.line.model.TFEdgeSaveDto;
import com.ourexists.omes.line.model.TFEdgeVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "TF 边关系")
@RestController
@RequestMapping("/tfEdge")
public class TFEdgeController {

    @Autowired
    private TFEdgeFeign tfEdgeFeign;

    @Operation(summary = "根据产线id查询边关系", description = "")
    @GetMapping("selectByLineId")
    public JsonResponseEntity<List<TFEdgeVo>> selectByLineId(@RequestParam String lineId) {
        return tfEdgeFeign.selectByLineId(lineId);
    }

    @Operation(summary = "保存整张边关系图(以 lineId 为粒度)", description = "")
    @PostMapping("saveByLineId")
    public JsonResponseEntity<Boolean> saveByLineId(@Validated @RequestBody TFEdgeSaveDto dto) {
        return tfEdgeFeign.saveByLineId(dto);
    }
}

