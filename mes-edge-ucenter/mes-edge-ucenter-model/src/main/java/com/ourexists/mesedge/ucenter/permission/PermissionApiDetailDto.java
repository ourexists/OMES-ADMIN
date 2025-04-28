/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.permission;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author pengcheng
 * @date 2022/4/15 19:00
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class PermissionApiDetailDto extends BaseDto {

    private static final long serialVersionUID = 8238906499482748010L;

    @Schema(description = "服务名")
    private String serverName;

    @Schema(description = "接口path")
    private String path;
}
