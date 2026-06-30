package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "仿真启动结果")
public class ProcessStepSimulationStartVO {

    @Schema(description = "仿真会话 ID")
    private String sessionId;

    @Schema(description = "工艺 ID")
    private String processId;

    @Schema(description = "总步骤数")
    private int totalSteps;

    @Schema(description = "当前步骤序号（从 0 开始）")
    private int currentStepIndex;

    @Schema(description = "当前步骤类型")
    private String currentStepType;

    @Schema(description = "当前步骤模式")
    private String currentStepMode;
}
