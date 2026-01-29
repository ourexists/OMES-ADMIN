/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.device.feign.WorkshopCollectFeign;
import com.ourexists.mesedge.device.model.WorkshopCollectDto;
import com.ourexists.mesedge.device.model.WorkshopCollectPageQuery;
import com.ourexists.mesedge.device.pojo.WorkshopCollect;
import com.ourexists.mesedge.device.service.WorkshopCollectService;
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
public class WorkshopCollectViewer implements WorkshopCollectFeign {

    @Autowired
    private WorkshopCollectService service;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<WorkshopCollectDto>> selectByPage(@RequestBody WorkshopCollectPageQuery dto) {
        Page<WorkshopCollect> page = service.selectByPage(dto);
        return JsonResponseEntity.success(WorkshopCollect.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    @Operation(summary = "新增记录", description = "新增记录")
    @PostMapping("save")
    public JsonResponseEntity<Boolean> save(@Validated @RequestBody WorkshopCollectDto dto) {
        service.save(WorkshopCollect.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "批量新增记录", description = "批量新增记录")
    @PostMapping("addBatch")
    public JsonResponseEntity<Boolean> addBatch(@Validated @RequestBody List<WorkshopCollectDto> dtos) {
        service.saveBatch(WorkshopCollect.wrap(dtos));
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.delete(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }
}
