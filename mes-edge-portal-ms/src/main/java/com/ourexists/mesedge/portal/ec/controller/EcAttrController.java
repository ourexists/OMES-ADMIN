/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.ec.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.ec.feign.EcAttrFeign;
import com.ourexists.mesedge.ec.model.EcAttrBatchDto;
import com.ourexists.mesedge.ec.model.EcAttrDto;
import com.ourexists.mesedge.ec.model.EcAttrPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "能耗属性")
@RestController
@RequestMapping("/ecattr")
public class EcAttrController {

    @Autowired
    private EcAttrFeign ecAttrFeign;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EcAttrDto>> selectByPage(@RequestBody EcAttrPageQuery dto) {
        return ecAttrFeign.selectByPage(dto);
    }

    @Operation(summary = "新增或修改根据id", description = "")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody EcAttrDto dto) {
        return ecAttrFeign.addOrUpdate(dto);
    }

    @Operation(summary = "删除", description = "")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return ecAttrFeign.delete(idsDto);
    }

    @Operation(summary = "批量新增", description = "批量新增")
    @PostMapping("insertBatch")
    public JsonResponseEntity<Boolean> insertBatch(@Validated @RequestBody EcAttrBatchDto dto) {
        return ecAttrFeign.insertBatch(dto);
    }

}
