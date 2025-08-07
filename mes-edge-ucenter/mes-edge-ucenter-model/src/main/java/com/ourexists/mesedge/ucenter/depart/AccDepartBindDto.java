/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */
package com.ourexists.mesedge.ucenter.depart;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author pengcheng
 * @date 2022/5/5 17:21
 * @since 1.0.0
 */
@Getter
@Setter
@Schema
public class AccDepartBindDto extends BaseDto {

    private static final long serialVersionUID = -4210245356097770387L;

    @Schema(description = "账户id")
    @NotBlank(message = "请选择账户")
    private String accId;

    @Schema(description = "部门id")
    @NotEmpty(message = "请选择用户归属的部门")
    private List<String> departIds;
}
