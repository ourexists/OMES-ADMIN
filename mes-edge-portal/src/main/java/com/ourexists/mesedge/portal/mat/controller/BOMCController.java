/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.mat.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.mat.feign.BOMCFeign;
import com.ourexists.mesedge.mat.model.BOMCDto;
import com.ourexists.mesedge.mat.model.BOMTreeNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "配方分类")
@RestController
@RequestMapping("/BOMC")
public class BOMCController {

    @Autowired
    private BOMCFeign feign;

    @Operation(summary = "分类树", description = "分类树")
    @GetMapping("treeClassify")
    public JsonResponseEntity<List<BOMTreeNode>> treeClassify() {
        return feign.treeClassify();
    }

    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody BOMCDto dto) {
        return feign.addOrUpdate(dto);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }
}
