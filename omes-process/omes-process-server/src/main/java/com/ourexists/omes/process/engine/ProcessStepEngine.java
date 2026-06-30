package com.ourexists.omes.process.engine;

import com.ourexists.omes.process.engine.model.ProcessExecutionContext;
import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import com.ourexists.omes.process.engine.model.ProcessStepMode;
import com.ourexists.omes.process.engine.model.ProcessStepTickResult;
import com.ourexists.omes.process.engine.model.ProcessStepType;
import com.ourexists.omes.process.model.StepEnginePhase;

/**
 * 单条工艺工序步骤执行引擎。
 */
public interface ProcessStepEngine {

    ProcessStepType supportedType();

    ProcessStepMode supportedMode();

    void start(ProcessStepDefinition definition, ProcessExecutionContext context);

    ProcessStepTickResult tick(ProcessExecutionContext context, long deltaMs);

    StepEnginePhase phase();

    void cancel();
}
