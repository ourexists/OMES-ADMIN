package com.ourexists.omes.process.engine.support;

import com.ourexists.omes.process.engine.model.ProcessExecutionContext;
import com.ourexists.omes.process.engine.model.ProcessStepTickResult;
import com.ourexists.omes.process.model.StepEnginePhase;
import org.springframework.util.StringUtils;

/**
 * 共用 PID 控制节拍逻辑（斜坡后控制、独立 PID 动作）。
 */
public final class ProcessPidRunner {

    private static final double DEFAULT_KP = 1.2D;
    private static final double DEFAULT_KI = 0.05D;
    private static final double DEFAULT_KD = 0.2D;
    private static final double BAND = 0.5D;

    private ProcessPidRunner() {
    }

    public static PidController newController(Double kp, Double ki, Double kd) {
        double p = kp != null ? kp : DEFAULT_KP;
        double i = ki != null ? ki : DEFAULT_KI;
        double d = kd != null ? kd : DEFAULT_KD;
        return new PidController(p, i, d);
    }

    public static ProcessStepTickResult tick(ProcessExecutionContext context,
                                             long deltaMs,
                                             double target,
                                             PidController pidController,
                                             String equipmentCode,
                                             String variable) {
        context.addElapsed(deltaMs);
        if (StringUtils.hasText(equipmentCode)) {
            context.putAttr("activeEquipmentCode", equipmentCode.trim());
        }
        String var = StringUtils.hasText(variable) ? variable.trim() : "temp";
        double actual = EventConditionEvaluator.readProcessVariable(context, var);
        double deltaSeconds = deltaMs / 1000D;
        double output = pidController.compute(target, actual, deltaSeconds);
        double commanded = target + output;
        context.setCommandedSetpoint(commanded);

        if (Math.abs(actual - target) <= BAND && context.getStepElapsedMs() >= 1000L) {
            return ProcessStepTickResult.completed(commanded, "设备控制已进入目标带");
        }
        return ProcessStepTickResult.running(commanded, "设备 PID 控制中");
    }

    public static ProcessStepTickResult tickCompleted(ProcessExecutionContext context) {
        return ProcessStepTickResult.builder()
                .phase(StepEnginePhase.COMPLETED)
                .commandedValue(context.getCommandedSetpoint())
                .message("已结束")
                .build();
    }
}
