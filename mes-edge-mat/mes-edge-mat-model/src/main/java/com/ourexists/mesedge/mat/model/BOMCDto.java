/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Schema
@Getter
@Setter
@Accessors(chain = true)
public class BOMCDto extends BaseDto {

    private String id;

    @NotBlank(message = "${valid.selfcode.empty}")
    private String selfCode;

    @NotBlank(message = "${valid.name.empty}")
    private String name;

    private String code;

    private String pcode;

    private String description;
}
