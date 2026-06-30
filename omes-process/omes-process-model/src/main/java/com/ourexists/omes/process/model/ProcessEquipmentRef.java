package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "工序关联设备")
public class ProcessEquipmentRef {

    @Schema(description = "设备编号")
    @NotBlank(message = "设备编号不能为空")
    private String equipmentCode;

    @Schema(description = "设备名称")
    @NotBlank(message = "设备名称不能为空")
    private String equipmentName;
}
