package com.ourexists.omes.process.engine.support;

import com.ourexists.omes.process.engine.model.ProcessConditionKind;
import com.ourexists.omes.process.engine.model.ProcessConditionSpec;
import com.ourexists.omes.process.engine.model.ProcessExecutionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 判断驱动 / 完成动作 / 异常条件是否满足（底层由 Aviator 求值）。
 */
@Component
@RequiredArgsConstructor
public class ProcessConditionMatcher {

    private final ProcessAviatorEvaluator aviatorEvaluator;

    public boolean isSatisfied(ProcessConditionSpec spec, ProcessExecutionContext context) {
        return isSatisfied(spec, context, null);
    }

    public boolean isSatisfied(ProcessConditionSpec spec,
                               ProcessExecutionContext context,
                               String phaseKey) {
        if (spec == null || spec.resolvedKind() == ProcessConditionKind.NONE) {
            return true;
        }
        ProcessConditionKind kind = spec.resolvedKind();
        if (kind == ProcessConditionKind.AUTO_NEXT) {
            throw new IllegalArgumentException("完成动作 AUTO_NEXT 应由组合引擎直接处理");
        }
        return aviatorEvaluator.evaluate(spec, context, phaseKey);
    }

    public void resetPhase(ProcessExecutionContext context, String phaseKey) {
        aviatorEvaluator.resetPhase(context, phaseKey);
    }
}
