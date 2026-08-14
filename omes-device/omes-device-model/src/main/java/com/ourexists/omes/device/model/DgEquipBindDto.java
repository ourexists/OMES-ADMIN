/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Schema
@Getter
@Setter
@Accessors(chain = true)
public class DgEquipBindDto {

    @Schema(description = "设备能力id")
    private String dgId;

    @Schema(description = "设备id列表")
    private List<String> equipIds;
}
