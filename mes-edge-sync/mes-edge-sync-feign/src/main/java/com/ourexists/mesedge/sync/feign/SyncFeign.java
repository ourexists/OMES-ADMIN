/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.sync.model.SyncDto;
import com.ourexists.mesedge.sync.model.SyncResourceDto;
import com.ourexists.mesedge.sync.model.SyncResourceVo;
import com.ourexists.mesedge.sync.model.SyncVo;
import com.ourexists.mesedge.sync.model.query.SyncPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

//@RequestMapping("/sync")
public interface SyncFeign {

    @Operation(summary = "分页", description = "分页")
    @PostMapping("selectByPage")
    JsonResponseEntity<List<SyncVo>> selectByPage(@RequestBody SyncPageQuery dto);

    @Operation(summary = "通过事务名查询最后一个事务", description = "用过事务名查询最后一个事务")
    @GetMapping("selectLastSyncByTx")
    JsonResponseEntity<SyncVo> selectLastSyncByTx(@RequestParam String syncTx);

    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody SyncDto dto);

    @Operation(summary = "id查詢", description = "id查詢")
    @GetMapping("selectById")
    JsonResponseEntity<SyncVo> selectById(@RequestParam String id);

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    @Operation(summary = "变更状态", description = "变更状态")
    @PostMapping("changeStatus")
    JsonResponseEntity<Boolean> changeStatus(@RequestParam String syncId, @RequestParam String status);

    @Operation(summary = "同步事务中最后一条分支资源", description = "同步事务中最后一条分支资源")
    @PostMapping("lastSyncBranch")
    JsonResponseEntity<SyncResourceVo> lastSyncBranch(@RequestParam String syncId);

    @Operation(summary = "新增或更新资源", description = "新增或更新资源")
    @PostMapping("addOrUpdateResource")
    JsonResponseEntity<Boolean> addOrUpdateResource(@Validated @RequestBody SyncResourceDto syncResourceDto);

    @Operation(summary = "清除历史", description = "清除历史")
    @PostMapping("clearHistory")
    JsonResponseEntity<Boolean> clearHistory(Date date);

    @Operation(summary = "是否存在同步资源", description = "是否存在同步资源")
    @GetMapping("existSyncResource")
    JsonResponseEntity<Boolean> existSyncResource(@RequestParam String syncTx,
                                                  @RequestParam String reqData);

    @Operation(summary = "断点重入", description = "断点重入")
    @GetMapping("breakpointProcess")
    JsonResponseEntity<Boolean> breakpointProcess(@RequestParam String id);

    @Operation(summary = "事务", description = "事务")
    @GetMapping("syncTx")
    JsonResponseEntity<List<MapDto>> syncTx();

    @Operation(summary = "状态", description = "状态")
    @GetMapping("status")
    JsonResponseEntity<List<MapDto>> status();

}
