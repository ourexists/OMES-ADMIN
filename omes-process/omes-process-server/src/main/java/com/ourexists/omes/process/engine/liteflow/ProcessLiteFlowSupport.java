package com.ourexists.omes.process.engine.liteflow;

import com.ourexists.omes.process.engine.model.ProcessConditionKind;
import com.ourexists.omes.process.engine.model.ProcessConditionSpec;
import com.ourexists.omes.process.engine.model.ProcessExecutionContext;
import com.ourexists.omes.process.engine.model.ProcessSegmentPhase;
import com.ourexists.omes.process.engine.model.ProcessStepCombination;
import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import com.ourexists.omes.process.engine.model.ProcessStepTickResult;
import com.ourexists.omes.process.model.StepEnginePhase;
import com.ourexists.omes.process.engine.support.ProcessAviatorEvaluator;
import com.yomahub.liteflow.core.NodeComponent;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class ProcessLiteFlowSupport {

    private static final List<ProcessSegmentPhase> DEFAULT_PHASE_ORDER = List.of(
            ProcessSegmentPhase.DRIVE,
            ProcessSegmentPhase.ACTION,
            ProcessSegmentPhase.COMPLETE);

    public ProcessLiteFlowSlot slot(NodeComponent bind) {
        return bind.getContextBean(ProcessLiteFlowSlot.class);
    }

    public void stopChain(NodeComponent bind) {
        bind.setIsEnd(true);
    }

    public boolean evaluate(ProcessAviatorEvaluator evaluator,
                          ProcessConditionSpec spec,
                          ProcessExecutionContext context,
                          String phaseKey) {
        if (spec == null || spec.resolvedKind() == ProcessConditionKind.NONE) {
            return true;
        }
        return evaluator.evaluate(spec, context, phaseKey);
    }

    public ProcessConditionKind resolveCompleteKind(ProcessConditionSpec complete) {
        if (complete == null || !StringUtils.hasText(complete.getKind())) {
            throw new IllegalStateException("完成动作未配置");
        }
        ProcessConditionKind kind = complete.resolvedKind();
        if (!kind.isCompleteActionKind()) {
            throw new IllegalStateException("无效的完成动作类型: " + complete.getKind());
        }
        return kind;
    }

    public void failSlot(ProcessLiteFlowSlot slot, String message) {
        slot.setEnginePhase(StepEnginePhase.FAILED);
        slot.setMessage(message);
    }

    public void mergeActionMessage(ProcessLiteFlowSlot slot, ProcessStepTickResult actionResult) {
        String prefix = slot.comboLabel() + "：";
        String detail = actionResult.getMessage() != null ? actionResult.getMessage() : "";
        slot.setMessage(prefix + detail);
    }

    public List<ProcessSegmentPhase> resolvePhaseOrder(ProcessStepCombination combo) {
        if (combo != null && !CollectionUtils.isEmpty(combo.getPhaseOrder())) {
            return combo.getPhaseOrder();
        }
        return DEFAULT_PHASE_ORDER;
    }

    public void enterCombo(ProcessLiteFlowSlot slot, ProcessAviatorEvaluator evaluator) {
        slot.setPhaseKey("combo" + slot.getComboIndex());
        slot.setPhaseStepIndex(0);
        slot.setActionEngine(null);
        ProcessExecutionContext ctx = slot.getExecutionContext();
        String key = slot.getPhaseKey();
        evaluator.resetPhase(ctx, key + ":drive");
        evaluator.resetPhase(ctx, key + ":complete");
        evaluator.resetPhase(ctx, key + ":exception");
        enterPhaseAt(slot, 0, evaluator);
    }

    /** 当前阶段完成后进入主链上下一阶段；若本段已全部完成则进入下一组合。 */
    public void completeCurrentPhaseAndAdvance(ProcessLiteFlowSlot slot,
                                               ProcessAviatorEvaluator evaluator,
                                               String doneMessage) {
        if (doneMessage != null && !doneMessage.isBlank()) {
            slot.setMessage(doneMessage);
        }
        int nextIndex = slot.getPhaseStepIndex() + 1;
        if (!enterPhaseAt(slot, nextIndex, evaluator)) {
            advanceToNextCombo(slot, evaluator, slot.getMessage());
        }
    }

    public boolean enterPhaseAt(ProcessLiteFlowSlot slot,
                                int fromIndex,
                                ProcessAviatorEvaluator evaluator) {
        ProcessStepCombination combo = slot.currentCombo();
        List<ProcessSegmentPhase> order = resolvePhaseOrder(combo);
        boolean autoNext = isAutoNextComplete(combo);
        for (int i = fromIndex; i < order.size(); i++) {
            ProcessSegmentPhase kind = order.get(i);
            if (kind == ProcessSegmentPhase.COMPLETE && autoNext) {
                continue;
            }
            slot.setPhaseStepIndex(i);
            slot.setPhase(ProcessComboPhase.fromSegmentPhase(kind));
            switch (kind) {
                case DRIVE -> slot.setMessage(slot.comboLabel() + "：等待驱动条件");
                case ACTION -> {
                    startAction(slot);
                    slot.setMessage(slot.comboLabel() + "：执行动作中");
                }
                case COMPLETE -> {
                    evaluator.resetPhase(slot.getExecutionContext(), slot.getPhaseKey() + ":complete");
                    slot.setMessage(slot.comboLabel() + "：等待人工确认");
                }
            }
            return true;
        }
        return false;
    }

    public void advanceToNextCombo(ProcessLiteFlowSlot slot,
                                   ProcessAviatorEvaluator evaluator,
                                   String message) {
        if (slot.getComboIndex() >= slot.getCombinations().size() - 1) {
            slot.setEnginePhase(StepEnginePhase.COMPLETED);
            slot.setMessage(message != null && !message.isBlank()
                    ? message : "工序全部驱动组合已完成");
            return;
        }
        slot.setComboIndex(slot.getComboIndex() + 1);
        enterCombo(slot, evaluator);
        if (message != null && !message.isBlank()) {
            slot.setMessage(message);
        }
    }

    private static boolean isAutoNextComplete(ProcessStepCombination combo) {
        return combo.getComplete() != null
                && combo.getComplete().resolvedKind() == ProcessConditionKind.AUTO_NEXT;
    }

    public void startAction(ProcessLiteFlowSlot slot) {
        ProcessStepCombination combo = slot.currentCombo();
        ProcessStepDefinition action = combo.getAction();
        if (action == null || action.getType() == null) {
            throw new IllegalArgumentException(slot.comboLabel() + " 缺少执行动作");
        }
        if (slot.getActionEngine() != null) {
            return;
        }
        var engine = slot.getActionEngineResolver().resolve(action);
        engine.start(action, slot.getExecutionContext());
        slot.setActionEngine(engine);
    }
}
