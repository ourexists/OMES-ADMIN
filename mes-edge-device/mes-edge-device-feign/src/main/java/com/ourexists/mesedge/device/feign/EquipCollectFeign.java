/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.device.model.EquipCollectDto;
import com.ourexists.mesedge.device.model.EquipCollectPageQuery;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//@Tag(name = "配方管理")
//@RestController
//@RequestMapping("/BOM")
@Component
public interface EquipCollectFeign  {

//    @Operation(summary = "分页查询", description = "分页查询")
//    @PostMapping("selectByPage")
    JsonResponseEntity<List<EquipCollectDto>> selectByPage(@RequestBody EquipCollectPageQuery dto);

//    @Operation(summary = "新增记录", description = "新增记录")
//    @PostMapping("save")
    JsonResponseEntity<Boolean> save(@Validated @RequestBody EquipCollectDto dto);

//    @Operation(summary = "批量新增记录", description = "批量新增记录")
//    @PostMapping("addBatch")
    JsonResponseEntity<Boolean> addBatch(@Validated @RequestBody List<EquipCollectDto> dtos);

//    @Operation(summary = "删除", description = "删除")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);
}
