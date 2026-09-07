package com.ourexists.omes.process.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.process.domain.BizProcessStep;
import com.ourexists.omes.process.engine.liteflow.ProcessLiteFlowChainRegistry;
import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import com.ourexists.omes.process.engine.recipe.ProcessRecipeAssembler;
import com.ourexists.omes.process.engine.support.ProcessStepScriptAssembler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 工序流程引擎编译配置：落库序列化 {@link ProcessStepDefinition}，执行时按 {@link ProcessEngineProperties} 选择来源。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessStepEngineConfigCodec {

    private final ObjectMapper objectMapper;
    private final ProcessStepScriptAssembler scriptAssembler;
    private final ProcessRecipeAssembler recipeAssembler;
    private final ProcessLiteFlowChainRegistry chainRegistry;
    private final ProcessEngineProperties engineProperties;

    @PostConstruct
    void logConfigSource() {
        log.info("工序引擎配置来源: {}（file=classpath 配方+params 编译，database=step_engine_config 字段）",
                engineProperties.getConfigSource());
    }

    /**
     * 从 stepScript / 名称模板编译引擎定义。
     */
    public Optional<ProcessStepDefinition> compileDefinition(BizProcessStep step, String equipmentCodeOverride) {
        if (step == null) {
            return Optional.empty();
        }
        Optional<ProcessStepDefinition> fromRecipe = recipeAssembler.assembleByStepName(
                step.getStepName(), equipmentCodeOverride, step.getId(), step.getStepScript(), step.getParams());
        if (fromRecipe.isPresent()) {
            return fromRecipe;
        }
        return parseStepScript(step.getStepScript(), step.getId());
    }

    private Optional<ProcessStepDefinition> parseStepScript(String stepScript, String stepId) {
        if (!StringUtils.hasText(stepScript) || !stepScript.trim().startsWith("{")) {
            return Optional.empty();
        }
        try {
            ProcessStepDefinition definition = scriptAssembler.assembleDefinition(stepScript.trim(), stepId);
            return Optional.ofNullable(definition);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("工序执行脚本解析失败: " + ex.getMessage());
        }
    }

    public String compileAndSerialize(BizProcessStep step, String equipmentCodeOverride) {
        if (engineProperties.isFileSource()) {
            return null;
        }
        return compileDefinition(step, equipmentCodeOverride)
                .map(this::serialize)
                .orElse(null);
    }

    public String serialize(ProcessStepDefinition definition) {
        try {
            return objectMapper.writeValueAsString(definition);
        } catch (Exception ex) {
            throw new BusinessException("引擎配置序列化失败: " + ex.getMessage());
        }
    }

    /**
     * 执行时加载：database 模式优先 {@code step_engine_config}；file 模式始终现场编译（配方模板 + params）。
     */
    public Optional<ProcessStepDefinition> resolve(BizProcessStep step) {
        if (step == null) {
            return Optional.empty();
        }
        if (engineProperties.isFileSource()) {
            return compileDefinition(step, null);
        }
        if (StringUtils.hasText(step.getStepEngineConfig())) {
            return Optional.of(deserialize(step.getStepEngineConfig(), step.getId()));
        }
        return compileDefinition(step, null);
    }

    public ProcessStepDefinition deserialize(String engineConfigJson, String stepId) {
        if (!StringUtils.hasText(engineConfigJson)) {
            throw new BusinessException("引擎配置为空");
        }
        try {
            ProcessStepDefinition definition = objectMapper.readValue(
                    engineConfigJson.trim(), ProcessStepDefinition.class);
            registerChains(definition, stepId, engineConfigJson);
            return definition;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("引擎配置解析失败: " + ex.getMessage());
        }
    }

    private void registerChains(ProcessStepDefinition definition, String stepId, String engineConfigJson) {
        if (definition == null || CollectionUtils.isEmpty(definition.getCombinations())) {
            return;
        }
        String scriptKey = StringUtils.hasText(definition.getScriptChainKey())
                ? definition.getScriptChainKey()
                : chainRegistry.resolveScriptKey(stepId, engineConfigJson);
        definition.setScriptChainKey(scriptKey);
        chainRegistry.registerScriptChains(scriptKey, definition.getCombinations());
    }
}
