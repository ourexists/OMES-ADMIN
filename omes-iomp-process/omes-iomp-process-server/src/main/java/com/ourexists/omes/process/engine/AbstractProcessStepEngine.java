package com.ourexists.omes.process.engine;

import com.ourexists.omes.process.engine.model.ProcessExecutionContext;
import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import com.ourexists.omes.process.model.StepEnginePhase;

import java.time.Instant;

public abstract class AbstractProcessStepEngine implements ProcessStepEngine {

    protected ProcessStepDefinition definition;
    protected StepEnginePhase phase = StepEnginePhase.IDLE;

    @Override
    public void start(ProcessStepDefinition definition, ProcessExecutionContext context) {
        this.definition = definition;
        this.phase = StepEnginePhase.RUNNING;
        context.markStepStarted(Instant.now());
        onStart(definition, context);
    }

    protected abstract void onStart(ProcessStepDefinition definition, ProcessExecutionContext context);

    @Override
    public StepEnginePhase phase() {
        return phase;
    }

    @Override
    public void cancel() {
        phase = StepEnginePhase.CANCELLED;
    }

    protected void complete() {
        phase = StepEnginePhase.COMPLETED;
    }

    protected void fail() {
        phase = StepEnginePhase.FAILED;
    }
}
