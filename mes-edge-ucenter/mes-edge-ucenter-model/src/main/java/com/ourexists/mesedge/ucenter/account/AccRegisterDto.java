/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author pengcheng
 * @date 2022/4/6 18:04
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class AccRegisterDto extends AccAddDto {

    private static final long serialVersionUID = 200002L;

    @Schema(description = "租户id")
    private String tenantId;

    @Schema(description ="租户角色(默认普通角色)")
    private String accRole;
}
