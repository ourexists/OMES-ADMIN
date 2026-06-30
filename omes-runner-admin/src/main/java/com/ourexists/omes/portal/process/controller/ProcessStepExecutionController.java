/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.portal.process.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.process.engine.ProcessStepScriptCodec;
import com.ourexists.omes.process.model.*;
import com.ourexists.omes.process.service.ProcessRecipeService;
import com.ourexists.omes.process.service.ProcessStepSimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "工艺工序执行", description = "工序脚本仿真与配方")
@RestController
@RequestMapping("/processes/execution")
@RequiredArgsConstructor
public class ProcessStepExecutionController {

    private final ProcessStepSimulationService simulationService;
    private final ProcessStepScriptCodec stepScriptCodec;
    private final ProcessRecipeService recipeService;

    @PostMapping("/simulate/start")
    @Operation(summary = "启动仿真")
    public JsonResponseEntity<ProcessStepSimulationStartVO> start(
            @Valid @RequestBody ProcessStepSimulationStartRequest request) {
        return JsonResponseEntity.success(simulationService.start(request));
    }

    @PostMapping("/simulate/tick")
    @Operation(summary = "仿真推进一步")
    public JsonResponseEntity<ProcessStepSimulationTickVO> tick(
            @Valid @RequestBody ProcessStepSimulationTickRequest request) {
        return JsonResponseEntity.success(simulationService.tick(request));
    }

    @PostMapping("/simulate/signals")
    @Operation(summary = "更新仿真过程量")
    public JsonResponseEntity<Boolean> signals(@Valid @RequestBody ProcessStepSimulationSignalsRequest request) {
        simulationService.updateSignals(request);
        return JsonResponseEntity.success(true);
    }

    @PostMapping("/simulate/status")
    @Operation(summary = "查询仿真状态")
    public JsonResponseEntity<ProcessStepSimulationStatusVO> status(
            @Valid @RequestBody ProcessStepSimulationSessionRequest request) {
        return JsonResponseEntity.success(simulationService.status(request.getSessionId()));
    }

    @PostMapping("/simulate/stop")
    @Operation(summary = "停止仿真")
    public JsonResponseEntity<Boolean> stop(@Valid @RequestBody ProcessStepSimulationSessionRequest request) {
        simulationService.stop(request.getSessionId());
        return JsonResponseEntity.success(true);
    }

    @PostMapping("/script/hot-reload")
    @Operation(summary = "热刷新工序脚本")
    public JsonResponseEntity<ProcessStepScriptHotReloadVO> hotReloadScript(
            @Valid @RequestBody ProcessStepScriptHotReloadRequest request) {
        return JsonResponseEntity.success(
                stepScriptCodec.hotReloadStepScript(request.getStepId(), request.getStepScript()));
    }

    @GetMapping("/recipes")
    @Operation(summary = "列出配置文件工艺配方")
    public JsonResponseEntity<List<ProcessRecipeVO>> listRecipes() {
        return JsonResponseEntity.success(recipeService.listRecipes());
    }

    @GetMapping("/recipes/by-name/{stepName}")
    @Operation(summary = "按工序名称查询引擎模板")
    public JsonResponseEntity<ProcessRecipeVO> getRecipeByStepName(@PathVariable String stepName) {
        return JsonResponseEntity.success(recipeService.getRecipeByStepName(stepName));
    }

    @GetMapping("/recipes/{recipeId}")
    @Operation(summary = "查询工艺配方详情")
    public JsonResponseEntity<ProcessRecipeVO> getRecipe(@PathVariable String recipeId) {
        return JsonResponseEntity.success(recipeService.getRecipe(recipeId));
    }
}
