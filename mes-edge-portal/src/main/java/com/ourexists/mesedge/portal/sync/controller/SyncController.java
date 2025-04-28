/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.sync.controller;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.portal.sync.manager.SyncManager;
import com.ourexists.mesedge.sync.enums.SyncStatusEnum;
import com.ourexists.mesedge.sync.enums.SyncTxEnum;
import com.ourexists.mesedge.sync.feign.SyncFeign;
import com.ourexists.mesedge.sync.model.SyncDto;
import com.ourexists.mesedge.sync.model.SyncVo;
import com.ourexists.mesedge.sync.model.query.SyncPageQuery;
import com.ourexists.mesedge.sync.pojo.Sync;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "数据同步")
@RestController
@RequestMapping("/sync")
public class SyncController {

    @Autowired
    private SyncFeign syncFeign;

    @Autowired
    private List<SyncManager> syncManagers;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<SyncVo>> selectByPage(@RequestBody SyncPageQuery dto) {
        return syncFeign.selectByPage(dto);
    }

    @Operation(summary = "id查詢", description = "id查詢")
    @GetMapping("selectById")
    public JsonResponseEntity<SyncVo> selectById(@RequestParam String id) {
        return syncFeign.selectById(id);
    }

    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody SyncDto dto) {
        return syncFeign.addOrUpdate(dto);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return syncFeign.delete(idsDto);
    }

    @Operation(summary = "断点重入", description = "断点重入")
    @GetMapping("breakpointProcess")
    public JsonResponseEntity<Boolean> breakpointProcess(@RequestParam String id) {
        //说明传入的id有误不处理
        SyncDto sync = null;
        try {
            sync = RemoteHandleUtils.getDataFormResponse(syncFeign.selectById(id));
        } catch (EraCommonException e) {
            throw new BusinessException(e.getMessage());
        }
        if (sync == null) {
            return JsonResponseEntity.success(true);
        }
        for (SyncManager syncManager : syncManagers) {
            if (syncManager.syncTx().equals(sync.getSyncTx())) {
                syncManager.breakpointProcess(Sync.wrap(sync));
            }
        }
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "事务", description = "事务")
    @GetMapping("syncTx")
    public JsonResponseEntity<List<MapDto>> syncTx() {
        List<MapDto> r = new ArrayList<>();
        for (SyncTxEnum value : SyncTxEnum.values()) {
            r.add(new MapDto().setId(value.name()).setName(value.name()));
        }
        return JsonResponseEntity.success(r);
    }

    @Operation(summary = "状态", description = "状态")
    @GetMapping("status")
    public JsonResponseEntity<List<MapDto>> status() {
        List<MapDto> r = new ArrayList<>();
        for (SyncStatusEnum value : SyncStatusEnum.values()) {
            r.add(new MapDto().setId(value.name()).setName(value.name()));
        }
        return JsonResponseEntity.success(r);
    }
}
