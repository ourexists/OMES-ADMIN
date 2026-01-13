/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.device.feign.EquipRecordRunFeign;
import com.ourexists.mesedge.device.model.EquipRecordRunDto;
import com.ourexists.mesedge.device.model.EquipRecordRunPageQuery;
import com.ourexists.mesedge.device.model.EquipRecordRunVo;
import com.ourexists.mesedge.device.pojo.EquipRecordRun;
import com.ourexists.mesedge.device.service.EquipRecordRunService;
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
public class EquipRecordRunViewer implements EquipRecordRunFeign {

    @Autowired
    private EquipRecordRunService service;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EquipRecordRunVo>> selectByPage(@RequestBody EquipRecordRunPageQuery dto) {
        Page<EquipRecordRun> page = service.selectByPage(dto);
        return JsonResponseEntity.success(EquipRecordRun.covert(page.getRecords(), EquipRecordRunVo.class), OrmUtils.extraPagination(page));
    }

    @Operation(summary = "新增记录", description = "新增记录")
    @PostMapping("add")
    public JsonResponseEntity<Boolean> add(@Validated @RequestBody EquipRecordRunDto dto) {
        service.add(dto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "批量新增记录", description = "批量新增记录")
    @PostMapping("addBatch")
    public JsonResponseEntity<Boolean> addBatch(@Validated @RequestBody List<EquipRecordRunDto> dtos) {
        for (EquipRecordRunDto dto : dtos) {
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
}
