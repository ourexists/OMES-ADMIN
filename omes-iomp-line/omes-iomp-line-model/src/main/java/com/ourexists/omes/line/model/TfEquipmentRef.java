/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.line.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Schema(description = "工序关联设备")
public class TfEquipmentRef {

    @Schema(description = "设备 ID")
    private String equipmentId;

    @Schema(description = "设备编号")
    private String equipmentCode;

    @Schema(description = "设备名称")
    private String equipmentName;
}
