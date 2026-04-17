/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.viewer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.tree.TreeUtil;
import com.ourexists.omes.device.feign.WorkshopFeign;
import com.ourexists.omes.device.model.*;
import com.ourexists.omes.device.pojo.Workshop;
import com.ourexists.omes.device.pojo.WorkshopAssign;
import com.ourexists.omes.device.pojo.WorkshopConfigCollect;
import com.ourexists.omes.device.pojo.WorkshopConfigMeta2d;
import com.ourexists.omes.device.pojo.WorkshopConfigScada;
import com.ourexists.omes.device.service.WorkshopAssignService;
import com.ourexists.omes.device.service.WorkshopConfigCollectService;
import com.ourexists.omes.device.service.WorkshopConfigMeta2dService;
import com.ourexists.omes.device.service.WorkshopConfigScadaService;
import com.ourexists.omes.device.service.WorkshopService;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@Tag(name = "配方管理")
//@RestController
//@RequestMapping("/BOM")
@Component
public class WorkshopViewer implements WorkshopFeign {

    @Autowired
    private WorkshopService service;

    @Autowired
    private WorkshopAssignService assignService;

    @Autowired
    private WorkshopConfigScadaService configScadaService;

    @Autowired
    private WorkshopConfigCollectService workshopConfigCollectService;

    @Autowired
    private WorkshopConfigMeta2dService configMeta2dService;

    @Operation(summary = "查询所有树", description = "查询所有树")
    @GetMapping("selectTree")
    public JsonResponseEntity<List<WorkshopTreeNode>> selectTree() {
        List<WorkshopTreeNode> nodes = Workshop.covert(service.list());
        nodes = TreeUtil.foldRootTree(nodes);
        return JsonResponseEntity.success(nodes);
    }


    @Operation(summary = "查询分配的树", description = "查询分配的树")
    @GetMapping("selectAssign")
    public JsonResponseEntity<List<WorkshopTreeNode>> selectAssign(@RequestParam String assignId) {
        List<String> workshopCodes = assignService.list(new LambdaQueryWrapper<WorkshopAssign>()
                        .eq(WorkshopAssign::getAssignId, assignId)
                        .select(WorkshopAssign::getWorkshopCode)).stream()
                .map(WorkshopAssign::getWorkshopCode)
                .toList();
        List<WorkshopTreeNode> nodes = Workshop.covert(
                service.list(new LambdaQueryWrapper<Workshop>()
                        .in(CollectionUtil.isNotBlank(workshopCodes), Workshop::getSelfCode, workshopCodes)));
        return JsonResponseEntity.success(nodes);
    }

    @Operation(summary = "查询分配的树", description = "查询分配的树")
    @PostMapping("selectAssignTrees")
    public JsonResponseEntity<List<WorkshopTreeNode>> selectAssignTrees(@RequestBody List<String> assignIds, Boolean needFold) {
        List<WorkshopAssign> workshopAssigns = assignService.list(new LambdaQueryWrapper<WorkshopAssign>()
                .in(WorkshopAssign::getAssignId, assignIds));
        List<String> workshopCodes = workshopAssigns.stream().map(WorkshopAssign::getWorkshopCode).toList();
        List<WorkshopTreeNode> nodes = null;
        if (CollectionUtil.isNotBlank(workshopCodes)) {
            nodes = Workshop.covert(
                    service.list(new LambdaQueryWrapper<Workshop>()
                            .in(Workshop::getSelfCode, workshopCodes)));
            if (needFold) {
                nodes = TreeUtil.foldRootTree(nodes);
            }
        }
        return JsonResponseEntity.success(nodes);
    }


    @Operation(summary = "查询分配的树", description = "查询分配的树")
    @PostMapping("assign")
    public JsonResponseEntity<Boolean> assign(@RequestBody WorkshopAssignBatchDto workshopAssignBatchDto) {
        assignService.assign(workshopAssignBatchDto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "通过设备id查询场景分配", description = "通过设备id查询场景分配")
    @GetMapping("selectWorkshopAssignByEquipId")
    public JsonResponseEntity<List<WorkshopAssignDto>> selectWorkshopAssignByEquipId(@RequestParam String equipId) {
        return JsonResponseEntity.success(WorkshopAssign.covert(assignService.selectWorkshopAssignByEquipId(equipId)));
    }


    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody WorkshopDto dto) {
        if (StringUtils.isBlank(dto.getPcode())) {
            dto.setPcode(TreeUtil.ROOT_CODE);
        }
        //生成级联编号
        if (StringUtils.isBlank(dto.getCode())) {
            Workshop s = service.getOne(new LambdaQueryWrapper<Workshop>().eq(Workshop::getPcode, dto.getPcode()).orderByDesc(Workshop::getCode).last("limit 1"));
            String otherMaxCode = s == null ? null : s.getCode();
            dto.setCode(TreeUtil.generateCode(dto.getPcode(), otherMaxCode));
        }
        service.addOrUpdate(dto);
        return JsonResponseEntity.success(true);
    }

    @Override
    public JsonResponseEntity<WorkshopTreeNode> selectByCode(String code) {
        return JsonResponseEntity.success(Workshop.covert(service.selectByCode(code)));
    }

    @Override
    public JsonResponseEntity<List<WorkshopTreeNode>> selectByCodes(List<String> codes) {
        return JsonResponseEntity.success(Workshop.covert(service.selectByCodes(codes)));
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {

        service.delete(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "场景SCADA配置", description = "场景SCADA配置")
    @GetMapping("queryScadaConfig")
    public JsonResponseEntity<WorkshopConfigScadaDto> queryScadaConfig(@RequestParam String workshopId) {
        return JsonResponseEntity.success(WorkshopConfigScada.covert(configScadaService.queryByWorkshop(workshopId)));
    }

    @Operation(summary = "场景SCADA配置", description = "场景SCADA配置")
    @GetMapping("queryScadaConfigByWorkshopCode")
    public JsonResponseEntity<WorkshopConfigScadaDto> queryScadaConfigByWorkshopCode(@RequestParam String workshopCode) {
        Workshop workshop = service.selectByCode(workshopCode);
        if (workshop == null) {
            return JsonResponseEntity.success(null);
        }
        return JsonResponseEntity.success(WorkshopConfigScada.covert(configScadaService.queryByWorkshop(workshop.getId())));
    }

    @Operation(summary = "设置场景SCADA配置", description = "设置场景SCADA配置")
    @PostMapping("setScadaConfig")
    public JsonResponseEntity<Boolean> setScadaConfig(@Validated @RequestBody WorkshopConfigScadaDto dto) {
        configScadaService.addOrUpdate(dto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "场景Meta2d组态配置", description = "场景Meta2d组态配置")
    @GetMapping("queryMeta2dConfig")
    public JsonResponseEntity<WorkshopConfigMeta2dDto> queryMeta2dConfig(@RequestParam String workshopId) {
        return JsonResponseEntity.success(WorkshopConfigMeta2d.covert(configMeta2dService.queryByWorkshop(workshopId)));
    }

    @Operation(summary = "设置场景Meta2d组态配置", description = "设置场景Meta2d组态配置")
    @PostMapping("setMeta2dConfig")
    public JsonResponseEntity<Boolean> setMeta2dConfig(@Validated @RequestBody WorkshopConfigMeta2dDto dto) {
        configMeta2dService.addOrUpdate(dto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "场景采集配置", description = "场景采集配置")
    @GetMapping("queryConfigCollect")
    public JsonResponseEntity<WorkshopConfigCollectDto> queryConfigCollect(@RequestParam String workshopId) {
        return JsonResponseEntity.success(WorkshopConfigCollect.covert(workshopConfigCollectService.queryByWorkshop(workshopId)));
    }

    @Operation(summary = "设置场景采集配置", description = "设置场景采集配置")
    @PostMapping("setConfigCollect")
    public JsonResponseEntity<Boolean> setConfigCollect(@Validated @RequestBody WorkshopConfigCollectDto dto) {
        workshopConfigCollectService.addOrUpdate(dto);
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "所有场景采集配置", description = "所有场景采集配置")
    @GetMapping("queryAllConfigCollect")
    public JsonResponseEntity<List<WorkshopConfigCollectDto>> queryAllConfigCollect() {
        return JsonResponseEntity.success(WorkshopConfigCollect.covert(workshopConfigCollectService.queryAllConfigCollect()));
    }

    @Operation(summary = "按网关ID查询场景采集配置", description = "用于 tags 快速查询")
    @GetMapping("queryConfigCollectByGwId")
    public JsonResponseEntity<List<WorkshopConfigCollectDto>> queryConfigCollectByGwId(@RequestParam String gatewayId) {
        return JsonResponseEntity.success(WorkshopConfigCollect.covert(workshopConfigCollectService.queryByGwId(gatewayId)));
    }

}
