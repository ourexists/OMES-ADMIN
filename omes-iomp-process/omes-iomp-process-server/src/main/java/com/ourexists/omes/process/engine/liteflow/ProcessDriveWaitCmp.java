package com.ourexists.omes.process.engine.liteflow;

import com.ourexists.omes.process.engine.support.ProcessAviatorEvaluator;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;

@LiteflowComponent("processDriveWait")
@RequiredArgsConstructor
public class ProcessDriveWaitCmp extends NodeComponent {

    private final ProcessAviatorEvaluator aviatorEvaluator;
    private final ProcessLiteFlowSupport support;

    @Override
    public void process() {
        ProcessLiteFlowSlot slot = support.slot(this);
        String phaseKey = slot.getPhaseKey() + ":drive";
        if (support.evaluate(aviatorEvaluator, slot.currentCombo().getDrive(),
                slot.getExecutionContext(), phaseKey)) {
            aviatorEvaluator.resetPhase(slot.getExecutionContext(), phaseKey);
            support.completeCurrentPhaseAndAdvance(
                    slot, aviatorEvaluator, slot.comboLabel() + "：驱动条件已满足");
        } else {
            slot.setMessage(slot.comboLabel() + "：等待驱动条件");
        }
        support.stopChain(this);
    }
}
