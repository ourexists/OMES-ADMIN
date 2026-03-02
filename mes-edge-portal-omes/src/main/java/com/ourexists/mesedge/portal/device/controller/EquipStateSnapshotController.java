/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.device.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.device.feign.EquipStateSnapshotFeign;
import com.ourexists.mesedge.device.model.EquipStateSnapshotCountDto;
import com.ourexists.mesedge.device.model.EquipStateSnapshotCountQuery;
import com.ourexists.mesedge.device.model.EquipStateSnapshotDto;
import com.ourexists.mesedge.device.model.EquipStateSnapshotPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "设备运行记录")
@RestController
@RequestMapping("/equipStateSnapshot")
public class EquipStateSnapshotController {

    @Autowired
    private EquipStateSnapshotFeign feign;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EquipStateSnapshotDto>> selectByPage(@RequestBody EquipStateSnapshotPageQuery dto) {
        return feign.selectByPage(dto);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }

    @Operation(summary = "通过时间段统计设备", description = "通过时间段统计设备")
    @PostMapping("countNumByTime")
    public JsonResponseEntity<List<EquipStateSnapshotCountDto>> countNumByTime(@RequestBody EquipStateSnapshotCountQuery dto) {
        return feign.countNumByTime(dto);
    }
}
