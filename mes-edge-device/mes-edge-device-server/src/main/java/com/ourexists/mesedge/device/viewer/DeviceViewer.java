/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.viewer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.core.utils.tree.TreeUtil;
import com.ourexists.mesedge.device.enums.DeviceStatusEnum;
import com.ourexists.mesedge.device.feign.DeviceFeign;
import com.ourexists.mesedge.device.model.DeviceDto;
import com.ourexists.mesedge.device.model.DeviceTreeNode;
import com.ourexists.mesedge.device.pojo.Device;
import com.ourexists.mesedge.device.service.DeviceService;
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
import java.util.stream.Collectors;

//@Tag(name = "配方分类")
//@RestController
//@RequestMapping("/BOMC")
@Component
public class DeviceViewer implements DeviceFeign {

    @Autowired
    private DeviceService service;

    @Operation(summary = "设备树", description = "设备树")
    @GetMapping("tree")
    public JsonResponseEntity<List<DeviceTreeNode>> tree(@RequestParam String dgId) {
        List<DeviceTreeNode> nodes = Device.covert(service.list(new LambdaQueryWrapper<Device>().eq(Device::getDgId, dgId)));
        nodes = TreeUtil.foldRootTree(nodes);
        return JsonResponseEntity.success(nodes);
    }


    @Operation(summary = "通过设备工艺id查询", description = "通过设备工艺id查询")
    @GetMapping("selectByDgId")
    public JsonResponseEntity<List<DeviceTreeNode>> selectByDgId(@RequestParam String dgId) {
        List<DeviceTreeNode> nodes = Device.covert(service.list(new LambdaQueryWrapper<Device>()
                .eq(Device::getDgId, dgId)));
        return JsonResponseEntity.success(nodes);
    }

    @Operation(summary = "通过设备工艺id查询", description = "通过设备工艺id查询")
    @GetMapping("selectByDgIdAndStatus")
    public JsonResponseEntity<List<DeviceTreeNode>> selectByDgIdAndStatus(@RequestParam String dgId,
                                                                          @RequestParam Integer status) {
        DeviceStatusEnum deviceStatusEnum = DeviceStatusEnum.valueof(status);
        List<DeviceTreeNode> nodes = Device.covert(service.list(new LambdaQueryWrapper<Device>()
                .eq(Device::getDgId, dgId)
                .eq(Device::getStatus, deviceStatusEnum.getCode())));
        return JsonResponseEntity.success(nodes);
    }

    @Operation(summary = "新增或修改根据id", description = "新增或修改根据id")
    @PostMapping("addOrUpdate")
    public JsonResponseEntity<Boolean> addOrUpdate(@Validated @RequestBody DeviceDto dto) {
        if (StringUtils.isBlank(dto.getPcode())) {
            dto.setPcode(TreeUtil.ROOT_CODE);
        }
        //生成级联编号
        if (StringUtils.isBlank(dto.getCode())) {
            Device device = service.getOne(new LambdaQueryWrapper<Device>()
                    .eq(Device::getPcode, dto.getPcode())
                    .orderByDesc(Device::getCode)
                    .last("limit 1")
            );
            String otherMaxCode = device == null ? null : device.getCode();
            dto.setCode(TreeUtil.generateCode(dto.getPcode(), otherMaxCode));
        }
        service.saveOrUpdate(Device.wrap(dto));
        return JsonResponseEntity.success(true);
    }

    @Operation(summary = "删除", description = "删除")
    @PostMapping("delete")
    public JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto) {
        List<Device> BOMCList = service.listByIds(idsDto.getIds());
        if (CollectionUtil.isBlank(BOMCList)) {
            return JsonResponseEntity.success(true);
        }
        List<String> codes = BOMCList.stream().map(Device::getCode).collect(Collectors.toList());
        List<Device> children = service.list(new LambdaQueryWrapper<Device>()
                .in(Device::getPcode, codes));
        if (CollectionUtil.isNotBlank(children)) {
            StringBuilder msg = new StringBuilder();
            for (Device child : children) {
                msg.append(child.getName()).append(",");
            }
            throw new BusinessException("${common.msg.delete.existchild}", msg.substring(0, msg.length() - 1));
        }
        service.removeByIds(idsDto.getIds());
        return JsonResponseEntity.success(true);
    }

    @Override
    @GetMapping("selectById")
    public JsonResponseEntity<DeviceTreeNode> selectById(@RequestParam String id) {
        return JsonResponseEntity.success(Device.covert(service.getById(id)));
    }

    @Override
    @GetMapping("changeStatus")
    public JsonResponseEntity<Boolean> changeStatus(@RequestParam String id, @RequestParam Integer status) {
        this.service.update(new LambdaUpdateWrapper<Device>().set(Device::getStatus, status).eq(Device::getId, id));
        return JsonResponseEntity.success(true);
    }

    @Override
    @GetMapping("isUseMat")
    public JsonResponseEntity<Boolean> isUseMat(@RequestParam List<String> matCodes) {
        return JsonResponseEntity.success(this.service.isUseMat(matCodes));
    }
}
