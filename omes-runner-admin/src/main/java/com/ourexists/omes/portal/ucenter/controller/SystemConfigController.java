/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.portal.ucenter.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.ucenter.feign.SystemConfigFeign;
import com.ourexists.omes.ucenter.systemconfig.SystemConfigDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "系统配置")
@RestController
@RequestMapping("/systemConfig")
public class SystemConfigController {

    @Autowired
    private SystemConfigFeign systemConfigFeign;

    @Operation(summary = "获取应用系统配置")
    @GetMapping("/get")
    public JsonResponseEntity<SystemConfigDto> get() {
        return systemConfigFeign.getAppConfig();
    }

    @Operation(summary = "保存应用系统配置")
    @PostMapping("/save")
    public JsonResponseEntity<Boolean> save(@Validated @RequestBody SystemConfigDto dto) {
        return systemConfigFeign.saveAppConfig(dto);
    }
}
