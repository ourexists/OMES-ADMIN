package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "工序脚本热刷新（不写库，仅重载 LiteFlow 链与 Aviator 表达式）")
public class ProcessStepScriptHotReloadRequest {

    @Schema(description = "工序 ID（可选；有则链 ID 为 ps_{stepId}_segN，无则用脚本摘要）")
    private String stepId;

    @NotBlank(message = "工序脚本不能为空")
    @Schema(description = "工序 stepScript JSON（version=3 + flow）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String stepScript;
}
