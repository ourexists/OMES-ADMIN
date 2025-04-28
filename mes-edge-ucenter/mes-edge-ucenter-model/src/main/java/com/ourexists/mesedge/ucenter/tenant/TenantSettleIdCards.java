/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.tenant;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author pengcheng
 * @version 1.0.0
 * @date 2023/1/30 14:57
 * @since 1.0.0
 */
@Getter
@Setter
public class TenantSettleIdCards {

    @NotBlank(message = "请选择要入驻的租户")
    private String tenantCode;

    private String role;

    @NotBlank(message = "请选择挂载对象归属的平台")
    private String platform;

    @NotEmpty(message = "请输入要入驻的身份证号")
    private List<String> idCards;
}
