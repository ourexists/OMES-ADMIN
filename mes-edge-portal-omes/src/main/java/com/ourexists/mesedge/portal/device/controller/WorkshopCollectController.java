/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.device.controller;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.device.feign.WorkshopCollectFeign;
import com.ourexists.mesedge.device.model.WorkshopCollectDto;
import com.ourexists.mesedge.device.model.WorkshopCollectPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "场景采集")
@RestController
@Slf4j
@RequestMapping("/workshop/collect")
public class WorkshopCollectController {

    @Autowired
    private WorkshopCollectFeign workshopCollectFeign;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<WorkshopCollectDto>> selectByPage(@RequestBody WorkshopCollectPageQuery dto) {
        return workshopCollectFeign.selectByPage(dto);
    }


    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return workshopCollectFeign.delete(idsDto);
    }
}
