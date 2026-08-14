/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.device.model.DevgDto;
import com.ourexists.omes.device.model.DevgPageQuery;
import com.ourexists.omes.device.model.DgEquipBindDto;
import com.ourexists.omes.device.model.DgEquipProcessDto;
import com.ourexists.omes.device.model.EquipDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@RequestMapping("/mat")
public interface DevgFeign {

    //    @Operation(summary = "分页", description = "")
//    @PostMapping("selectByPage")
    JsonResponseEntity<List<DevgDto>> selectByPage(@RequestBody DevgPageQuery dto);

    //    @Operation(summary = "新增或修改根据id", description = "")
//    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody DevgDto dto);

    //    @Operation(summary = "删除", description = "")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    JsonResponseEntity<List<DevgDto>> selectByCodes(@RequestBody IdsDto idsDto);

    JsonResponseEntity<List<EquipDto>> listEquips(@RequestParam String dgId);

    JsonResponseEntity<Boolean> bindEquips(@Validated @RequestBody DgEquipBindDto dto);

    JsonResponseEntity<Boolean> unbindEquips(@Validated @RequestBody DgEquipBindDto dto);

    JsonResponseEntity<Boolean> saveEquipProcess(@Validated @RequestBody DgEquipProcessDto dto);
}
