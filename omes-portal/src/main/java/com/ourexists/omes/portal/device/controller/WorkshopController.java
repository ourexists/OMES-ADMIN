/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.device.controller;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtime;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtimeCollect;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtimeConfig;
import com.ourexists.omes.device.core.workshop.cache.WorkshopRealtimeManager;
import com.ourexists.omes.device.feign.WorkshopFeign;
import com.ourexists.omes.device.model.*;
import com.ourexists.omes.portal.device.ScadaPathManager;
import com.ourexists.omes.portal.device.model.ScadaUrlDto;
import com.ourexists.omes.ucenter.feign.RoleFeign;
import com.ourexists.omes.ucenter.role.RoleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    @Autowired
    private WorkshopRealtimeManager workshopRealtimeManager;


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

    @Operation(summary = "设置场景采集配置", description = "设置场景采集配置")
    @PostMapping("setConfigCollect")
    public JsonResponseEntity<Boolean> setConfigCollect(@Validated @RequestBody WorkshopConfigCollectDto dto) {
        try {
            RemoteHandleUtils.getDataFormResponse(workshopFeign.setConfigCollect(dto));
            WorkshopConfigCollectDto enriched = RemoteHandleUtils.getDataFormResponse(workshopFeign.queryConfigCollect(dto.getWorkshopId()));
            if (enriched != null && enriched.getConfig() != null) {
                WorkshopRealtime workshopRealtime = new WorkshopRealtime();
                workshopRealtime.setId(dto.getWorkshopId());
                WorkshopRealtimeConfig workshopRealtimeConfig = new WorkshopRealtimeConfig();
                BeanUtils.copyProperties(enriched.getConfig(), workshopRealtimeConfig);
                List<WorkshopRealtimeCollect> attrconfigs = toRealtimeCollectList(enriched.getConfig());
                workshopRealtimeConfig.setAttrs(attrconfigs);
                workshopRealtime.setConfig(workshopRealtimeConfig);
                workshopRealtime.setAttrsRealtime(null);
                workshopRealtimeManager.build(workshopRealtime);
            }
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
        return JsonResponseEntity.success(true);
    }

    private List<WorkshopRealtimeCollect> toRealtimeCollectList(WorkshopConfigCollectDetail config) {
        List<WorkshopRealtimeCollect> result = new ArrayList<>();
        if (config != null && config.getAttrs() != null) {
            for (WorkshopConfigCollectAttr attr : config.getAttrs()) {
                result.add(toRealtimeCollect(attr));
            }
        }
        return result;
    }

    private WorkshopRealtimeCollect toRealtimeCollect(WorkshopConfigCollectAttr attr) {
        WorkshopRealtimeCollect c = new WorkshopRealtimeCollect();
        BeanUtils.copyProperties(attr, c);
        return c;
    }

    @Operation(summary = "场景采集配置")
    @GetMapping("queryConfigCollect")
    public JsonResponseEntity<WorkshopConfigCollectDto> queryConfigCollect(@RequestParam String workshopId) {
        return workshopFeign.queryConfigCollect(workshopId);
    }

    @Operation(summary = "场景SCADA配置")
    @GetMapping("queryScadaConfig")
    public JsonResponseEntity<WorkshopConfigScadaDto> queryScadaConfig(@RequestParam String workshopId) {
        return workshopFeign.queryScadaConfig(workshopId);
    }

    @Operation(summary = "设置场景SCADA配置")
    @PostMapping("setScadaConfig")
    public JsonResponseEntity<Boolean> setScadaConfig(@Validated @RequestBody WorkshopConfigScadaDto dto) {
        return workshopFeign.setScadaConfig(dto);
    }

    @Operation(summary = "设置设备配置")
    @GetMapping("getScadaUrl")
    public JsonResponseEntity<ScadaUrlDto> getScadaUrl(@RequestParam String workshopId) {
        try {
            WorkshopConfigScadaDto workshopConfigScadaDto =
                    RemoteHandleUtils.getDataFormResponse(workshopFeign.queryScadaConfig(workshopId));
            if (workshopConfigScadaDto == null) {
                return JsonResponseEntity.success(null);
            }
            ScadaUrlDto scadaUrlDto = new ScadaUrlDto()
                    .setUrl(scadaPathManager.getScadaPath(workshopConfigScadaDto.getScadaConfig(), 1))
                    .setInterval(workshopConfigScadaDto.getScadaConfig().getInterval());
            return JsonResponseEntity.success(scadaUrlDto);
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Operation(summary = "场景SCADA配置")
    @GetMapping("getScadaUrlByWorkshopCode")
    public JsonResponseEntity<ScadaUrlDto> getScadaUrlByWorkshopCode(@RequestParam String workshopCode,
                                                                     @RequestParam Integer platform) {
        try {
            WorkshopConfigScadaDto workshopConfigScadaDto =
                    RemoteHandleUtils.getDataFormResponse(workshopFeign.queryScadaConfigByWorkshopCode(workshopCode));
            if (workshopConfigScadaDto == null) {
                return JsonResponseEntity.success(null);
            }
            ScadaUrlDto scadaUrlDto = new ScadaUrlDto()
                    .setUrl(scadaPathManager.getScadaPath(workshopConfigScadaDto.getScadaConfig(), platform))
                    .setInterval(workshopConfigScadaDto.getScadaConfig().getInterval());
            return JsonResponseEntity.success(scadaUrlDto);
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Operation(summary = "获取所有SCADA服务类型")
    @GetMapping("scadaServer")
    public JsonResponseEntity<List<String>> scadaServer() {
        return JsonResponseEntity.success(scadaPathManager.getAllRequesters());
    }

    @Operation(summary = "获取场景实时采集数据")
    @GetMapping("getWorkshopRealtimeCollect")
    public JsonResponseEntity<List<WorkshopRealtimeCollect>> getWorkshopRealtimeCollect(@RequestParam String workshopCode) {
        try {
            WorkshopTreeNode workshopTreeNode =
                    RemoteHandleUtils.getDataFormResponse(workshopFeign.selectByCode(workshopCode));
            if (workshopTreeNode == null) {
                return JsonResponseEntity.success(null);
            }
            WorkshopRealtime workshopRealtime = workshopRealtimeManager.get(workshopTreeNode.getId());
            if (workshopRealtime != null) {
                return JsonResponseEntity.success(workshopRealtime.getAttrsRealtime());
            }
            return JsonResponseEntity.success(null);
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }
}
