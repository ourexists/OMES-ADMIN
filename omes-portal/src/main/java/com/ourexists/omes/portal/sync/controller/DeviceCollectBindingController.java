/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.sync.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.sync.feign.DeviceCollectBindingFeign;
import com.ourexists.omes.sync.model.DeviceCollectBindingDto;
import com.ourexists.omes.sync.model.query.DeviceCollectBindingPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "设备采集绑定")
@RestController
@RequestMapping("/deviceCollectBinding")
public class DeviceCollectBindingController {

    @Autowired
    private DeviceCollectBindingFeign deviceCollectBindingFeign;

    @PostMapping("selectByPage")
    @Operation(summary = "分页查询")
    public JsonResponseEntity<List<DeviceCollectBindingDto>> selectByPage(@RequestBody DeviceCollectBindingPageQuery query) {
        return deviceCollectBindingFeign.selectByPage(query);
    }

    @GetMapping("selectById")
    @Operation(summary = "按ID查询")
    public JsonResponseEntity<DeviceCollectBindingDto> selectById(@RequestParam String id) {
        return deviceCollectBindingFeign.selectById(id);
    }

    @PostMapping("addOrUpdate")
    @Operation(summary = "新增或修改")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody DeviceCollectBindingDto dto) {
        return deviceCollectBindingFeign.addOrUpdate(dto);
    }

    @PostMapping("delete")
    @Operation(summary = "删除")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return deviceCollectBindingFeign.delete(idsDto);
    }
}
