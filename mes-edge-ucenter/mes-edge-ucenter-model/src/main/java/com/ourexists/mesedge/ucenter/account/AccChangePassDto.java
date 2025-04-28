/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.account;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author pengcheng
 * @date 2022/4/13 19:02
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class AccChangePassDto extends BaseDto {

    private static final long serialVersionUID = 918522401821273115L;

    @Schema(description = "所属平台", required = true)
    private String platform;

    @Schema(description = "账户名", required = true)
    @NotBlank
    private String accName;

    @Schema(description = "旧密码", required = true)
    @NotBlank
    private String oldPass;

    @Schema(description = "新密码", required = true)
    @NotBlank
    private String newPass;

    @Schema(description = "确认密码", required = true)
    @NotBlank
    private String confirmPass;
}
