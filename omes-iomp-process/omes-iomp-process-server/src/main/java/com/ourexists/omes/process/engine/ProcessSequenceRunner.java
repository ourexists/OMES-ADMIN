package com.ourexists.omes.process.engine;

import com.ourexists.omes.process.engine.model.ProcessExecutionContext;
import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import com.ourexists.omes.process.engine.model.ProcessStepScript;
import com.ourexists.omes.process.engine.model.ProcessStepTickResult;
import com.ourexists.omes.process.model.StepEnginePhase;
import com.ourexists.omes.process.engine.spi.ProcessSignalProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 按脚本顺序驱动各工序步骤引擎，适合调度层周期性 tick 调用。
 */
@Component
@RequiredArgsConstructor
public class ProcessSequenceRunner {

    private final ProcessStepEngineRegistry engineRegistry;

    public RunningSequence start(ProcessStepScript script, ProcessSignalProvider signalProvider) {
        ProcessExecutionContext context = new ProcessExecutionContext(signalProvider);
        List<ProcessStepDefinition> steps = script.getSteps();
        ProcessStepEngine firstEngine = engineRegistry.resolve(steps.get(0));
        firstEngine.start(steps.get(0), context);
        return new RunningSequence(script, context, firstEngine, 0);
    }

    public ProcessStepTickResult tick(RunningSequence sequence, long deltaMs) {
        ProcessStepTickResult result = sequence.activeEngine.tick(sequence.context, deltaMs);
        if (result.getPhase() == StepEnginePhase.FAILED) {
            sequence.finished = true;
            return result;
        }
        if (result.getPhase() != StepEnginePhase.COMPLETED) {
            return result;
        }
        int nextIndex = sequence.stepIndex + 1;
        if (nextIndex >= sequence.script.getSteps().size()) {
            sequence.finished = true;
            return result;
        }
        ProcessStepDefinition nextStep = sequence.script.getSteps().get(nextIndex);
        ProcessStepEngine nextEngine = engineRegistry.resolve(nextStep);
        nextEngine.start(nextStep, sequence.context);
        sequence.activeEngine = nextEngine;
        sequence.stepIndex = nextIndex;
        return ProcessStepTickResult.running(sequence.context.getCommandedSetpoint(), "进入下一步骤");
    }

    @Getter
    public static class RunningSequence {

        private final ProcessStepScript script;
        private final ProcessExecutionContext context;
        private ProcessStepEngine activeEngine;
        private int stepIndex;
        private boolean finished;

        private final List<ProcessStepTickResult> history = new ArrayList<>();

        RunningSequence(ProcessStepScript script,
                        ProcessExecutionContext context,
                        ProcessStepEngine activeEngine,
                        int stepIndex) {
            this.script = script;
            this.context = context;
            this.activeEngine = activeEngine;
            this.stepIndex = stepIndex;
        }

        public void record(ProcessStepTickResult result) {
            history.add(result);
        }
    }
}
