package com.ourexists.omes.process.engine.impl;

import com.ourexists.omes.process.engine.AbstractProcessStepEngine;
import com.ourexists.omes.process.engine.ProcessStepActionEngineResolver;
import com.ourexists.omes.process.engine.liteflow.ProcessLiteFlowSlot;
import com.ourexists.omes.process.engine.liteflow.ProcessLiteFlowSupport;
import com.ourexists.omes.process.engine.model.ProcessExecutionContext;
import com.ourexists.omes.process.engine.model.ProcessStepCombination;
import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import com.ourexists.omes.process.engine.model.ProcessStepMode;
import com.ourexists.omes.process.engine.model.ProcessStepTickResult;
import com.ourexists.omes.process.engine.model.ProcessStepType;
import com.ourexists.omes.process.model.StepEnginePhase;
import com.ourexists.omes.process.engine.support.ProcessAviatorEvaluator;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.flow.LiteflowResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * 工序单元引擎：LiteFlow 编排「驱动 → 动作 → 完成」，Aviator 求值条件。
 */
@Component
@RequiredArgsConstructor
public class CombinationStepEngine extends AbstractProcessStepEngine {

    private final FlowExecutor flowExecutor;
    private final ProcessStepActionEngineResolver actionEngineResolver;
    private final ProcessLiteFlowSupport liteFlowSupport;
    private final ProcessAviatorEvaluator aviatorEvaluator;

    private ProcessLiteFlowSlot slot;

    @Override
    public ProcessStepType supportedType() {
        return ProcessStepType.COMBINATION;
    }

    @Override
    public ProcessStepMode supportedMode() {
        return ProcessStepMode.UNIT;
    }

    @Override
    protected void onStart(ProcessStepDefinition definition, ProcessExecutionContext context) {
        if (CollectionUtils.isEmpty(definition.getCombinations())) {
            throw new IllegalArgumentException("COMBINATION 步骤 combinations 不能为空");
        }
        slot = new ProcessLiteFlowSlot();
        slot.setExecutionContext(context);
        slot.setCombinations(definition.getCombinations());
        slot.setActionEngineResolver(actionEngineResolver);
        slot.setComboIndex(0);
        slot.setEnginePhase(StepEnginePhase.RUNNING);
        liteFlowSupport.enterCombo(slot, aviatorEvaluator);
    }

    @Override
    public ProcessStepTickResult tick(ProcessExecutionContext context, long deltaMs) {
        if (phase != StepEnginePhase.RUNNING) {
            return ProcessStepTickResult.completed(context.getCommandedSetpoint(), "已结束");
        }
        context.addElapsed(deltaMs);
        slot.setDeltaMs(deltaMs);
        slot.setEnginePhase(StepEnginePhase.RUNNING);

        String chainId = slot.currentCombo().getChainId();
        if (chainId == null || chainId.isBlank()) {
            fail();
            return ProcessStepTickResult.builder()
                    .phase(StepEnginePhase.FAILED)
                    .commandedValue(context.getCommandedSetpoint())
                    .message("工序段未注册 LiteFlow 链，请重新保存工序脚本")
                    .build();
        }
        LiteflowResponse response = flowExecutor.execute2Resp(chainId, null, slot);
        if (!response.isSuccess()) {
            fail();
            return ProcessStepTickResult.builder()
                    .phase(StepEnginePhase.FAILED)
                    .commandedValue(context.getCommandedSetpoint())
                    .message("LiteFlow 执行失败: " + response.getMessage())
                    .build();
        }

        if (slot.getEnginePhase() == StepEnginePhase.COMPLETED) {
            complete();
        } else if (slot.getEnginePhase() == StepEnginePhase.FAILED) {
            fail();
        }
        return slot.toTickResult();
    }
}
