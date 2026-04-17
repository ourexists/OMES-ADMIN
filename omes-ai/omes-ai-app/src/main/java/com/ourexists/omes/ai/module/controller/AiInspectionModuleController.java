package com.ourexists.omes.ai.module.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.ai.config.AiInspectionProperties;
import com.ourexists.omes.ai.knowledge.service.AiInspectionKnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "AI巡检报告-模块状态")
@RestController
@RequestMapping("/inspection/ai")
public class AiInspectionModuleController {

    private final AiInspectionProperties properties;
    private final AiInspectionKnowledgeService knowledgeService;

    public AiInspectionModuleController(AiInspectionProperties properties,
                                        AiInspectionKnowledgeService knowledgeService) {
        this.properties = properties;
        this.knowledgeService = knowledgeService;
    }

    @Operation(summary = "AI巡检模块状态")
    @GetMapping("/status")
    public JsonResponseEntity<Map<String, Object>> status() {
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", properties.isEnabled());
        result.put("defaultDays", properties.getDefaultDays());
        result.put("defaultLimit", properties.getDefaultLimit());
        result.put("vectorProvider", knowledgeService.providerSummary());
        return JsonResponseEntity.success(result);
    }
}
