/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.device.feign.EquipRunRecordFeign;
import com.ourexists.mesedge.device.model.EquipRunRecordDto;
import com.ourexists.mesedge.device.model.EquipRunRecordPageQuery;
import com.ourexists.mesedge.device.pojo.EquipRunRecord;
import com.ourexists.mesedge.device.service.EquipRunRecordService;
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
public class EquipRunRecordViewer implements EquipRunRecordFeign {

    @Autowired
    private EquipRunRecordService service;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EquipRunRecordDto>> selectByPage(@RequestBody EquipRunRecordPageQuery dto) {
        Page<EquipRunRecord> page = service.selectByPage(dto);
        return JsonResponseEntity.success(EquipRunRecord.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }


    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody EquipRunRecordDto dto) {
        service.addOrUpdate(dto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.delete(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }
}
