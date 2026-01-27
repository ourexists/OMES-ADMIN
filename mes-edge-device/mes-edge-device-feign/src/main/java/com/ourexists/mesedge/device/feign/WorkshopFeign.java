/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.device.model.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@RequestMapping("/mat")
public interface WorkshopFeign {

    JsonResponseEntity<List<WorkshopTreeNode>> selectTree();

    //    @Operation(summary = "新增或修改根据id", description = "")
//    @PostMapping("addOrUpdate")
    JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody WorkshopDto dto);

    JsonResponseEntity<WorkshopTreeNode> selectByCode(@RequestParam String code);

    //    @Operation(summary = "删除", description = "")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    JsonResponseEntity<Boolean> assign(@RequestBody WorkshopAssignBatchDto workshopAssignBatchDto);

    JsonResponseEntity<List<WorkshopAssignDto>> selectWorkshopAssignByEquipId(@RequestParam String equipId);

    JsonResponseEntity<List<WorkshopTreeNode>> selectAssign(@RequestParam String assignId);

    JsonResponseEntity<List<WorkshopTreeNode>> selectAssignTrees(@RequestParam List<String> assignIds, Boolean needFold);

    JsonResponseEntity<WorkshopConfigScadaDto> queryScadaConfig(@RequestParam String workshopId);

    JsonResponseEntity<WorkshopConfigScadaDto> queryScadaConfigByWorkshopCode(@RequestParam String workshopCode);

    JsonResponseEntity<Boolean> setScadaConfig(@Validated @RequestBody WorkshopConfigScadaDto dto);
}
