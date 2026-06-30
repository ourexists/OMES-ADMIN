package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "仿真 tick 结果")
public class ProcessStepSimulationTickVO {

    @Schema(description = "会话 ID")
    private String sessionId;

    @Schema(description = "当前步骤阶段")
    private StepEnginePhase phase;

    @Schema(description = "输出说明")
    private String message;

    @Schema(description = "指令设定值")
    private double commandedValue;

    @Schema(description = "实测温度")
    private double actualTemperature;

    @Schema(description = "当前步骤序号（从 0 开始）")
    private int currentStepIndex;

    @Schema(description = "总步骤数")
    private int totalSteps;

    @Schema(description = "整段脚本是否已全部完成")
    private boolean finished;

    @Schema(description = "当前步骤已运行毫秒")
    private long stepElapsedMs;
}
