/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.viewer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.orm.mybatisplus.OrmUtils;
import com.ourexists.mesedge.device.core.EquipAttrRealtime;
import com.ourexists.mesedge.device.core.EquipRealtime;
import com.ourexists.mesedge.device.core.EquipRealtimeManager;
import com.ourexists.mesedge.device.feign.EquipAttrFeign;
import com.ourexists.mesedge.device.model.EquipAttrBatchDto;
import com.ourexists.mesedge.device.model.EquipAttrDto;
import com.ourexists.mesedge.device.model.EquipAttrPageQuery;
import com.ourexists.mesedge.device.pojo.EquipAttr;
import com.ourexists.mesedge.device.service.EquipAttrService;
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
public class EquipAttrViewer implements EquipAttrFeign {

    @Autowired
    private EquipAttrService service;



    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EquipAttrDto>> selectByPage(@RequestBody EquipAttrPageQuery dto) {
        Page<EquipAttr> page = service.selectByPage(dto);
        return JsonResponseEntity.success(EquipAttr.covert(page.getRecords()), OrmUtils.extraPagination(page));
    }

    @Operation(summary = "批量新增", description = "批量新增")
    @PostMapping("insertBatch")
    public JsonResponseEntity<Boolean> insertBatch(@Validated @RequestBody EquipAttrBatchDto dto) {
        service.insertBatch(dto);
        return JsonResponseEntity.success(true);
    }
}
