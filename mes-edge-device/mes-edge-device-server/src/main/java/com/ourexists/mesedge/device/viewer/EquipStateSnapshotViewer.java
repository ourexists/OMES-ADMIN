/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.device.feign.EquipStateSnapshotFeign;
import com.ourexists.mesedge.device.model.EquipStateSnapshotCountDto;
import com.ourexists.mesedge.device.model.EquipStateSnapshotCountQuery;
import com.ourexists.mesedge.device.model.EquipStateSnapshotDto;
import com.ourexists.mesedge.device.model.EquipStateSnapshotPageQuery;
import com.ourexists.mesedge.device.pojo.EquipStateSnapshot;
import com.ourexists.mesedge.device.service.EquipStateSnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//@Tag(name = "配方管理")
//@RestController
//@RequestMapping("/BOM")
@Component
public class EquipStateSnapshotViewer implements EquipStateSnapshotFeign {

    @Autowired
    private EquipStateSnapshotService service;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EquipStateSnapshotDto>> selectByPage(@RequestBody EquipStateSnapshotPageQuery dto) {
        Page<EquipStateSnapshot> page = service.selectByPage(dto);
        return JsonResponseEntity.success(EquipStateSnapshot.covert(page.getRecords(), EquipStateSnapshotDto.class), OrmUtils.extraPagination(page));
    }

    @Operation(summary = "新增记录", description = "新增记录")
    @PostMapping("add")
    public JsonResponseEntity<Boolean> add(@Validated @RequestBody EquipStateSnapshotDto dto) {
        service.add(dto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "批量新增记录", description = "批量新增记录")
    @PostMapping("addBatch")
    public JsonResponseEntity<Boolean> addBatch(@Validated @RequestBody List<EquipStateSnapshotDto> dtos) {
        service.addBatch(dtos);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "通过时间段统计设备", description = "通过时间段统计设备")
    @PostMapping("countNumByTime")
    public JsonResponseEntity<List<EquipStateSnapshotCountDto>> countNumByTime(EquipStateSnapshotCountQuery dto) {

        return JsonResponseEntity.success(service.countNumByTime(dto));
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.delete(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }
}
