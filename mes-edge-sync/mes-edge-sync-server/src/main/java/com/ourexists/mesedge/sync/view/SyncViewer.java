/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.view;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.sync.enums.SyncTxEnum;
import com.ourexists.mesedge.sync.feign.SyncFeign;
import com.ourexists.mesedge.sync.model.SyncDto;
import com.ourexists.mesedge.sync.model.SyncResourceDto;
import com.ourexists.mesedge.sync.model.SyncResourceVo;
import com.ourexists.mesedge.sync.model.SyncVo;
import com.ourexists.mesedge.sync.model.query.SyncPageQuery;
import com.ourexists.mesedge.sync.pojo.Sync;
import com.ourexists.mesedge.sync.pojo.SyncResource;
import com.ourexists.mesedge.sync.service.SyncResourceService;
import com.ourexists.mesedge.sync.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

//@Tag(name = "数据同步")
//@RestController
//@RequestMapping("/sync")
@Component
public class SyncViewer implements SyncFeign {

    @Autowired
    private SyncService service;

    @Autowired
    private SyncResourceService syncResourceService;


    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<SyncVo>> selectByPage(@RequestBody SyncPageQuery dto) {
        Page<Sync> page = service.selectByPage(dto);
        List<SyncVo> syncVos = Sync.covert(page.getRecords());
        if (dto.getQueryResource() && CollectionUtils.isNotEmpty(syncVos)) {
            List<String> ids = syncVos.stream().map(SyncVo::getId).collect(Collectors.toList());
            List<SyncResource> syncResources = syncResourceService.selectBySyncId(ids);
            if (CollectionUtils.isNotEmpty(syncResources)) {
                for (SyncVo syncVo : syncVos) {
                    List<SyncResourceVo> syncDVos = new ArrayList<>();
                    for (SyncResource syncResource : syncResources) {
                        if (syncResource.getSyncId().equals(syncVo.getId())) {
                            syncDVos.add(SyncResource.covert(syncResource));
                        }
                    }
                    syncVo.setResources(syncDVos);
                }
            }
        }
        return JsonResponseEntity.success(syncVos, OrmUtils.extraPagination(page));
    }

    @Operation(summary = "用过事务名查询最后一个事务", description = "用过事务名查询最后一个事务")
    @GetMapping("selectLastSyncByTx")
    public JsonResponseEntity<SyncVo> selectLastSyncByTx(@RequestParam String syncTx) {
        return JsonResponseEntity.success(Sync.covert(service.selectLastSync(syncTx)));
    }

    @Operation(summary = "id查詢", description = "id查詢")
    @GetMapping("selectById")
    public JsonResponseEntity<SyncVo> selectById(@RequestParam String id) {
        SyncVo syncVo = Sync.covert(this.service.getById(id));
        syncVo.setResources(SyncResource.covert(syncResourceService.selectBySyncId(id)));
        return JsonResponseEntity.success(syncVo);
    }

    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody SyncDto dto) {
        service.saveOrUpdate(Sync.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "变更状态", description = "变更状态")
    @GetMapping("changeStatus")
    public JsonResponseEntity<Boolean> changeStatus(@RequestParam String syncId, @RequestParam String status) {
        service.updateStatus(syncId, status);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "同步事务中最后一条分支资源", description = "同步事务中最后一条分支资源")
    @PostMapping("lastSyncBranch")
    public JsonResponseEntity<SyncResourceVo> lastSyncBranch(@RequestParam String syncId) {
        return JsonResponseEntity.success(SyncResource.covert(syncResourceService.getLastFlow(syncId)));
    }

    @Operation(summary = "新增或更新资源", description = "新增或更新资源")
    @PostMapping("addOrUpdateResource")
    public JsonResponseEntity<Boolean> addOrUpdateResource(@Validated @RequestBody SyncResourceDto syncResourceDto) {
        syncResourceService.saveOrUpdate(SyncResource.wrap(syncResourceDto));
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "清除历史", description = "清除历史")
    @PostMapping("clearHistory")
    public JsonResponseEntity<Boolean> clearHistory(Date date) {
        service.clearHistory(date);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "是否存在同步资源", description = "是否存在同步资源")
    @GetMapping("existSyncResource")
    public JsonResponseEntity<Boolean> existSyncResource(@RequestParam String syncTx,
                                                         @RequestParam String reqData) {
        return JsonResponseEntity.success(syncResourceService.existSync(SyncTxEnum.valueOf(syncTx), reqData));
    }

}
