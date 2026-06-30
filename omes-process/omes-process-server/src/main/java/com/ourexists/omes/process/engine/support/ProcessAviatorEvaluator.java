package com.ourexists.omes.process.engine.support;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.ourexists.omes.process.engine.model.ProcessConditionKind;
import com.ourexists.omes.process.engine.model.ProcessConditionSpec;
import com.ourexists.omes.process.engine.model.ProcessExecutionContext;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用 Aviator 求值工序条件表达式。
 */
@Component
public class ProcessAviatorEvaluator {

    private static final String ENV_CTX = "ctx";
    private static final String ENV_PHASE_KEY = "phaseKey";

    private final AviatorEvaluatorInstance aviator = AviatorEvaluator.newInstance();
    private final Map<String, Expression> compiledCache = new ConcurrentHashMap<>();

    @PostConstruct
    void registerFunctions() {
        aviator.addFunction(new PvFunction());
        aviator.addFunction(new ElapsedFunction());
        aviator.addFunction(new ManualConfirmedFunction());
    }

    public boolean evaluate(ProcessConditionSpec spec,
                            ProcessExecutionContext context,
                            String phaseKey) {
        if (spec == null || spec.resolvedKind() == ProcessConditionKind.NONE) {
            return true;
        }
        ProcessConditionKind kind = spec.resolvedKind();
        if (kind == ProcessConditionKind.AUTO_NEXT) {
            throw new IllegalArgumentException("AUTO_NEXT 应由组合引擎直接处理");
        }
        if (kind == ProcessConditionKind.MANUAL_CONFIRM) {
            return matchManualConfirm(context, phaseKey);
        }
        String expression = resolveExpression(spec, kind);
        if (!StringUtils.hasText(expression)) {
            return kind != ProcessConditionKind.EVENT;
        }
        Map<String, Object> env = Map.of(
                ENV_CTX, context,
                ENV_PHASE_KEY, phaseKey != null ? phaseKey : ""
        );
        Expression compiled = compiledCache.computeIfAbsent(expression, aviator::compile);
        Object result = compiled.execute(env);
        if (result instanceof Boolean bool) {
            return bool;
        }
        if (result instanceof Number number) {
            return number.doubleValue() != 0D;
        }
        return false;
    }

    private static String resolveExpression(ProcessConditionSpec spec, ProcessConditionKind kind) {
        if (StringUtils.hasText(spec.getAviatorExpression())) {
            return spec.getAviatorExpression();
        }
        if (kind == ProcessConditionKind.EVENT && StringUtils.hasText(spec.getCondition())) {
            return ProcessAviatorExpressionBuilder.compileLegacyCondition(spec.getCondition().trim());
        }
        throw new IllegalStateException("条件未编译 Aviator 表达式: " + kind);
    }

    private static boolean matchManualConfirm(ProcessExecutionContext context, String phaseKey) {
        String key = StringUtils.hasText(phaseKey) ? phaseKey : "complete";
        return context.getSignalProvider().isCompleteConfirmed(key);
    }

    public void resetPhase(ProcessExecutionContext context, String phaseKey) {
        if (!StringUtils.hasText(phaseKey)) {
            return;
        }
        context.putAttr("conditionSince:" + phaseKey, null);
        context.getSignalProvider().resetCompleteConfirm(phaseKey);
    }

    private final class PvFunction extends AbstractFunction {

        @Override
        public String getName() {
            return "pv";
        }

        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
            ProcessExecutionContext ctx = (ProcessExecutionContext) env.get(ENV_CTX);
            String equipment = FunctionUtils.getStringValue(arg1, env);
            String variable = FunctionUtils.getStringValue(arg2, env);
            double value = ProcessVariableReader.readProcessVariable(ctx, equipment, variable);
            return AviatorDouble.valueOf(value);
        }
    }

    private final class ElapsedFunction extends AbstractFunction {

        @Override
        public String getName() {
            return "elapsed";
        }

        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
            ProcessExecutionContext ctx = (ProcessExecutionContext) env.get(ENV_CTX);
            String phaseKey = FunctionUtils.getStringValue(arg1, env);
            String key = "conditionSince:" + phaseKey;
            Instant since = ctx.attr(key);
            Instant now = Instant.now();
            if (since == null) {
                ctx.putAttr(key, now);
                return AviatorDouble.valueOf(0D);
            }
            return AviatorDouble.valueOf(now.toEpochMilli() - since.toEpochMilli());
        }
    }

    private final class ManualConfirmedFunction extends AbstractFunction {

        @Override
        public String getName() {
            return "manualConfirmed";
        }

        @Override
        public AviatorObject call(Map<String, Object> env, AviatorObject arg1) {
            ProcessExecutionContext ctx = (ProcessExecutionContext) env.get(ENV_CTX);
            String phaseKey = FunctionUtils.getStringValue(arg1, env);
            return AviatorBoolean.valueOf(matchManualConfirm(ctx, phaseKey));
        }
    }
}
