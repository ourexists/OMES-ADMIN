package com.ourexists.omes.process.engine.impl;

import com.ourexists.omes.process.engine.AbstractProcessStepEngine;
import com.ourexists.omes.process.engine.model.ProcessExecutionContext;
import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import com.ourexists.omes.process.engine.model.ProcessStepMode;
import com.ourexists.omes.process.engine.model.ProcessStepTickResult;
import com.ourexists.omes.process.engine.model.ProcessStepTimeSegment;
import com.ourexists.omes.process.engine.model.ProcessStepType;
import com.ourexists.omes.process.engine.model.RampAfterControlSpec;
import com.ourexists.omes.process.model.StepEnginePhase;
import com.ourexists.omes.process.engine.support.EventConditionEvaluator;
import com.ourexists.omes.process.engine.support.PidController;
import com.ourexists.omes.process.engine.support.ProcessPidRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 时间驱动：segments 顺序斜坡（可选保持）→ 可选斜坡后设备 PID 控制。
 */
@Component
public class TimeDrivenStepEngine extends AbstractProcessStepEngine {

    private enum SegmentPhase {
        RAMP, HOLD, CONTROL
    }

    private List<ProcessStepTimeSegment> segments;
    private int segmentIndex;
    private SegmentPhase segmentPhase;
    private double fromValue;
    private double toValue;
    private long segmentDurationMs;
    private long holdDurationMs;
    private long segmentElapsedMs;

    private RampAfterControlSpec afterControl;
    private String controlEquipmentCode;
    private String controlVariable;
    private double controlTarget;
    private PidController pidController;

    @Override
    public ProcessStepType supportedType() {
        return ProcessStepType.RAMP;
    }

    @Override
    public ProcessStepMode supportedMode() {
        return ProcessStepMode.TIME;
    }

    @Override
    protected void onStart(ProcessStepDefinition definition, ProcessExecutionContext context) {
        segments = definition.resolvedTimeSegments();
        segmentIndex = 0;
        afterControl = definition.getAfterControl();
        controlEquipmentCode = definition.getEquipmentCode();
        controlVariable = StringUtils.hasText(definition.getVariable())
                ? definition.getVariable().trim() : "temp";

        if (!StringUtils.hasText(definition.getEquipmentCode())) {
            throw new IllegalArgumentException("斜坡动作须绑定 equipmentCode");
        }
        String equipmentCode = definition.getEquipmentCode().trim();
        context.putAttr("activeEquipmentCode", equipmentCode);
        context.setCommandedSetpoint(
                EventConditionEvaluator.readProcessVariable(context, controlVariable));
        beginSegment(context);
    }

    @Override
    public ProcessStepTickResult tick(ProcessExecutionContext context, long deltaMs) {
        if (phase != StepEnginePhase.RUNNING) {
            return ProcessStepTickResult.completed(context.getCommandedSetpoint(), "已结束");
        }

        if (segmentPhase == SegmentPhase.CONTROL) {
            ProcessStepTickResult result = ProcessPidRunner.tick(
                    context, deltaMs, controlTarget,
                    pidController, controlEquipmentCode, controlVariable);
            if (result.getPhase() == StepEnginePhase.COMPLETED) {
                complete();
                return ProcessStepTickResult.completed(
                        result.getCommandedValue(),
                        "斜坡与设备控制完成，最终值 " + String.format("%.1f", result.getCommandedValue()));
            }
            return result;
        }

        context.addElapsed(deltaMs);
        segmentElapsedMs += Math.max(0L, deltaMs);

        if (segmentPhase == SegmentPhase.RAMP) {
            return tickRamp(context);
        }
        return tickHold(context);
    }

    private ProcessStepTickResult tickRamp(ProcessExecutionContext context) {
        double ratio = segmentDurationMs <= 0 ? 1D
                : Math.min(1D, (double) segmentElapsedMs / segmentDurationMs);
        double value = fromValue + (toValue - fromValue) * ratio;
        context.setCommandedSetpoint(value);

        if (segmentElapsedMs < segmentDurationMs) {
            return ProcessStepTickResult.running(value, formatRampProgress(false));
        }

        context.setCommandedSetpoint(toValue);
        if (holdDurationMs > 0) {
            segmentPhase = SegmentPhase.HOLD;
            segmentElapsedMs = 0L;
            return ProcessStepTickResult.running(toValue, formatHoldProgress(false));
        }
        return finishSegmentOrAdvance(context);
    }

    private ProcessStepTickResult tickHold(ProcessExecutionContext context) {
        context.setCommandedSetpoint(toValue);
        if (segmentElapsedMs < holdDurationMs) {
            return ProcessStepTickResult.running(toValue, formatHoldProgress(false));
        }
        return finishSegmentOrAdvance(context);
    }

    private ProcessStepTickResult finishSegmentOrAdvance(ProcessExecutionContext context) {
        if (segmentIndex < segments.size() - 1) {
            segmentIndex++;
            beginSegment(context);
            return ProcessStepTickResult.running(
                    context.getCommandedSetpoint(),
                    formatRampProgress(true));
        }
        if (shouldRunAfterControl()) {
            beginAfterControl(context);
            return ProcessStepTickResult.running(
                    context.getCommandedSetpoint(),
                    String.format("斜坡完成，开始设备控制→%.1f", controlTarget));
        }
        complete();
        return ProcessStepTickResult.completed(
                toValue,
                String.format("斜坡完成，最终值 %.1f", toValue));
    }

    private boolean shouldRunAfterControl() {
        return afterControl != null
                && Boolean.TRUE.equals(afterControl.getEnabled())
                && afterControl.getTarget() != null;
    }

    private void beginAfterControl(ProcessExecutionContext context) {
        segmentPhase = SegmentPhase.CONTROL;
        controlTarget = afterControl.getTarget();
        if (StringUtils.hasText(afterControl.getEquipmentCode())) {
            controlEquipmentCode = afterControl.getEquipmentCode().trim();
        }
        if (StringUtils.hasText(afterControl.getVariable())) {
            controlVariable = afterControl.getVariable().trim();
        }
        pidController = ProcessPidRunner.newController(
                afterControl.getKp(), afterControl.getKi(), afterControl.getKd());
        context.markStepStarted(java.time.Instant.now());
        context.setCommandedSetpoint(controlTarget);
    }

    private void beginSegment(ProcessExecutionContext context) {
        ProcessStepTimeSegment segment = segments.get(segmentIndex);
        segmentPhase = SegmentPhase.RAMP;
        fromValue = context.getCommandedSetpoint();
        toValue = segment.getTo();
        segmentDurationMs = segment.getDuration() * 1000L;
        holdDurationMs = segment.getHoldDuration() != null && segment.getHoldDuration() > 0
                ? segment.getHoldDuration() * 1000L : 0L;
        segmentElapsedMs = 0L;
    }

    private String formatRampProgress(boolean entering) {
        ProcessStepTimeSegment segment = segments.get(segmentIndex);
        int seconds = segment.getDuration() != null ? segment.getDuration() : 0;
        String range = String.format("%.1f→%.1f", fromValue, toValue);
        if (entering) {
            return String.format("进入第 %d/%d 段：%ds 内 %s", segmentIndex + 1, segments.size(), seconds, range);
        }
        return String.format("第 %d/%d 段：%ds 内 %s", segmentIndex + 1, segments.size(), seconds, range);
    }

    private String formatHoldProgress(boolean entering) {
        int holdSec = (int) (holdDurationMs / 1000L);
        if (entering) {
            return String.format("第 %d/%d 段：保持 %.1f 共 %ds", segmentIndex + 1, segments.size(), toValue, holdSec);
        }
        return String.format("第 %d/%d 段：保持中 %.1f（%ds）", segmentIndex + 1, segments.size(), toValue, holdSec);
    }
}
