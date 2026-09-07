/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.omes.ucenter.systemconfig;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "系统配置")
public class SystemConfigDto extends BaseDto {

    private static final long serialVersionUID = 1L;

    private String id;

    @Schema(description = "配置键，默认 app")
    private String configKey;

    @Schema(description = "配置内容(JSON)")
    private SystemConfigValue config;
}
