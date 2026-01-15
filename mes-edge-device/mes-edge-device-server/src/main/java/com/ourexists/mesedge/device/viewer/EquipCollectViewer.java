/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.device.feign.EquipCollectFeign;
import com.ourexists.mesedge.device.model.EquipCollectDto;
import com.ourexists.mesedge.device.model.EquipCollectPageQuery;
import com.ourexists.mesedge.device.pojo.EquipCollect;
import com.ourexists.mesedge.device.service.EquipCollectService;
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
public class EquipCollectViewer implements EquipCollectFeign {

    @Autowired
    private EquipCollectService service;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EquipCollectDto>> selectByPage(@RequestBody EquipCollectPageQuery dto) {
        Page<EquipCollect> page = service.selectByPage(dto);
        return JsonResponseEntity.success(EquipCollect.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    @Operation(summary = "新增记录", description = "新增记录")
    @PostMapping("save")
    public JsonResponseEntity<Boolean> save(@Validated @RequestBody EquipCollectDto dto) {
        service.save(EquipCollect.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "批量新增记录", description = "批量新增记录")
    @PostMapping("addBatch")
    public JsonResponseEntity<Boolean> addBatch(@Validated @RequestBody List<EquipCollectDto> dtos) {
        service.saveBatch(EquipCollect.wrap(dtos));
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.delete(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }
}
