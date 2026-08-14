/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.device.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.device.feign.DeviceFeign;
import com.ourexists.omes.device.model.DeviceTreeNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "设备能力匹配")
@RestController
@RequestMapping("/device")
public class DeviceController {

    @Autowired
    private DeviceFeign feign;

    @Operation(summary = "按设备能力查询可投料设备")
    @GetMapping("selectByDgIdAndStatus")
    public JsonResponseEntity<List<DeviceTreeNode>> selectByDgIdAndStatus(@RequestParam String dgId) {
        return feign.selectByDgIdAndStatus(dgId);
    }
}
