/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.ucenter.systemconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 系统配置 JSON 内容（存于 t_system_config.config）。
 */
@Schema(description = "系统配置内容(JSON)")
@Getter
@Setter
@Accessors(chain = true)
public class SystemConfigValue {

    @Schema(description = "百度地图浏览器端 AK")
    private String baiduMapAk;
}
