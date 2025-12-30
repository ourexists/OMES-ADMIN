/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.device.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.device.feign.EquipAttrFeign;
import com.ourexists.mesedge.device.model.EquipAttrBatchDto;
import com.ourexists.mesedge.device.model.EquipAttrDto;
import com.ourexists.mesedge.device.model.EquipAttrPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "设备属性")
@RestController
@RequestMapping("/equipattr")
public class EquipAttrController {

    @Autowired
    private EquipAttrFeign equipAttrFeign;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EquipAttrDto>> selectByPage(@RequestBody EquipAttrPageQuery dto) {
        return equipAttrFeign.selectByPage(dto);
    }

    @Operation(summary = "批量新增", description = "批量新增")
    @PostMapping("insertBatch")
    public JsonResponseEntity<Boolean> insertBatch(@Validated @RequestBody EquipAttrBatchDto dto) {
        return equipAttrFeign.insertBatch(dto);
    }

}
