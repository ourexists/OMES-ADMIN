/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.ucenter.viewer;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.ucenter.feign.SystemConfigFeign;
import com.ourexists.omes.ucenter.systemconfig.SystemConfigDto;
import com.ourexists.omes.ucenter.systemconfig.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
public class SystemConfigViewer implements SystemConfigFeign {

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    @Operation(summary = "获取应用系统配置")
    @GetMapping("/getAppConfig")
    public JsonResponseEntity<SystemConfigDto> getAppConfig() {
        return JsonResponseEntity.success(systemConfigService.getAppConfig());
    }

    @Override
    @Operation(summary = "保存应用系统配置")
    @PostMapping("/saveAppConfig")
    public JsonResponseEntity<Boolean> saveAppConfig(@Validated @RequestBody SystemConfigDto dto) {
        systemConfigService.saveAppConfig(dto);
        return JsonResponseEntity.success(true);
    }

    @Override
    @Operation(summary = "获取百度地图 AK")
    @GetMapping("/getBaiduMapAk")
    public JsonResponseEntity<String> getBaiduMapAk() {
        return JsonResponseEntity.success(systemConfigService.getBaiduMapAk());
    }
}
