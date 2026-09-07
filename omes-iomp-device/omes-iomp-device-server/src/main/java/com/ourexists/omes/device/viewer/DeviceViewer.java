/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.viewer;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.device.feign.DeviceFeign;
import com.ourexists.omes.device.model.DeviceTreeNode;
import com.ourexists.omes.device.service.EquipProcessService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
public class DeviceViewer implements DeviceFeign {

    @Autowired
    private EquipProcessService equipProcessService;

    @Override
    @Operation(summary = "按设备能力查询可投料设备")
    @GetMapping("selectByDgIdAndStatus")
    public JsonResponseEntity<List<DeviceTreeNode>> selectByDgIdAndStatus(@RequestParam String dgId) {
        return JsonResponseEntity.success(equipProcessService.listBoundAsDeviceNodes(dgId));
    }

    @Override
    @GetMapping("isUseMat")
    public JsonResponseEntity<Boolean> isUseMat(@RequestParam List<String> matCodes) {
        return JsonResponseEntity.success(equipProcessService.isUseMat(matCodes));
    }
}
