package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "工序驱动引擎脚本更新请求")
public class ProcessStepScriptUpdateRequest {

    @Schema(description = "工序 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "工序ID不能为空")
    private String stepId;

    @Schema(description = "工序 stepScript JSON（为空则清除配置）")
    private String stepScript;

    @Schema(description = "工序动态参数 JSON（如 segments）")
    private String params;
}
