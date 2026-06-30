package com.ourexists.omes.process.engine;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import com.ourexists.omes.process.engine.model.ProcessStepMode;
import com.ourexists.omes.process.engine.model.ProcessStepType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProcessStepEngineRegistry {

    private final Map<String, ProcessStepEngine> engines;

    public ProcessStepEngineRegistry(List<ProcessStepEngine> engineList) {
        this.engines = engineList.stream()
                .collect(Collectors.toUnmodifiableMap(
                        engine -> key(engine.supportedType(), engine.supportedMode()),
                        Function.identity(),
                        (left, right) -> {
                            throw new IllegalStateException("重复的工序步骤引擎: "
                                    + left.supportedType() + "/" + left.supportedMode());
                        }));
    }

    public ProcessStepEngine resolve(ProcessStepDefinition definition) {
        ProcessStepType type = definition.resolvedType();
        ProcessStepMode mode = definition.resolvedMode();
        ProcessStepEngine engine = engines.get(key(type, mode));
        if (engine == null) {
            throw new BusinessException("不支持的工序步骤: type=" + type + ", mode=" + mode);
        }
        return engine;
    }

    private static String key(ProcessStepType type, ProcessStepMode mode) {
        return type.name() + "#" + mode.name();
    }
}
