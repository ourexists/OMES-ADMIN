/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.permission;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author pengcheng
 * @date 2022/4/15 19:00
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class PermissionApiDto extends BaseDto {

    private static final long serialVersionUID = 2278122870881467959L;

    @NotBlank(message = "请选择权限对象！")
    @Schema(description ="权限id")
    private String permissionId;

    @NotEmpty(message = "要分配的api信息不能为空！")
    private List<PermissionApiDetailDto> detailDtos;
}
