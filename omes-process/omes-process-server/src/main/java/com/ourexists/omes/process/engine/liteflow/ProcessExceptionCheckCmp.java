package com.ourexists.omes.process.engine.liteflow;

import com.ourexists.omes.process.engine.model.ProcessConditionKind;
import com.ourexists.omes.process.engine.support.ProcessAviatorEvaluator;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.core.NodeComponent;
import lombok.RequiredArgsConstructor;

@LiteflowComponent("processExceptionCheck")
@RequiredArgsConstructor
public class ProcessExceptionCheckCmp extends NodeComponent {

    private final ProcessAviatorEvaluator aviatorEvaluator;
    private final ProcessLiteFlowSupport support;

    @Override
    public void process() {
        ProcessLiteFlowSlot slot = support.slot(this);
        var exception = slot.currentCombo().getException();
        if (exception != null
                && exception.resolvedKind() != ProcessConditionKind.NONE
                && support.evaluate(aviatorEvaluator, exception, slot.getExecutionContext(),
                slot.getPhaseKey() + ":exception")) {
            support.failSlot(slot, "触发异常条件，工序中止");
            support.stopChain(this);
        }
    }
}
