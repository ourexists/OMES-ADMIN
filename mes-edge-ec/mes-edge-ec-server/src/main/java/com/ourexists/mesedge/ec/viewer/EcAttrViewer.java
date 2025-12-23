/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ec.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.ec.feign.EcAttrFeign;
import com.ourexists.mesedge.ec.model.EcAttrBatchDto;
import com.ourexists.mesedge.ec.model.EcAttrDto;
import com.ourexists.mesedge.ec.model.EcAttrPageQuery;
import com.ourexists.mesedge.ec.pojo.EcAttr;
import com.ourexists.mesedge.ec.service.EcAttrService;
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
public class EcAttrViewer implements EcAttrFeign {

    @Autowired
    private EcAttrService service;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EcAttrDto>> selectByPage(@RequestBody EcAttrPageQuery dto) {
        Page<EcAttr> page = service.selectByPage(dto);
        return JsonResponseEntity.success(EcAttr.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }


    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody EcAttrDto dto) {
        service.addOrUpdate(dto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        service.delete(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "批量新增", description = "批量新增")
    @PostMapping("insertBatch")
    public JsonResponseEntity<Boolean> insertBatch(@Validated @RequestBody EcAttrBatchDto dto) {
        service.insertBatch(dto);
        return JsonResponseEntity.success(true);
    }
}
