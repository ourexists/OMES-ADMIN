package com.ourexists.omes.process.engine.support;

import com.ourexists.omes.process.engine.model.ProcessConditionKind;
import com.ourexists.omes.process.engine.model.ProcessConditionSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 从 {@code application.yml} 的 {@code process.condition} 加载 TIME / 完成动作等 Aviator 规则模板。
 */
@Component
@RequiredArgsConstructor
public class ProcessAviatorRuleCatalog {

    private static final String KEY_NONE = "none";
    private static final String KEY_TIME = "time";
    private static final String KEY_MANUAL = "manual-confirm";
    private static final String KEY_AUTO = "auto-next";

    private final ProcessAviatorRuleProperties properties;

    public String template(ProcessConditionKind kind) {
        return switch (kind) {
            case NONE -> requireRule(KEY_NONE);
            case TIME -> requireRule(KEY_TIME);
            case MANUAL_CONFIRM -> requireRule(KEY_MANUAL);
            case AUTO_NEXT -> requireRule(KEY_AUTO);
            default -> throw new IllegalArgumentException("无 Aviator 规则模板: " + kind);
        };
    }

    public String resolveForSpec(ProcessConditionSpec spec) {
        if (spec == null || !StringUtils.hasText(spec.getKind())) {
            return template(ProcessConditionKind.NONE);
        }
        ProcessConditionKind kind = spec.resolvedKind();
        if (kind == ProcessConditionKind.TIME) {
            return applyTimePlaceholders(template(kind), spec);
        }
        if (kind.isCompleteActionKind() || kind == ProcessConditionKind.NONE) {
            return template(kind);
        }
        throw new IllegalArgumentException("请使用 buildEventExpression 编译事件条件");
    }

    private String applyTimePlaceholders(String template, ProcessConditionSpec spec) {
        Integer duration = spec.getDuration();
        long durationMs = duration == null || duration <= 0 ? 0L : duration * 1000L;
        return template.replace("${durationMs}", Long.toString(durationMs));
    }

    private String requireRule(String key) {
        Map<String, String> rules = properties.getCondition();
        String rule = rules.get(key);
        if (!StringUtils.hasText(rule)) {
            return fallback(key);
        }
        return rule.trim();
    }

    private static String fallback(String key) {
        return switch (key) {
            case KEY_NONE -> "true";
            case KEY_TIME -> "elapsed(phaseKey) >= ${durationMs}";
            case KEY_MANUAL -> "manualConfirmed(phaseKey)";
            case KEY_AUTO -> "true";
            default -> "true";
        };
    }
}
