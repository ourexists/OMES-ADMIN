/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.account;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author pengcheng
 * @version 2.0.0
 * @date 2023/7/18 17:10
 * @since 2.0.0
 */
@Getter
@Setter
@Schema
public class AccResetPassPhoneDto {

    private String platform;

    @Schema(description = "手机号")
    @NotBlank
    private String mobile;

    @Schema(description = "新密码")
    @NotBlank
    private String newPass;
}
