/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.tenant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author pengcheng
 * @version 1.0.0
 * @date 2023/1/30 14:57
 * @since 1.0.0
 */
@Getter
@Setter
public class TenantSettlePhones {

    @NotBlank(message = "请选择要入驻的租户")
    private String tenantCode;

    private String role;

    @NotBlank(message = "请选择挂载对象归属的平台")
    private String platform;

    @NotEmpty(message = "请输入要入驻的手机号")
    private List<String> phones;
}
