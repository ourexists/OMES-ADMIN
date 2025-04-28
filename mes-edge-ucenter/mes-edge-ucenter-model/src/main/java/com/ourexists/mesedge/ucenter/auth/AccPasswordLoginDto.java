/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.auth;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 账户密码登录认证模型
 *
 * @author PengCheng
 * @date 2022/4/8 16:28
 */
@Getter
@Setter
@Schema
public class AccPasswordLoginDto extends BaseDto {

    private static final long serialVersionUID = 5919246245280401738L;
    @Schema(description = "账号")
    private String account;

    @Schema(description = "密码")
    private String password;
}
