package com.ourexists.omes.process.engine.impl;

import com.ourexists.omes.process.engine.AbstractProcessStepEngine;
import com.ourexists.omes.process.engine.model.ProcessExecutionContext;
import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import com.ourexists.omes.process.engine.model.ProcessStepMode;
import com.ourexists.omes.process.engine.model.ProcessStepTickResult;
import com.ourexists.omes.process.engine.model.ProcessStepType;
import com.ourexists.omes.process.model.StepEnginePhase;
import com.ourexists.omes.process.engine.support.PidController;
import com.ourexists.omes.process.engine.support.ProcessPidRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 反馈控制型：PID 闭环维持目标过程量。
 */
@Component
public class FeedbackControlStepEngine extends AbstractProcessStepEngine {

    private PidController pidController;
    private double target;
    private String equipmentCode;
    private String variable;

    @Override
    public ProcessStepType supportedType() {
        return ProcessStepType.PID_CONTROL;
    }

    @Override
    public ProcessStepMode supportedMode() {
        return ProcessStepMode.CONTROL;
    }

    @Override
    protected void onStart(ProcessStepDefinition definition, ProcessExecutionContext context) {
        if (definition.getTarget() == null) {
            throw new IllegalArgumentException("PID_CONTROL 步骤缺少 target");
        }
        if (!StringUtils.hasText(definition.getEquipmentCode())) {
            throw new IllegalArgumentException("PID_CONTROL 步骤缺少 equipmentCode（须绑定设备）");
        }
        target = definition.getTarget();
        equipmentCode = definition.getEquipmentCode().trim();
        variable = StringUtils.hasText(definition.getVariable()) ? definition.getVariable().trim() : "temp";
        pidController = ProcessPidRunner.newController(
                definition.getKp(), definition.getKi(), definition.getKd());
        context.setCommandedSetpoint(target);
    }

    @Override
    public ProcessStepTickResult tick(ProcessExecutionContext context, long deltaMs) {
        if (phase != StepEnginePhase.RUNNING) {
            return ProcessStepTickResult.completed(context.getCommandedSetpoint(), "已结束");
        }
        ProcessStepTickResult result = ProcessPidRunner.tick(
                context, deltaMs, target, pidController, equipmentCode, variable);
        if (result.getPhase() == StepEnginePhase.COMPLETED) {
            complete();
        }
        return result;
    }
}
