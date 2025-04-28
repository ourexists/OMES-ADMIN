/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.tenant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author pengcheng
 * @version 1.0.0
 * @date 2023/2/9 10:51
 * @since 1.0.0
 */
@Setter
@Getter
@Schema
public class TenantUVo extends TenantVo {

    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "租户中的角色")
    private String role;
}
