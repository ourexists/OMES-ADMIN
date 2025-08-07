/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.role;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


/**
 * @author pengcheng
 * @date 2022/4/12 16:18
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class RoleDto extends BaseDto {

    private static final long serialVersionUID = -3012429300973571229L;
    private String id;

    @Schema(description = "角色名")
    @NotBlank(message = "请输入角色名")
    private String name;

    @Schema(description = "角色code")
    @NotBlank(message = "请输入角色编号")
    private String code;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "角色类别")
    private Integer type = 0;
}
