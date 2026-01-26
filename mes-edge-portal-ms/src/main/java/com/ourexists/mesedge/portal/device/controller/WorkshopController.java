/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.device.controller;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.device.feign.WorkshopFeign;
import com.ourexists.mesedge.device.model.WorkshopAssignBatchDto;
import com.ourexists.mesedge.device.model.WorkshopConfigScadaDto;
import com.ourexists.mesedge.device.model.WorkshopDto;
import com.ourexists.mesedge.device.model.WorkshopTreeNode;
import com.ourexists.mesedge.portal.device.ScadaPathManager;
import com.ourexists.mesedge.portal.device.model.ScadaUrlDto;
import com.ourexists.mesedge.ucenter.feign.RoleFeign;
import com.ourexists.mesedge.ucenter.role.RoleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "设备车间")
@RestController
@RequestMapping("/workshop")
public class WorkshopController {

    @Autowired
    private WorkshopFeign workshopFeign;

    @Autowired
    private RoleFeign roleFeign;

    @Autowired
    private ScadaPathManager scadaPathManager;


    @Operation(summary = "查询所有树", description = "查询所有树")
    @GetMapping("selectTree")
    public JsonResponseEntity<List<WorkshopTreeNode>> selectTree() {
        return workshopFeign.selectTree();
    }

    @Operation(summary = "新增或修改根据id", description = "")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody WorkshopDto dto) {
        return workshopFeign.addOrUpdate(dto);
    }

    @Operation(summary = "删除", description = "")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return workshopFeign.delete(idsDto);
    }

    @Operation(summary = "分配", description = "分配")
    @PostMapping("assign")
    public JsonResponseEntity<Boolean> assign(@Validated @RequestBody WorkshopAssignBatchDto workshopAssignBatchDto) {
        return workshopFeign.assign(workshopAssignBatchDto);
    }

    @Operation(summary = "查询分配的树", description = "查询分配的树")
    @GetMapping("selectAssign")
    public JsonResponseEntity<List<WorkshopTreeNode>> selectAssign(@RequestParam String assignId) {
        return workshopFeign.selectAssign(assignId);
    }

    @Operation(summary = "查询分配的树", description = "查询分配的树")
    @GetMapping("selectUserAssignTree")
    public JsonResponseEntity<List<WorkshopTreeNode>> selectUserAssignTree() {
        try {
            List<RoleDto> roleDtos = RemoteHandleUtils.getDataFormResponse(
                    roleFeign.selectRoleWhichAccHoldOnly(UserContext.getUser().getId())
            );
            List<String> roleIds = roleDtos.stream().map(RoleDto::getId).toList();
            return workshopFeign.selectAssignTrees(roleIds, true);
        } catch (EraCommonException e) {
            throw new RuntimeException(e);
        }
    }

    @Operation(summary = "查询设备配置", description = "查询设备配置")
    @GetMapping("queryScadaConfig")
    public JsonResponseEntity<WorkshopConfigScadaDto> queryScadaConfig(@RequestParam String workshopId) {
        return workshopFeign.queryScadaConfig(workshopId);
    }

    @Operation(summary = "设置设备配置", description = "设置设备配置")
    @PostMapping("setScadaConfig")
    public JsonResponseEntity<Boolean> setScadaConfig(@Validated @RequestBody WorkshopConfigScadaDto dto) {
        return workshopFeign.setScadaConfig(dto);
    }

    @Operation(summary = "设置设备配置", description = "设置设备配置")
    @GetMapping("getScadaUrl")
    public JsonResponseEntity<ScadaUrlDto> getScadaUrl(@RequestParam String workshopId) {
        try {
            WorkshopConfigScadaDto workshopConfigScadaDto =
                    RemoteHandleUtils.getDataFormResponse(workshopFeign.queryScadaConfig(workshopId));
            if (workshopConfigScadaDto == null) {
                return JsonResponseEntity.success(null);
            }
            ScadaUrlDto scadaUrlDto = new ScadaUrlDto()
                    .setUrl(scadaPathManager.getScadaPath(workshopConfigScadaDto.getScadaConfig()))
                    .setInterval(workshopConfigScadaDto.getScadaConfig().getInterval());
            return JsonResponseEntity.success(scadaUrlDto);
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Operation(summary = "获取所有SCADA服务类型", description = "获取所有SCADA服务类型")
    @GetMapping("scadaServer")
    public JsonResponseEntity<List<String>> scadaServer() {
        return JsonResponseEntity.success(scadaPathManager.getAllRequesters());
    }
}
