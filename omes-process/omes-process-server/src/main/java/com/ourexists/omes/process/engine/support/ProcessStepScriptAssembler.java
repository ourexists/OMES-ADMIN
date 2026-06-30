package com.ourexists.omes.process.engine.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.process.engine.liteflow.ProcessLiteFlowChainRegistry;
import com.ourexists.omes.process.engine.model.ProcessStepCombination;
import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 编译工序脚本并绑定 Aviator 表达式与 LiteFlow 段链。
 */
@Component
@RequiredArgsConstructor
public class ProcessStepScriptAssembler {

    private final ObjectMapper objectMapper;
    private final ProcessAviatorExpressionCompiler expressionCompiler;
    private final ProcessLiteFlowChainRegistry chainRegistry;

    public List<ProcessStepCombination> assembleFlow(JsonNode root, String stepScript, String stepId) {
        String scriptKey = chainRegistry.resolveScriptKey(stepId, stepScript);
        return ProcessStepFlowCompiler.compile(
                root, objectMapper, expressionCompiler, chainRegistry, scriptKey);
    }

    public ProcessStepDefinition assembleDefinition(String stepScript, String stepId) {
        try {
            JsonNode root = objectMapper.readTree(stepScript);
            List<ProcessStepCombination> combinations = assembleFlow(root, stepScript, stepId);
            if (CollectionUtils.isEmpty(combinations)) {
                return null;
            }
            ProcessStepDefinition definition = new ProcessStepDefinition();
            definition.setType("COMBINATION");
            definition.setMode("UNIT");
            definition.setScriptVersion(3);
            definition.setScriptChainKey(chainRegistry.resolveScriptKey(stepId, stepScript));
            definition.setCombinations(combinations);
            return definition;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("工序脚本组装失败: " + ex.getMessage());
        }
    }

    public void hotReload(String stepId, String stepScript) {
        if (!StringUtils.hasText(stepScript)) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(stepScript);
            assembleFlow(root, stepScript, stepId);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("工序脚本热刷新失败: " + ex.getMessage());
        }
    }
}
