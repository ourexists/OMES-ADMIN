package com.ourexists.omes.process.engine.liteflow;

import com.ourexists.omes.process.engine.model.ProcessStepTickResult;
import com.ourexists.omes.process.model.StepEnginePhase;
import com.ourexists.omes.process.engine.support.ProcessAviatorEvaluator;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;

@LiteflowComponent("processActionTick")
@RequiredArgsConstructor
public class ProcessActionTickCmp extends NodeComponent {

    private final ProcessAviatorEvaluator aviatorEvaluator;
    private final ProcessLiteFlowSupport support;

    @Override
    public void process() {
        ProcessLiteFlowSlot slot = support.slot(this);
        if (slot.getActionEngine() == null) {
            support.startAction(slot);
        }
        ProcessStepTickResult actionResult =
                slot.getActionEngine().tick(slot.getExecutionContext(), slot.getDeltaMs());
        if (actionResult.getPhase() != StepEnginePhase.COMPLETED) {
            support.mergeActionMessage(slot, actionResult);
            support.stopChain(this);
            return;
        }
        slot.setActionEngine(null);
        support.completeCurrentPhaseAndAdvance(
                slot, aviatorEvaluator, slot.comboLabel() + "：执行动作已完成");
        support.stopChain(this);
    }
}
