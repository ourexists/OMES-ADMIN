/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.portal.device.controller;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.dto.MapDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.era.framework.core.utils.tree.TreeUtil;
import com.ourexists.mesedge.device.enums.DeviceLocalizationEnum;
import com.ourexists.mesedge.device.enums.DeviceStatusEnum;
import com.ourexists.mesedge.device.enums.DeviceTypeEnum;
import com.ourexists.mesedge.device.feign.DeviceFeign;
import com.ourexists.mesedge.device.model.DeviceDto;
import com.ourexists.mesedge.device.model.DeviceTreeNode;
import com.ourexists.mesedge.mat.enums.BOMTypeEnum;
import com.ourexists.mesedge.mat.feign.MATFeign;
import com.ourexists.mesedge.mat.model.MaterialDto;
import com.ourexists.mesedge.portal.device.model.DeviceTreeNodeVo;
import com.ourexists.mesedge.task.model.TaskVo;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javafx.scene.paint.Material;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "设备")
@RestController
@Slf4j
@RequestMapping("/device")
public class DeviceController {

    @Autowired
    private DeviceFeign feign;

    @Autowired
    private MATFeign matFeign;

    @Operation(summary = "树", description = "树")
    @GetMapping("tree")
    public JsonResponseEntity<List<DeviceTreeNodeVo>> tree(@RequestParam String dgId) {
        try {
            List<DeviceTreeNode> treeNodes = RemoteHandleUtils.getDataFormResponse(feign.selectByDgId(dgId));
            if (CollectionUtil.isBlank(treeNodes)) {
                return JsonResponseEntity.success(new ArrayList<>());
            }
            List<String> matCodes = treeNodes.stream().map(DeviceTreeNode::getMatCode).collect(Collectors.toList());
            List<MaterialDto> materialDtos = null;
            if (CollectionUtil.isNotBlank(matCodes)) {
                IdsDto idsDto = new IdsDto();
                idsDto.setIds(matCodes);
                materialDtos = RemoteHandleUtils.getDataFormResponse(matFeign.selectByCodes(idsDto));
            }
            List<DeviceTreeNodeVo> r = new ArrayList<>();
            for (DeviceTreeNode treeNode : treeNodes) {
                DeviceTreeNodeVo d = new DeviceTreeNodeVo();
                BeanUtils.copyProperties(treeNode, d);
                if (CollectionUtil.isNotBlank(materialDtos)) {
                    for (MaterialDto materialDto : materialDtos) {
                        if (d.getMatCode().equals(materialDto.getSelfCode())) {
                            d.setMaterialDto(materialDto);
                            break;
                        }
                    }
                }
                r.add(d);
            }
            r = TreeUtil.foldRootTree(r);
            return JsonResponseEntity.success(r);
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Operation(summary = "通过设备工艺id查询", description = "通过设备工艺id查询")
    @GetMapping("selectByDgIdAndStatus")
    public JsonResponseEntity<List<DeviceTreeNode>> selectByDgIdAndStatus(@RequestParam String dgId,
                                                                          @RequestParam Integer status) {
        return feign.selectByDgIdAndStatus(dgId, status);
    }

    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody DeviceDto dto) {
        return feign.addOrUpdate(dto);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        return feign.delete(idsDto);
    }

    @Operation(summary = "启用", description = "启用")
    @GetMapping("enable")
    public JsonResponseEntity<Boolean> enable(@RequestParam String id) {
        return feign.changeStatus(id, DeviceStatusEnum.enable.getCode());
    }

    @Operation(summary = "停用", description = "停用")
    @GetMapping("disable")
    public JsonResponseEntity<Boolean> disable(@RequestParam String id) {
        return feign.changeStatus(id, DeviceStatusEnum.disable.getCode());
    }

    @Operation(summary = "id查詢", description = "id查詢")
    @GetMapping("selectById")
    public JsonResponseEntity<DeviceTreeNodeVo> selectById(@RequestParam String id) {
        try {
            DeviceTreeNode treeNode = RemoteHandleUtils.getDataFormResponse(feign.selectById(id));
            DeviceTreeNodeVo d = new DeviceTreeNodeVo();
            BeanUtils.copyProperties(treeNode, d);
            if (StringUtils.isNotEmpty(treeNode.getMatCode())) {
                IdsDto idsDto = new IdsDto();
                idsDto.setIds(Collections.singletonList(treeNode.getMatCode()));
                List<MaterialDto> materialDtos = RemoteHandleUtils.getDataFormResponse(matFeign.selectByCodes(idsDto));
                if (CollectionUtil.isNotBlank(materialDtos)) {
                    d.setMaterialDto(materialDtos.get(0));
                }
            }
            return JsonResponseEntity.success(d);
        } catch (EraCommonException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }

    @Operation(summary = "类型", description = "类型")
    @GetMapping("type")
    public JsonResponseEntity<List<MapDto>> type() {
        List<MapDto> r = new ArrayList<>();
        for (DeviceTypeEnum value : DeviceTypeEnum.values()) {
            r.add(new MapDto().setId(value.getCode().toString()).setName(value.getDesc()));
        }
        return JsonResponseEntity.success(r);
    }

    @Operation(summary = "定位", description = "定位")
    @GetMapping("localization")
    public JsonResponseEntity<List<MapDto>> localization() {
        List<MapDto> r = new ArrayList<>();
        for (DeviceLocalizationEnum value : DeviceLocalizationEnum.values()) {
            r.add(new MapDto().setId(value.getCode().toString()).setName(value.getDesc()));
        }
        return JsonResponseEntity.success(r);
    }


}
