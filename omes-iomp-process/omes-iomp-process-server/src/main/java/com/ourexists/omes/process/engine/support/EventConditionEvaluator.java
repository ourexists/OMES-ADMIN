package com.ourexists.omes.process.engine.support;

import com.ourexists.omes.process.engine.model.ProcessExecutionContext;

/**
 * 过程变量读取（事件表达式由 {@link ProcessAviatorEvaluator} 求值）。
 */
public final class EventConditionEvaluator {

    private EventConditionEvaluator() {
    }

    public static double readProcessVariable(ProcessExecutionContext context, String variable) {
        return ProcessVariableReader.readProcessVariable(context, variable);
    }
}
