/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.device.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.device.feign.EquipRecordOnlineFeign;
import com.ourexists.mesedge.device.model.EquipRecordCountQuery;
import com.ourexists.mesedge.device.model.EquipRecordOnlinePageQuery;
import com.ourexists.mesedge.device.model.EquipRecordOnlineVo;
import com.ourexists.mesedge.device.model.EquipRecordRunVo;
import com.ourexists.mesedge.device.pojo.EquipRecordOnline;
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
@RequestMapping("/equipRecordOnline")
public class EquipRecordOnlineController {

    @Autowired
    private EquipRecordOnlineFeign feign;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EquipRecordOnlineVo>> selectByPage(@RequestBody EquipRecordOnlinePageQuery dto) {
        return feign.selectByPage(dto);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }

    @Operation(summary = "统计", description = "统计")
    @PostMapping("countMerging")
    public JsonResponseEntity<List<EquipRecordOnlineVo>> countMerging(@RequestBody EquipRecordCountQuery query) {
        return feign.countMerging(query);
    }
}
