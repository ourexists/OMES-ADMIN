package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "仿真会话请求")
public class ProcessStepSimulationSessionRequest {

    @NotBlank(message = "sessionId 不能为空")
    @Schema(description = "仿真会话 ID")
    private String sessionId;
}
