/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mps.model;

import com.ourexists.era.framework.core.model.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class MPSCDto extends BaseDto {

    private String id;

    @NotBlank(message = "${valid.selfcode.empty}")
    private String selfCode;

    @NotBlank(message = "${valid.name.empty}")
    private String name;

    private String code;

    private String pcode;

    private String description;
}
