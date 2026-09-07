package com.ourexists.omes.process.engine.model;

import com.ourexists.omes.process.model.StepEnginePhase;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessStepTickResult {

    private StepEnginePhase phase;
    private double commandedValue;
    private String message;

    public static ProcessStepTickResult running(double commandedValue, String message) {
        return ProcessStepTickResult.builder()
                .phase(StepEnginePhase.RUNNING)
                .commandedValue(commandedValue)
                .message(message)
                .build();
    }

    public static ProcessStepTickResult completed(double commandedValue, String message) {
        return ProcessStepTickResult.builder()
                .phase(StepEnginePhase.COMPLETED)
                .commandedValue(commandedValue)
                .message(message)
                .build();
    }
}
