package com.ourexists.omes.process.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.process.domain.BizProcessStep;
import com.ourexists.omes.process.model.ProcessStepScriptHotReloadSegmentVO;
import com.ourexists.omes.process.model.ProcessStepScriptHotReloadVO;
import com.ourexists.omes.process.engine.model.ProcessStepCombination;
import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import com.ourexists.omes.process.engine.model.ProcessStepScript;
import com.ourexists.omes.process.engine.recipe.ProcessRecipeAssembler;
import com.ourexists.omes.process.engine.support.ProcessStepScriptAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 工序执行脚本 {@code stepScript} 编解码（v3 流程图格式）。
 */
@Component
@RequiredArgsConstructor
public class ProcessStepScriptCodec {

    private final ObjectMapper objectMapper;
    private final ProcessStepScriptAssembler scriptAssembler;
    private final ProcessStepEngineConfigCodec engineConfigCodec;

    public boolean isScriptJson(String stepScript) {
        if (!StringUtils.hasText(stepScript)) {
            return false;
        }
        String trimmed = stepScript.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}");
    }

    public void validateStepScript(String stepScript) {
        validateStepScript(stepScript, null);
    }

    public void validateStepScript(String stepScript, String stepId) {
        if (!isScriptJson(stepScript)) {
            return;
        }
        try {
            parseFlowScript(stepScript.trim(), stepId);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("工序执行脚本格式无效: " + ex.getMessage());
        }
    }

    public ProcessStepScriptHotReloadVO hotReloadStepScript(String stepId, String stepScript) {
        if (!isScriptJson(stepScript)) {
            throw new BusinessException("工序脚本须为 JSON 格式（version=3 + flow）");
        }
        ProcessStepDefinition definition = scriptAssembler.assembleDefinition(stepScript.trim(), stepId);
        if (definition == null || CollectionUtils.isEmpty(definition.getCombinations())) {
            throw new BusinessException("工序脚本未包含可执行驱动段");
        }
        return toHotReloadVo(stepId, definition);
    }

    private ProcessStepScriptHotReloadVO toHotReloadVo(String stepId, ProcessStepDefinition definition) {
        ProcessStepScriptHotReloadVO vo = new ProcessStepScriptHotReloadVO();
        vo.setStepId(stepId);
        vo.setScriptChainKey(definition.getScriptChainKey());
        vo.setSegmentCount(definition.getCombinations().size());
        for (int i = 0; i < definition.getCombinations().size(); i++) {
            var combo = definition.getCombinations().get(i);
            ProcessStepScriptHotReloadSegmentVO seg = new ProcessStepScriptHotReloadSegmentVO();
            seg.setSegmentIndex(i);
            seg.setName(combo.getName());
            seg.setChainId(combo.getChainId());
            seg.setLiteflowEl(combo.getLiteflowEl());
            if (combo.getDrive() != null) {
                seg.setDriveKind(combo.getDrive().getKind());
            }
            if (combo.getComplete() != null) {
                seg.setCompleteKind(combo.getComplete().getKind());
            }
            vo.getSegments().add(seg);
        }
        return vo;
    }

    public Optional<ProcessStepDefinition> parseStepDefinition(String stepScript) {
        return parseStepDefinition(stepScript, null);
    }

    public Optional<ProcessStepDefinition> parseStepDefinition(String stepScript, String stepId) {
        if (!isScriptJson(stepScript)) {
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

    public ProcessStepScript parseScript(String scriptJson) {
        if (!StringUtils.hasText(scriptJson)) {
            throw new BusinessException("工序脚本不能为空");
        }
        try {
            ProcessStepDefinition definition = parseStepDefinition(scriptJson)
                    .orElseThrow(() -> new BusinessException("工序脚本未包含可执行流程"));
            ProcessStepScript script = new ProcessStepScript();
            script.getSteps().add(definition);
            return script;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("工序脚本解析失败: " + ex.getMessage());
        }
    }

    public ProcessStepScript buildScriptFromSteps(List<BizProcessStep> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new BusinessException("工艺未配置工序");
        }
        List<ProcessStepDefinition> definitions = new ArrayList<>();
        for (BizProcessStep step : steps) {
            engineConfigCodec.resolve(step).ifPresent(definitions::add);
        }
        if (definitions.isEmpty()) {
            throw new BusinessException("工序未配置流程引擎（stepEngineConfig / stepName 模板 / stepScript）");
        }
        ProcessStepScript script = new ProcessStepScript();
        script.setSteps(definitions);
        return script;
    }

    private List<ProcessStepCombination> parseFlowScript(String stepScript, String stepId) throws Exception {
        JsonNode root = objectMapper.readTree(stepScript);
        if (!root.has("version") || root.get("version").asInt() != 3) {
            throw new BusinessException("工序脚本 version 须为 3");
        }
        if (!root.has("flow")) {
            throw new BusinessException("工序脚本须包含 flow");
        }
        return scriptAssembler.assembleFlow(root, stepScript, stepId);
    }

    /**
     * 解析工序引擎定义（来源由 process.engine.config-source 决定）。
     */
    public Optional<ProcessStepDefinition> resolveStepDefinition(BizProcessStep step) {
        return engineConfigCodec.resolve(step);
    }

    public String compileEngineConfig(BizProcessStep step, String equipmentCodeOverride) {
        return engineConfigCodec.compileAndSerialize(step, equipmentCodeOverride);
    }
}
