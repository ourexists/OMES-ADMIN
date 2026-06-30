package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Schema(description = "更新仿真过程量")
public class ProcessStepSimulationSignalsRequest {

    @NotBlank(message = "sessionId 不能为空")
    @Schema(description = "仿真会话 ID")
    private String sessionId;

    @Schema(description = "当前温度")
    private Double temperature;

    @Schema(description = "离散状态，如 DOOR_CLOSED")
    private Map<String, Boolean> states = new LinkedHashMap<>();

    @Schema(description = "人工确认完成动作（仿真/调试）")
    private Boolean completeConfirmed;
}
