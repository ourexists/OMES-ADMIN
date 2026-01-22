/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.device.feign.EquipRecordAlarmFeign;
import com.ourexists.mesedge.device.model.EquipRecordAlarmDto;
import com.ourexists.mesedge.device.model.EquipRecordAlarmPageQuery;
import com.ourexists.mesedge.device.model.EquipRecordAlarmVo;
import com.ourexists.mesedge.device.model.EquipRecordCountQuery;
import com.ourexists.mesedge.device.pojo.EquipRecordAlarm;
import com.ourexists.mesedge.device.service.EquipRecordAlarmService;
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
public class EquipRecordAlarmViewer implements EquipRecordAlarmFeign {

    @Autowired
    private EquipRecordAlarmService service;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EquipRecordAlarmVo>> selectByPage(@RequestBody EquipRecordAlarmPageQuery dto) {
        Page<EquipRecordAlarm> page = service.selectByPage(dto);
        return JsonResponseEntity.success(EquipRecordAlarm.covert(page.getRecords(), EquipRecordAlarmVo.class), OrmUtils.extraPagination(page));
    }

    @Operation(summary = "新增记录", description = "新增记录")
    @PostMapping("add")
    public JsonResponseEntity<Boolean> add(@Validated @RequestBody EquipRecordAlarmDto dto) {
        service.add(dto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "批量新增记录", description = "批量新增记录")
    @PostMapping("addBatch")
    public JsonResponseEntity<Boolean> addBatch(@Validated @RequestBody List<EquipRecordAlarmDto> dtos) {
        for (EquipRecordAlarmDto dto : dtos) {
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
    public JsonResponseEntity<List<EquipRecordAlarmVo>> countMerging(@Validated @RequestBody EquipRecordCountQuery query) {
        return JsonResponseEntity.success(service.countMerging(query));
    }
}
