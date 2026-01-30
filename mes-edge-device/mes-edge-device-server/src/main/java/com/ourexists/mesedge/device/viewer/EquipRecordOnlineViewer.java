/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.device.core.equip.cache.EquipRealtime;
import com.ourexists.mesedge.device.core.equip.cache.EquipRealtimeManager;
import com.ourexists.mesedge.device.feign.EquipRecordOnlineFeign;
import com.ourexists.mesedge.device.model.EquipRecordCountQuery;
import com.ourexists.mesedge.device.model.EquipRecordOnlineDto;
import com.ourexists.mesedge.device.model.EquipRecordOnlinePageQuery;
import com.ourexists.mesedge.device.model.EquipRecordOnlineVo;
import com.ourexists.mesedge.device.pojo.EquipRecordOnline;
import com.ourexists.mesedge.device.service.EquipRecordOnlineService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

//@Tag(name = "配方管理")
//@RestController
//@RequestMapping("/BOM")
@Component
public class EquipRecordOnlineViewer implements EquipRecordOnlineFeign {

    @Autowired
    private EquipRecordOnlineService service;

    @Autowired
    private EquipRealtimeManager realtimeManager;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EquipRecordOnlineVo>> selectByPage(@RequestBody EquipRecordOnlinePageQuery dto) {
        Page<EquipRecordOnline> page = service.selectByPage(dto);
        return JsonResponseEntity.success(EquipRecordOnline.covert(page.getRecords(), EquipRecordOnlineVo.class), OrmUtils.extraPagination(page));
    }

    @Operation(summary = "新增记录", description = "新增记录")
    @PostMapping("add")
    public JsonResponseEntity<Boolean> add(@Validated @RequestBody EquipRecordOnlineDto dto) {
        service.add(dto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "批量新增记录", description = "批量新增记录")
    @PostMapping("addBatch")
    public JsonResponseEntity<Boolean> addBatch(@Validated @RequestBody List<EquipRecordOnlineDto> dtos) {
        for (EquipRecordOnlineDto dto : dtos) {
            service.add(dto);
        }
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.delete(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "统计", description = "统计")
    @PostMapping("countMerging")
    public JsonResponseEntity<List<EquipRecordOnlineVo>> countMerging(@Validated @RequestBody EquipRecordCountQuery query) {
        List<EquipRecordOnlineVo> r = new ArrayList<>();
        EquipRealtime equipRealtime = realtimeManager.get(query.getSn());
        return JsonResponseEntity.success(service.countMerging(equipRealtime, query));
    }
}
