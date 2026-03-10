/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.device.controller;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.core.equip.cache.EquipControlRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipRealtime;
import com.ourexists.omes.device.core.equip.cache.EquipRealtimeConfig;
import com.ourexists.omes.device.core.equip.cache.EquipRealtimeManager;
import com.ourexists.omes.device.core.equip.protocol.ProtocolManager;
import com.ourexists.omes.device.enums.EquipTypeEnum;
import com.ourexists.omes.device.feign.EquipFeign;
import com.ourexists.omes.device.feign.GatewayFeign;
import com.ourexists.omes.device.feign.WorkshopFeign;
import com.ourexists.omes.device.model.*;
import com.ourexists.omes.portal.device.model.EquipCountDto;
import com.ourexists.omes.portal.device.protocol.ProtocolManagerRunner;
import com.ourexists.omes.ucenter.feign.RoleFeign;
import com.ourexists.omes.ucenter.role.RoleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
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
    private GatewayFeign gatewayFeign;

    @Autowired
    private EquipRealtimeManager equipRealtimeManager;

    @Autowired
    private ProtocolManagerRunner protocolManagerRunner;

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
    public JsonResponseEntity<GwBindingDto> queryEquipConfig(@RequestParam String equipId) {
        return feign.queryEquipConfig(equipId);
    }

    @Operation(summary = "查询设备配置", description = "查询设备配置")
    @GetMapping("queryEquipConfigBySn")
    public JsonResponseEntity<GwBindingDto> queryEquipConfigBySn(@RequestParam String equipSn) {
        return feign.queryEquipConfigBySn(equipSn);
    }

    @Operation(summary = "设置设备配置", description = "设置设备配置")
    @PostMapping("setEquipConfig")
    public JsonResponseEntity<Boolean> setEquipConfig(@Validated @RequestBody GwBindingDto gwBindingDto) {
        return feign.setEquipConfig(gwBindingDto);
    }

    @Operation(summary = "控制写入", description = "向设备写入开关量/模拟量控制值")
    @PostMapping("writeControl")
    public JsonResponseEntity<Boolean> writeControl(@Validated @RequestBody EquipControlWriteDto dto) {
        EquipRealtime equipRealtime = equipRealtimeManager.getById(dto.getEquipId());
        if (equipRealtime == null) {
            throw new BusinessException("设备不存在或未加载");
        }
        EquipRealtimeConfig rtConfig = equipRealtime.getEquipRealtimeConfig();
        if (rtConfig == null || rtConfig.getGwId() == null) {
            throw new BusinessException("设备未绑定网关");
        }
        if (rtConfig.getControls() == null || rtConfig.getControls().isEmpty()) {
            throw new BusinessException("设备未配置控制点");
        }
        EquipControlRealtime matchedCtrl = null;
        for (EquipControlRealtime ctrl : rtConfig.getControls()) {
            if (dto.getAddress().equals(ctrl.getMap())) {
                matchedCtrl = ctrl;
                break;
            }
        }
        if (matchedCtrl == null) {
            throw new BusinessException("控制地址不在配置范围内: " + dto.getAddress());
        }
        if (matchedCtrl.getType() != null && matchedCtrl.getType() == 1) {
            try {
                double val = Double.parseDouble(dto.getValue().toString());
                if (StringUtils.hasText(matchedCtrl.getMin()) && val < Double.parseDouble(matchedCtrl.getMin())) {
                    throw new BusinessException("写入值低于最小值 " + matchedCtrl.getMin());
                }
                if (StringUtils.hasText(matchedCtrl.getMax()) && val > Double.parseDouble(matchedCtrl.getMax())) {
                    throw new BusinessException("写入值超过最大值 " + matchedCtrl.getMax());
                }
            } catch (NumberFormatException e) {
                throw new BusinessException("模拟量值格式错误");
            }
        }
        GatewayDto gw;
        try {
            gw = RemoteHandleUtils.getDataFormResponse(gatewayFeign.selectById(rtConfig.getGwId()));
        } catch (EraCommonException e) {
            log.error("查询网关失败, gwId={}", rtConfig.getGwId(), e);
            throw new BusinessException("查询网关失败");
        }
        if (gw == null) {
            throw new BusinessException("网关不存在");
        }
        ProtocolManager protocolManager = protocolManagerRunner.getProtocolManager(gw.getProtocol());
        if (protocolManager == null) {
            throw new BusinessException("不支持的协议: " + gw.getProtocol());
        }
        boolean result = protocolManager.write(rtConfig.getGwId(), dto.getAddress(), dto.getValue());
        if (!result) {
            throw new BusinessException("写入失败");
        }
        String writeValStr = String.valueOf(dto.getValue());
        matchedCtrl.setValue(writeValStr);
        if (equipRealtime.getEquipControlRealtimes() != null) {
            for (EquipControlRealtime ec : equipRealtime.getEquipControlRealtimes()) {
                if (dto.getAddress().equals(ec.getMap())) {
                    ec.setValue(writeValStr);
                    break;
                }
            }
        }
        return JsonResponseEntity.success(true);
    }
}
