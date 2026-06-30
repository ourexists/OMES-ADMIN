package com.ourexists.omes.process.engine.liteflow;

import com.ourexists.omes.process.engine.model.ProcessConditionKind;
import com.ourexists.omes.process.engine.support.ProcessAviatorEvaluator;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;

@LiteflowComponent("processCompleteWait")
@RequiredArgsConstructor
public class ProcessCompleteWaitCmp extends NodeComponent {

    private final ProcessAviatorEvaluator aviatorEvaluator;
    private final ProcessLiteFlowSupport support;

    @Override
    public void process() {
        ProcessLiteFlowSlot slot = support.slot(this);
        String phaseKey = slot.getPhaseKey() + ":complete";
        if (support.evaluate(aviatorEvaluator, slot.currentCombo().getComplete(),
                slot.getExecutionContext(), phaseKey)) {
            aviatorEvaluator.resetPhase(slot.getExecutionContext(), phaseKey);
            support.advanceToNextCombo(
                    slot, aviatorEvaluator, slot.comboLabel() + "：完成动作已满足，进入下一工序");
        } else {
            ProcessConditionKind kind = support.resolveCompleteKind(slot.currentCombo().getComplete());
            slot.setMessage(slot.comboLabel() + (kind == ProcessConditionKind.MANUAL_CONFIRM
                    ? "：等待人工确认" : "：等待完成"));
        }
        support.stopChain(this);
    }

}
