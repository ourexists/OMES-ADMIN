/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.device.model.EquipStateSnapshotCountDto;
import com.ourexists.mesedge.device.model.EquipStateSnapshotCountQuery;
import com.ourexists.mesedge.device.model.EquipStateSnapshotDto;
import com.ourexists.mesedge.device.model.EquipStateSnapshotPageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface EquipStateSnapshotFeign {

    JsonResponseEntity<List<EquipStateSnapshotDto>> selectByPage(@RequestBody EquipStateSnapshotPageQuery dto);

    //    @Operation(summary = "删除", description = "")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    //    @Operation(summary = "新增记录", description = "新增记录")
//    @PostMapping("add")
    JsonResponseEntity<Boolean> add(@Validated @RequestBody EquipStateSnapshotDto dto);

    //    @Operation(summary = "批量新增记录", description = "批量新增记录")
//    @PostMapping("addBatch")
    JsonResponseEntity<Boolean> addBatch(@Validated @RequestBody List<EquipStateSnapshotDto> dtos);

    JsonResponseEntity<List<EquipStateSnapshotCountDto>> countNumByTime(EquipStateSnapshotCountQuery dto);
}
