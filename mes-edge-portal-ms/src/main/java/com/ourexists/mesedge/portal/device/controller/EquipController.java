/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.device.controller;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.mesedge.device.core.equip.collect.EquipRealtimeCollectSelector;
import com.ourexists.mesedge.device.enums.EquipTypeEnum;
import com.ourexists.mesedge.device.feign.EquipFeign;
import com.ourexists.mesedge.device.feign.WorkshopFeign;
import com.ourexists.mesedge.device.model.EquipConfigDto;
import com.ourexists.mesedge.device.model.EquipDto;
import com.ourexists.mesedge.device.model.EquipPageQuery;
import com.ourexists.mesedge.device.model.WorkshopTreeNode;
import com.ourexists.mesedge.portal.device.model.EquipCountDto;
import com.ourexists.mesedge.ucenter.feign.RoleFeign;
import com.ourexists.mesedge.ucenter.role.RoleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "设备")
@RestController
@Slf4j
@RequestMapping("/equip")
public class EquipController {

    @Autowired
    private EquipFeign feign;

    @Autowired
    private WorkshopFeign workshopFeign;

    @Autowired
    private RoleFeign roleFeign;

    @Autowired
    private EquipRealtimeCollectSelector equipRealtimeCollectSelector;

    @Operation(summary = "分页查询", description = "分页查询")
    @PostMapping("selectByPage")
    public JsonResponseEntity<List<EquipDto>> selectByPage(@RequestBody EquipPageQuery dto) {
        try {
            if (dto.getLimitUserWorkshop()) {
                List<RoleDto> roleDtos = RemoteHandleUtils.getDataFormResponse(
                        roleFeign.selectRoleWhichAccHoldOnly(UserContext.getUser().getId())
                );
                List<String> roleIds = roleDtos.stream().map(RoleDto::getId).toList();
                List<WorkshopTreeNode> workshopTreeNodes =
                        RemoteHandleUtils.getDataFormResponse(workshopFeign.selectAssignTrees(roleIds, false));
                if (CollectionUtil.isBlank(workshopTreeNodes)) {
                    return JsonResponseEntity.success(Collections.emptyList());
                }
                List<String> workshopCodes = workshopTreeNodes.stream().map(WorkshopTreeNode::getSelfCode).toList();
                dto.setWorkshopCodes(workshopCodes);
            }
        } catch (EraCommonException e) {
            throw new RuntimeException(e);
        }
        return feign.selectByPage(dto);
    }

    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody EquipDto dto) {
        return feign.addOrUpdate(dto);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }

    @Operation(summary = "通过id查询", description = "通过id查询")
    @GetMapping("selectById")
    public JsonResponseEntity<EquipDto> selectById(@RequestParam String id) {
        return feign.selectById(id, false);
    }

    @Operation(summary = "通过id查询", description = "通过id查询")
    @GetMapping("selectRealtimeById")
    public JsonResponseEntity<EquipDto> selectRealtimeById(@RequestParam String id) {
        return feign.selectById(id, true);
    }

    @Operation(summary = "设备类型", description = "设备类型")
    @GetMapping("equipType")
    public JsonResponseEntity<Map<Integer, String>> equipType() {
        Map<Integer, String> r = new HashMap<>();
        for (EquipTypeEnum value : EquipTypeEnum.values()) {
            r.put(value.getCode(), value.getDesc());
        }
        return JsonResponseEntity.success(r);
    }

    @Operation(summary = "统计实时数据", description = "统计实时数据")
    @PostMapping("countRealtime")
    public JsonResponseEntity<EquipCountDto> countRealtime(@RequestBody EquipPageQuery dto) {
        EquipCountDto r = new EquipCountDto();
        try {
            dto.setRequirePage(false);
            dto.setNeedRealtime(true);
            if (dto.getLimitUserWorkshop()) {
                List<RoleDto> roleDtos = RemoteHandleUtils.getDataFormResponse(
                        roleFeign.selectRoleWhichAccHoldOnly(UserContext.getUser().getId())
                );
                List<String> roleIds = roleDtos.stream().map(RoleDto::getId).toList();
                List<WorkshopTreeNode> workshopTreeNodes =
                        RemoteHandleUtils.getDataFormResponse(workshopFeign.selectAssignTrees(roleIds, false));
                if (CollectionUtil.isBlank(workshopTreeNodes)) {
                    return JsonResponseEntity.success(r);
                }
                List<String> workshopCodes = workshopTreeNodes.stream().map(WorkshopTreeNode::getSelfCode).toList();
                dto.setWorkshopCodes(workshopCodes);
            }
            List<EquipDto> equipDtos = RemoteHandleUtils.getDataFormResponse(feign.selectByPage(dto));
            for (EquipDto equipDto : equipDtos) {
                r.setTotal(r.getTotal() + 1);
                if (equipDto.getOnlineState() == 1) {
                    r.setOnline(r.getOnline() + 1);
                    if (equipDto.getRunState() == 1) {
                        r.setRun(r.getRun() + 1);
                    } else {
                        r.setStopped(r.getStopped() + 1);
                    }
                    if (equipDto.getAlarmState() == 1) {
                        r.setAlarm(r.getAlarm() + 1);
                    }
                } else {
                    r.setOffline(r.getOffline() + 1);
                }
            }

        } catch (EraCommonException e) {
            throw new RuntimeException(e);
        }
        return JsonResponseEntity.success(r);
    }


    @Operation(summary = "查询设备配置", description = "查询设备配置")
    @GetMapping("queryEquipConfig")
    public JsonResponseEntity<EquipConfigDto> queryEquipConfig(@RequestParam String equipId) {
        return feign.queryEquipConfig(equipId);
    }

    @Operation(summary = "查询设备配置", description = "查询设备配置")
    @GetMapping("queryEquipConfigBySn")
    public JsonResponseEntity<EquipConfigDto> queryEquipConfigBySn(@RequestParam String equipSn) {
        return feign.queryEquipConfigBySn(equipSn);
    }

    @Operation(summary = "设置设备配置", description = "设置设备配置")
    @PostMapping("setEquipConfig")
    public JsonResponseEntity<Boolean> setEquipConfig(@Validated @RequestBody EquipConfigDto equipConfigDto) {
        return feign.setEquipConfig(equipConfigDto);
    }

    @Operation(summary = "所有采集方式")
    @GetMapping("collectType")
    public JsonResponseEntity<List<String>> collectType() {
        return JsonResponseEntity.success(equipRealtimeCollectSelector.getAllNames());
    }
}
