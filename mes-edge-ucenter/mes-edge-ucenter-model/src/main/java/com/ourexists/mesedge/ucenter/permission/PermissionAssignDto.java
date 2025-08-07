/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.permission;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/12 19:01
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class PermissionAssignDto extends BaseDto {

    private static final long serialVersionUID = -8144552300307885919L;
    @Schema(description = "对象id")
    @NotBlank(message = "请选择要分配的对象")
    private String id;

    @Schema(description = "权限id")
    @NotEmpty(message = "请为对应租户分配权限")
    private List<String> permissionIds;
}
