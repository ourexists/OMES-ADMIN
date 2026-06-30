package com.ourexists.omes.process.engine.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.omes.process.engine.model.ProcessConditionKind;
import com.ourexists.omes.process.engine.model.ProcessConditionSpec;
import com.ourexists.omes.process.engine.model.ProcessStepCombination;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 为驱动组合各条件编译 Aviator 表达式（事件逻辑 + 规则模板）。
 */
@Component
@RequiredArgsConstructor
public class ProcessAviatorExpressionCompiler {

    private final ProcessAviatorRuleCatalog ruleCatalog;

    public void enrichCombination(ProcessStepCombination combo,
                                 JsonNode driveJson,
                                 JsonNode exceptionItemJson,
                                 ObjectMapper objectMapper) {
        compileCondition(combo.getDrive(), driveJson, objectMapper);
        compileCondition(combo.getException(), exceptionItemJson, objectMapper);
        compileComplete(combo.getComplete());
    }

    public void compileCondition(ProcessConditionSpec spec, JsonNode sourceJson, ObjectMapper objectMapper) {
        if (spec == null) {
            return;
        }
        ProcessConditionKind kind = spec.resolvedKind();
        if (kind == ProcessConditionKind.EVENT) {
            spec.setAviatorExpression(ProcessAviatorExpressionBuilder.buildEventExpression(spec, sourceJson));
        } else {
            spec.setAviatorExpression(ruleCatalog.resolveForSpec(spec));
        }
    }

    public void compileComplete(ProcessConditionSpec spec) {
        if (spec == null) {
            return;
        }
        spec.setAviatorExpression(ruleCatalog.resolveForSpec(spec));
    }
}
