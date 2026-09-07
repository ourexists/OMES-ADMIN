package com.ourexists.omes.process.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "仿真会话状态")
public class ProcessStepSimulationStatusVO {

    private String sessionId;
    private String processId;
    private int currentStepIndex;
    private int totalSteps;
    private String currentStepType;
    private String currentStepMode;
    private double commandedValue;
    private double actualTemperature;
    private boolean finished;
    private Instant createdAt;
    private Instant lastTickAt;
}
