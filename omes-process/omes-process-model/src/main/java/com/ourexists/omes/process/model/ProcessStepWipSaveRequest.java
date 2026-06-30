package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "保存工序 WIP/排产配置")
public class ProcessStepWipSaveRequest {

    @NotBlank(message = "工序名称不能为空")
    @Schema(description = "工序名称（全局唯一，可跨工艺复用）")
    private String stepName;

    @NotNull(message = "WIP配置不能为空")
    @Valid
    @Schema(description = "WIP/排产配置")
    private ProcessStepWipItem wip;
}
