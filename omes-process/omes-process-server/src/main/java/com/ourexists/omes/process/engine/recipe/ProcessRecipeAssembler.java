package com.ourexists.omes.process.engine.recipe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.omes.process.engine.liteflow.ProcessLiteFlowChainRegistry;
import com.ourexists.omes.process.engine.model.ProcessConditionKind;
import com.ourexists.omes.process.engine.model.ProcessConditionSpec;
import com.ourexists.omes.process.engine.model.ProcessStepCombination;
import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import com.ourexists.omes.process.engine.model.ProcessStepTimeSegment;
import com.ourexists.omes.process.engine.support.DurationTextParser;
import com.ourexists.omes.process.engine.support.ProcessAviatorExpressionCompiler;
import com.ourexists.omes.process.engine.support.ProcessStepFlowCompiler;
import com.ourexists.omes.process.engine.support.ProcessStepParamsParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 将 classpath 工艺配方编译为工序引擎 {@link ProcessStepDefinition}（COMBINATION + LiteFlow 链）。
 * <p>
 * 支持 {@code engine} 模板：固定驱动/完成/斜坡后控制等，{@code segments} 优先由工序 {@code params} 注入，其次 {@code stepScript}。
 */
@Component
@RequiredArgsConstructor
public class ProcessRecipeAssembler {

    private final ProcessRecipeCatalog recipeCatalog;
    private final ProcessAviatorExpressionCompiler expressionCompiler;
    private final ProcessLiteFlowChainRegistry chainRegistry;
    private final ObjectMapper objectMapper;
    private final ProcessStepParamsParser paramsParser;

    public ProcessStepDefinition assemble(String recipeId) {
        return assemble(recipeId, null, null, null, null);
    }

    public ProcessStepDefinition assemble(String recipeId, String equipmentCodeOverride) {
        return assemble(recipeId, equipmentCodeOverride, null, null, null);
    }

    public ProcessStepDefinition assemble(String recipeId, String equipmentCodeOverride, String stepId) {
        return assemble(recipeId, equipmentCodeOverride, stepId, null, null);
    }

    public ProcessStepDefinition assemble(String recipeId,
                                          String equipmentCodeOverride,
                                          String stepId,
                                          String stepScript) {
        return assemble(recipeId, equipmentCodeOverride, stepId, stepScript, null);
    }

    public ProcessStepDefinition assemble(String recipeId,
                                          String equipmentCodeOverride,
                                          String stepId,
                                          String stepScript,
                                          String params) {
        ProcessRecipeYamlSpec spec = recipeCatalog.require(recipeId);
        if (spec.getEngine() != null) {
            return assembleFromEngineTemplate(spec, equipmentCodeOverride, stepId, stepScript, params);
        }
        return assembleLegacyRamp(spec, recipeId, equipmentCodeOverride, stepId);
    }

    /**
     * 按工序名称匹配配方并编译；未匹配返回 empty。
     */
    public java.util.Optional<ProcessStepDefinition> assembleByStepName(String stepName,
                                                                        String equipmentCodeOverride,
                                                                        String stepId) {
        return assembleByStepName(stepName, equipmentCodeOverride, stepId, null, null);
    }

    public java.util.Optional<ProcessStepDefinition> assembleByStepName(String stepName,
                                                                        String equipmentCodeOverride,
                                                                        String stepId,
                                                                        String stepScript) {
        return assembleByStepName(stepName, equipmentCodeOverride, stepId, stepScript, null);
    }

    public java.util.Optional<ProcessStepDefinition> assembleByStepName(String stepName,
                                                                        String equipmentCodeOverride,
                                                                        String stepId,
                                                                        String stepScript,
                                                                        String params) {
        return recipeCatalog.findRecipeIdByStepName(stepName)
                .map(recipeId -> assemble(recipeId, equipmentCodeOverride, stepId, stepScript, params));
    }

    private ProcessStepDefinition assembleFromEngineTemplate(ProcessRecipeYamlSpec spec,
                                                           String equipmentCodeOverride,
                                                           String stepId,
                                                           String stepScript,
                                                           String params) {
        ProcessStepDefinition definition = objectMapper.convertValue(
                spec.getEngine(), ProcessStepDefinition.class);
        if (definition == null || CollectionUtils.isEmpty(definition.getCombinations())) {
            throw new BusinessException("工艺配方 engine.combinations 不能为空");
        }
        mergeRampSegments(definition, params, stepScript);
        validateRampSegmentsPresent(definition);
        if (StringUtils.hasText(equipmentCodeOverride)) {
            applyEquipmentOverride(definition, equipmentCodeOverride.trim());
        }
        compileCombinations(definition.getCombinations());
        String scriptKey = StringUtils.hasText(stepId)
                ? chainRegistry.resolveScriptKey(stepId, "recipe-engine")
                : "recipe_engine";
        definition.setScriptVersion(3);
        definition.setScriptChainKey(scriptKey);
        chainRegistry.registerScriptChains(scriptKey, definition.getCombinations());
        return definition;
    }

    private void mergeRampSegments(ProcessStepDefinition definition, String params, String stepScript) {
        List<ProcessStepTimeSegment> segments = paramsParser.parseRampSegments(params);
        if (CollectionUtils.isEmpty(segments)) {
            segments = extractRampSegmentsFromStepScript(stepScript);
        }
        if (CollectionUtils.isEmpty(segments)) {
            return;
        }
        applyRampSegments(definition, segments);
    }

    private List<ProcessStepTimeSegment> extractRampSegmentsFromStepScript(String stepScript) {
        if (!StringUtils.hasText(stepScript) || !stepScript.trim().startsWith("{")) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(stepScript.trim());
            return ProcessStepFlowCompiler.extractFirstRampSegments(root, objectMapper);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("从工序脚本合并斜坡 segments 失败: " + ex.getMessage());
        }
    }

    private void applyRampSegments(ProcessStepDefinition definition, List<ProcessStepTimeSegment> segments) {
        for (ProcessStepCombination combo : definition.getCombinations()) {
            ProcessStepDefinition action = combo.getAction();
            if (action == null || !"RAMP".equalsIgnoreCase(action.getType())) {
                continue;
            }
            action.setSegments(new ArrayList<>(segments));
        }
    }

    private void validateRampSegmentsPresent(ProcessStepDefinition definition) {
        for (int i = 0; i < definition.getCombinations().size(); i++) {
            ProcessStepDefinition action = definition.getCombinations().get(i).getAction();
            if (action == null || !"RAMP".equalsIgnoreCase(action.getType())) {
                continue;
            }
            if (CollectionUtils.isEmpty(action.getSegments())) {
                throw new BusinessException("工艺配方斜坡段须由工序 params 或 stepScript 配置 segments，或于模板 engine 中预置");
            }
            action.resolvedTimeSegments();
        }
    }

    private void applyEquipmentOverride(ProcessStepDefinition definition, String equipmentCode) {
        definition.setEquipmentCode(equipmentCode);
        if (CollectionUtils.isEmpty(definition.getCombinations())) {
            return;
        }
        for (ProcessStepCombination combo : definition.getCombinations()) {
            ProcessStepDefinition action = combo.getAction();
            if (action != null && StringUtils.hasText(action.getType())) {
                action.setEquipmentCode(equipmentCode);
            }
        }
    }

    private ProcessStepDefinition assembleLegacyRamp(ProcessRecipeYamlSpec spec,
                                                     String recipeId,
                                                     String equipmentCodeOverride,
                                                     String stepId) {
        String equipmentCode = StringUtils.hasText(equipmentCodeOverride)
                ? equipmentCodeOverride.trim()
                : spec.getEquipmentCode();
        if (!StringUtils.hasText(equipmentCode)) {
            throw new BusinessException("工艺配方须配置 equipment-code 或传入 equipmentCode");
        }
        String variable = StringUtils.hasText(spec.getVariable()) ? spec.getVariable().trim() : "temp";

        List<ProcessStepCombination> combinations = new ArrayList<>();
        combinations.add(buildRampCombo(spec, equipmentCode, variable));

        ProcessRecipeYamlSpec.ProcessRecipeYamlShutdown shutdown = spec.getShutdown();
        if (shutdown != null && Boolean.TRUE.equals(shutdown.getEnabled())) {
            combinations.add(buildShutdownCombo(shutdown, equipmentCode));
        }

        compileCombinations(combinations);

        String scriptKey = StringUtils.hasText(stepId)
                ? chainRegistry.resolveScriptKey(stepId, "recipe:" + recipeId.trim())
                : "recipe_" + recipeId.trim();
        chainRegistry.registerScriptChains(scriptKey, combinations);

        ProcessStepDefinition definition = new ProcessStepDefinition();
        definition.setType("COMBINATION");
        definition.setMode("UNIT");
        definition.setScriptVersion(3);
        definition.setScriptChainKey(scriptKey);
        definition.setCombinations(combinations);
        definition.setEquipmentCode(equipmentCode);
        definition.setVariable(variable);
        return definition;
    }

    private ProcessStepCombination buildRampCombo(ProcessRecipeYamlSpec spec,
                                                  String equipmentCode,
                                                  String variable) {
        List<ProcessStepTimeSegment> segments = toTimeSegments(spec);
        ProcessStepDefinition action = new ProcessStepDefinition();
        action.setType("RAMP");
        action.setMode("TIME");
        action.setEquipmentCode(equipmentCode);
        action.setVariable(variable);
        action.setSegments(segments);

        ProcessStepCombination combo = new ProcessStepCombination();
        combo.setName(StringUtils.hasText(spec.getName()) ? spec.getName() : "升温恒温");
        combo.setDrive(noneCondition());
        combo.setAction(action);
        combo.setComplete(autoNextCondition());
        combo.setException(noneCondition());
        return combo;
    }

    private ProcessStepCombination buildShutdownCombo(ProcessRecipeYamlSpec.ProcessRecipeYamlShutdown shutdown,
                                                      String equipmentCode) {
        String shutdownVar = StringUtils.hasText(shutdown.getVariable())
                ? shutdown.getVariable().trim() : "setpoint";
        Double target = shutdown.getTarget() != null ? shutdown.getTarget() : 0D;

        ProcessStepDefinition action = new ProcessStepDefinition();
        action.setType("PID_CONTROL");
        action.setMode("CONTROL");
        action.setEquipmentCode(equipmentCode);
        action.setVariable(shutdownVar);
        action.setTarget(target);

        ProcessStepCombination combo = new ProcessStepCombination();
        combo.setName("关闭加热");
        combo.setDrive(noneCondition());
        combo.setAction(action);
        combo.setComplete(autoNextCondition());
        combo.setException(noneCondition());
        return combo;
    }

    private List<ProcessStepTimeSegment> toTimeSegments(ProcessRecipeYamlSpec spec) {
        if (spec.getRamp() == null || CollectionUtils.isEmpty(spec.getRamp().getSegments())) {
            throw new BusinessException("工艺配方 ramp.segments 不能为空");
        }
        List<ProcessStepTimeSegment> segments = new ArrayList<>();
        int index = 0;
        for (ProcessRecipeYamlSpec.ProcessRecipeYamlSegment raw : spec.getRamp().getSegments()) {
            index++;
            if (raw.getTo() == null) {
                throw new BusinessException("配方第 " + index + " 段缺少 to（目标温度）");
            }
            if (raw.getDuration() == null) {
                throw new BusinessException("配方第 " + index + " 段缺少 duration");
            }
            int durationSec = DurationTextParser.parseToSeconds(raw.getDuration());
            Integer holdSec = null;
            if (raw.getHoldDuration() != null) {
                holdSec = DurationTextParser.parseToSeconds(raw.getHoldDuration());
            }
            segments.add(new ProcessStepTimeSegment(raw.getTo(), durationSec, holdSec));
        }
        return segments;
    }

    private void compileCombinations(List<ProcessStepCombination> combinations) {
        for (ProcessStepCombination combo : combinations) {
            expressionCompiler.compileCondition(combo.getDrive(), null, null);
            expressionCompiler.compileCondition(combo.getException(), null, null);
            expressionCompiler.compileComplete(combo.getComplete());
        }
    }

    private static ProcessConditionSpec noneCondition() {
        ProcessConditionSpec spec = new ProcessConditionSpec();
        spec.setKind(ProcessConditionKind.NONE.name());
        return spec;
    }

    private static ProcessConditionSpec autoNextCondition() {
        ProcessConditionSpec spec = new ProcessConditionSpec();
        spec.setKind(ProcessConditionKind.AUTO_NEXT.name());
        return spec;
    }
}
