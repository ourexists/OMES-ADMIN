/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.platform;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * @author pengcheng
 * @date 2022/4/2 16:13
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class PlatformDto extends BaseDto {

    private static final long serialVersionUID = -5139652996423605914L;

    private String id;

    @Schema(description = "平台名")
    @NotBlank(message = "请输入平台名")
    private String name;

    @Schema(description = "平台编号")
    @NotBlank(message = "请输入平台编号")
    private String code;

}
