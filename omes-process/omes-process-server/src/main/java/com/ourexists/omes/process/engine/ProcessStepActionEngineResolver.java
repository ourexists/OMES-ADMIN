package com.ourexists.omes.process.engine;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import com.ourexists.omes.process.engine.model.ProcessStepMode;
import com.ourexists.omes.process.engine.model.ProcessStepType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 解析执行动作引擎（不含 COMBINATION 单元引擎）。
 * 延迟加载引擎列表，避免与 {@link CombinationStepEngine} 构造期循环依赖。
 */
@Component
public class ProcessStepActionEngineResolver {

    private final ObjectProvider<List<ProcessStepEngine>> enginesProvider;
    private volatile Map<String, ProcessStepEngine> actionEngines;

    public ProcessStepActionEngineResolver(ObjectProvider<List<ProcessStepEngine>> enginesProvider) {
        this.enginesProvider = enginesProvider;
    }

    public ProcessStepEngine resolve(ProcessStepDefinition action) {
        ProcessStepType type = action.resolvedType();
        ProcessStepMode mode = action.resolvedMode();
        ProcessStepEngine engine = actionEngines().get(key(type, mode));
        if (engine == null) {
            throw new BusinessException("不支持的执行动作: type=" + type + ", mode=" + mode);
        }
        return engine;
    }

    private Map<String, ProcessStepEngine> actionEngines() {
        Map<String, ProcessStepEngine> cached = actionEngines;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            if (actionEngines == null) {
                actionEngines = enginesProvider.getObject().stream()
                        .filter(engine -> engine.supportedType() != ProcessStepType.COMBINATION)
                        .collect(Collectors.toUnmodifiableMap(
                                engine -> key(engine.supportedType(), engine.supportedMode()),
                                Function.identity(),
                                (left, right) -> {
                                    throw new IllegalStateException("重复的动作步骤引擎: "
                                            + left.supportedType() + "/" + left.supportedMode());
                                }));
            }
            return actionEngines;
        }
    }

    private static String key(ProcessStepType type, ProcessStepMode mode) {
        return type.name() + "#" + mode.name();
    }
}
