/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;


@Schema
@Getter
@Setter
@Accessors(chain = true)
public class EquipRecordCountQuery {

    @NotNull
    private Date startDate;

    @NotNull
    private Date endDate;

    @NotBlank
    private String sn;
}
