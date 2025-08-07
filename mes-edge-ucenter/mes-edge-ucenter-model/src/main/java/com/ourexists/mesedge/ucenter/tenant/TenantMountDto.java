/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.tenant;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


/**
 * @author pengcheng
 * @date 2022/5/31 13:51
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class TenantMountDto extends BaseDto {

    private static final long serialVersionUID = -2849700606740153904L;

    @Schema(description = "账户id")
    @NotBlank(message = "请选择要挂载的账户")
    private String accId;

    @Schema(description = "租户id")
    @NotBlank(message = "请选择挂载的租户主体")
    private String tenantId;

    @Schema(description = "账户角色")
    @NotBlank(message = "请选择账户主角色")
    private String accRole;
}
