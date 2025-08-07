/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.ucenter.depart;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


/**
 * @author pengcheng
 * @date 2022/4/11 16:39
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class DepartModifyDto extends BaseDto {

    private static final long serialVersionUID = 6522037125549685122L;
    @NotBlank(message = "请选择要修改的用户组")
    private String id;

    @Schema(description = "用户组名")
    @NotBlank(message = "请输入用户组名")
    private String name;
}
