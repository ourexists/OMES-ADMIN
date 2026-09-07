package com.ourexists.omes.process.service;

import com.ourexists.omes.process.domain.BizProcessStep;
import com.ourexists.omes.process.model.ProcessRecipeSegmentVO;
import com.ourexists.omes.process.model.ProcessRecipeVO;
import com.ourexists.omes.process.engine.model.ProcessStepDefinition;
import com.ourexists.omes.process.engine.model.ProcessStepScript;
import com.ourexists.omes.process.engine.recipe.ProcessRecipeAssembler;
import com.ourexists.omes.process.engine.recipe.ProcessRecipeCatalog;
import com.ourexists.omes.process.engine.recipe.ProcessRecipeYamlSpec;
import com.ourexists.omes.process.engine.support.DurationTextParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProcessRecipeService {

    private final ProcessRecipeCatalog recipeCatalog;
    private final ProcessRecipeAssembler recipeAssembler;

    public List<ProcessRecipeVO> listRecipes() {
        List<ProcessRecipeVO> list = new ArrayList<>();
        for (String recipeId : recipeCatalog.recipeIds().stream().sorted().toList()) {
            list.add(toVo(recipeId, recipeCatalog.require(recipeId)));
        }
        return list;
    }

    public ProcessRecipeVO getRecipe(String recipeId) {
        return toVo(recipeId, recipeCatalog.require(recipeId));
    }

    public ProcessStepScript buildScript(String recipeId, String equipmentCode) {
        ProcessStepDefinition definition = recipeAssembler.assemble(recipeId, equipmentCode);
        ProcessStepScript script = new ProcessStepScript();
        script.getSteps().add(definition);
        return script;
    }

    public Optional<ProcessStepDefinition> resolveStepDefinition(BizProcessStep step) {
        return resolveStepDefinition(step, null);
    }

    public Optional<ProcessStepDefinition> resolveStepDefinition(BizProcessStep step, String equipmentCodeOverride) {
        if (step == null || !StringUtils.hasText(step.getStepName())) {
            return Optional.empty();
        }
        return recipeAssembler.assembleByStepName(
                step.getStepName(), equipmentCodeOverride, step.getId(), step.getStepScript(), step.getParams());
    }

    public ProcessRecipeVO getRecipeByStepName(String stepName) {
        String recipeId = recipeCatalog.findRecipeIdByStepName(stepName)
                .orElseThrow(() -> new com.ourexists.era.framework.core.exceptions.BusinessException(
                        404, "未找到与工序名称匹配的引擎模板: " + stepName));
        return toVo(recipeId, recipeCatalog.require(recipeId));
    }

    private ProcessRecipeVO toVo(String recipeId, ProcessRecipeYamlSpec spec) {
        List<ProcessRecipeSegmentVO> segments = new ArrayList<>();
        String equipmentCode = spec.getEquipmentCode();
        String variable = spec.getVariable();
        boolean shutdown = spec.getShutdown() != null && Boolean.TRUE.equals(spec.getShutdown().getEnabled());

        if (spec.getEngine() != null && !CollectionUtils.isEmpty(spec.getEngine().getCombinations())) {
            ProcessStepDefinition action = spec.getEngine().getCombinations().get(0).getAction();
            if (action != null) {
                if (StringUtils.hasText(action.getEquipmentCode())) {
                    equipmentCode = action.getEquipmentCode();
                }
                if (StringUtils.hasText(action.getVariable())) {
                    variable = action.getVariable();
                }
                if (!CollectionUtils.isEmpty(action.getSegments())) {
                    for (var raw : action.getSegments()) {
                        segments.add(ProcessRecipeSegmentVO.builder()
                                .to(raw.getTo())
                                .duration(raw.getDuration())
                                .holdDuration(raw.getHoldDuration())
                                .build());
                    }
                }
            }
            shutdown = false;
        } else if (spec.getRamp() != null && !CollectionUtils.isEmpty(spec.getRamp().getSegments())) {
            for (ProcessRecipeYamlSpec.ProcessRecipeYamlSegment raw : spec.getRamp().getSegments()) {
                Integer duration = raw.getDuration() != null
                        ? DurationTextParser.parseToSeconds(raw.getDuration()) : null;
                Integer hold = raw.getHoldDuration() != null
                        ? DurationTextParser.parseToSeconds(raw.getHoldDuration()) : null;
                segments.add(ProcessRecipeSegmentVO.builder()
                        .to(raw.getTo())
                        .duration(duration)
                        .holdDuration(hold)
                        .build());
            }
        }

        return ProcessRecipeVO.builder()
                .recipeId(recipeId)
                .name(spec.getName())
                .description(spec.getDescription())
                .equipmentCode(equipmentCode)
                .variable(variable)
                .segmentCount(segments.size())
                .shutdownEnabled(shutdown)
                .segments(segments)
                .build();
    }
}
