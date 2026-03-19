/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.device.model.*;
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

    JsonResponseEntity<List<WorkshopTreeNode>> selectByCodes(@RequestBody List<String> codes);

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

    JsonResponseEntity<WorkshopConfigMeta2dDto> queryMeta2dConfig(@RequestParam String workshopId);

    JsonResponseEntity<Boolean> setMeta2dConfig(@Validated @RequestBody WorkshopConfigMeta2dDto dto);

    JsonResponseEntity<WorkshopConfigCollectDto> queryConfigCollect(String workshopId);

    JsonResponseEntity<Boolean> setConfigCollect(@Validated @RequestBody WorkshopConfigCollectDto dto);

    JsonResponseEntity<List<WorkshopConfigCollectDto>> queryAllConfigCollect();

    /** 按关联网关ID查询场景采集配置（用于 tags 快速查询） */
    JsonResponseEntity<List<WorkshopConfigCollectDto>> queryConfigCollectByGwId(@RequestParam String gatewayId);
}
