/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.device.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.device.feign.DevgFeign;
import com.ourexists.omes.device.model.DevgDto;
import com.ourexists.omes.device.model.DevgPageQuery;
import com.ourexists.omes.device.model.DgEquipBindDto;
import com.ourexists.omes.device.model.DgEquipProcessDto;
import com.ourexists.omes.device.model.EquipDto;
import com.ourexists.omes.portal.device.support.EquipMaterialNameFiller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "设备能力")
@RestController
@RequestMapping("/devg")
public class DevgController {

    @Autowired
    private DevgFeign devgFeign;

    @Autowired
    private EquipMaterialNameFiller equipMaterialNameFiller;

    @Operation(summary = "分页", description = "")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<DevgDto>> selectByPage(@RequestBody DevgPageQuery dto) {
        return devgFeign.selectByPage(dto);
    }

    @Operation(summary = "新增或修改根据id", description = "")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody DevgDto materialDto) {
        return devgFeign.addOrUpdate(materialDto);
    }

    @Operation(summary = "删除", description = "")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return devgFeign.delete(idsDto);
    }

    @Operation(summary = "查询能力绑定设备", description = "返回设备及其加工原料与对应容量")
    @GetMapping("equips")
    public JsonResponseEntity<List<EquipDto>> listEquips(@RequestParam String dgId) {
        JsonResponseEntity<List<EquipDto>> response = devgFeign.listEquips(dgId);
        if (response != null) {
            equipMaterialNameFiller.fill(response.getData());
        }
        return response;
    }

    @Operation(summary = "绑定设备", description = "从设备管理选择设备加入能力方案")
    @PostMapping("bindEquips")
    public JsonResponseEntity<Boolean> bindEquips(@Validated @RequestBody DgEquipBindDto dto) {
        return devgFeign.bindEquips(dto);
    }

    @Operation(summary = "解绑设备", description = "从能力方案中移除设备，不删除设备档案")
    @PostMapping("unbindEquips")
    public JsonResponseEntity<Boolean> unbindEquips(@Validated @RequestBody DgEquipBindDto dto) {
        return devgFeign.unbindEquips(dto);
    }

    @Operation(summary = "保存设备原料容量", description = "按原料分别保存本能力方案下的容量")
    @PostMapping("saveEquipProcess")
    public JsonResponseEntity<Boolean> saveEquipProcess(@Validated @RequestBody DgEquipProcessDto dto) {
        return devgFeign.saveEquipProcess(dto);
    }

}
