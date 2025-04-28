/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.sync.view;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.sync.feign.SyncFeign;
import com.ourexists.mesedge.sync.model.SyncDto;
import com.ourexists.mesedge.sync.model.SyncResourceVo;
import com.ourexists.mesedge.sync.model.SyncVo;
import com.ourexists.mesedge.sync.model.query.SyncPageQuery;
import com.ourexists.mesedge.sync.pojo.Sync;
import com.ourexists.mesedge.sync.pojo.SyncResource;
import com.ourexists.mesedge.sync.service.SyncResourceService;
import com.ourexists.mesedge.sync.service.SyncService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
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


    //    @Operation(summary = "分页查询", description = "分页查询")
//    @PostMapping("selectByPage")
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

    //    @Operation(summary = "id查詢", description = "id查詢")
//    @GetMapping("selectById")
    public JsonResponseEntity<SyncVo> selectById(@RequestParam String id) {
        SyncVo syncVo = Sync.covert(this.service.getById(id));
        syncVo.setResources(SyncResource.covert(syncResourceService.selectBySyncId(id)));
        return JsonResponseEntity.success(syncVo);
    }

    //    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
//    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody SyncDto dto) {
        service.saveOrUpdate(Sync.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    //    @Operation(summary = "删除", description = "删除")
//    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

}
