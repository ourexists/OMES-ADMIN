/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.mat.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.mat.feign.MCFeign;
import com.ourexists.mesedge.mat.model.MaterialClassifyDto;
import com.ourexists.mesedge.mat.model.query.MaterialClassifyPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "物料分类")
@RestController
@RequestMapping("/mc")
public class MCController {

    @Autowired
    private MCFeign mcFeign;

    @Operation(summary = "分页", description = "")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<MaterialClassifyDto>> selectByPage(@RequestBody MaterialClassifyPageQuery dto) {
        return mcFeign.selectByPage(dto);
    }

    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody MaterialClassifyDto dto) {
        return mcFeign.addOrUpdate(dto);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return mcFeign.delete(idsDto);
    }

}
