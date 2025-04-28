/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.mat.model.BOMDto;
import com.ourexists.mesedge.mat.model.query.BOMPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@Tag(name = "配方管理")
//@RestController
//@RequestMapping("/BOM")
public interface BOMFeign {

//    @Operation(summary = "分页查询", description = "分页查询")
//    @PostMapping("selectByPage")
    JsonResponseEntity<List<BOMDto>> selectByPage(@RequestBody BOMPageQuery dto);

//    @Operation(summary = "通过id查询所有", description = "")
//    @GetMapping("selectById")
    JsonResponseEntity<BOMDto> selectById(@RequestParam String id);

//    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
//    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody BOMDto dto);

//    @Operation(summary = "删除", description = "删除")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);
}
