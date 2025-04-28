/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.mat.model.MaterialClassifyDto;
import com.ourexists.mesedge.mat.model.query.MaterialClassifyPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//@RequestMapping("/mc")
public interface MCFeign {

    @Operation(summary = "分页", description = "")
    @PostMapping("selectByPage")
    JsonResponseEntity<List<MaterialClassifyDto>> selectByPage(@RequestBody MaterialClassifyPageQuery dto);

    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody MaterialClassifyDto dto);

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);
}
