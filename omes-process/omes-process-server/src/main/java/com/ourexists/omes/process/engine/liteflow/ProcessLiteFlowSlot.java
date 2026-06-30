package com.ourexists.omes.process.engine.liteflow;

import com.ourexists.omes.process.engine.ProcessStepActionEngineResolver;
import com.ourexists.omes.process.engine.ProcessStepEngine;
import com.ourexists.omes.process.engine.model.ProcessExecutionContext;
import com.ourexists.omes.process.engine.model.ProcessStepCombination;
import com.ourexists.omes.process.engine.model.ProcessStepTickResult;
import com.ourexists.omes.process.model.StepEnginePhase;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * LiteFlow 与工序 tick 引擎共享的运行槽位。
 */
@Getter
@Setter
public class ProcessLiteFlowSlot {

    private ProcessExecutionContext executionContext;
    private List<ProcessStepCombination> combinations;
    private ProcessStepActionEngineResolver actionEngineResolver;
    private int comboIndex;
    /** 当前段内阶段下标，对应 {@link com.ourexists.omes.process.engine.model.ProcessStepCombination#getPhaseOrder()} */
    private int phaseStepIndex;
    private ProcessComboPhase phase = ProcessComboPhase.WAIT_DRIVE;
    private String phaseKey;
    private ProcessStepEngine actionEngine;
    private StepEnginePhase enginePhase = StepEnginePhase.RUNNING;
    private String message = "";
    private long deltaMs;
    private boolean chainStop;

    public ProcessStepCombination currentCombo() {
        return combinations.get(comboIndex);
    }

    public String comboLabel() {
        ProcessStepCombination combo = currentCombo();
        if (combo.getName() != null && !combo.getName().isBlank()) {
            return combo.getName().trim();
        }
        return "组合" + (comboIndex + 1);
    }

    public ProcessStepTickResult toTickResult() {
        double commanded = executionContext.getCommandedSetpoint();
        return switch (enginePhase) {
            case COMPLETED -> ProcessStepTickResult.completed(commanded, message);
            case FAILED -> ProcessStepTickResult.builder()
                    .phase(StepEnginePhase.FAILED)
                    .commandedValue(commanded)
                    .message(message)
                    .build();
            default -> ProcessStepTickResult.running(commanded, message);
        };
    }
}
