/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.sync.model.SyncDto;
import com.ourexists.mesedge.sync.model.SyncVo;
import com.ourexists.mesedge.sync.model.query.SyncPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@RequestMapping("/sync")
public interface SyncFeign {

    JsonResponseEntity<List<SyncVo>> selectByPage(@RequestBody SyncPageQuery dto);

//    @Operation(summary = "id查詢", description = "id查詢")
//    @GetMapping("selectById")
    JsonResponseEntity<SyncVo> selectById(@RequestParam String id);

//    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
//    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody SyncDto dto);

//    @Operation(summary = "删除", description = "删除")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);
}
