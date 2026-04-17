package com.ourexists.omes.ai.knowledge.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.ai.knowledge.service.AiInspectionKnowledgeService;
import com.ourexists.omes.ai.model.AiKnowledgeAskRequest;
import com.ourexists.omes.ai.model.AiKnowledgeIngestRequest;
import com.ourexists.omes.ai.model.AiKnowledgeReindexRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "AI巡检报告-知识库子模块")
@RestController
@RequestMapping("/inspection/ai/knowledge")
public class AiInspectionKnowledgeController {

    private final AiInspectionKnowledgeService knowledgeService;

    public AiInspectionKnowledgeController(AiInspectionKnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @Operation(summary = "重建巡检知识库")
    @PostMapping("/reindex")
    public JsonResponseEntity<Map<String, Object>> reindex(@RequestBody(required = false) AiKnowledgeReindexRequest request) {
        Integer limit = request == null ? null : request.getLimit();
        int indexed = knowledgeService.rebuildKnowledgeBase(limit);
        Map<String, Object> result = new HashMap<>();
        result.put("indexed", indexed);
        result.put("provider", knowledgeService.providerSummary());
        return JsonResponseEntity.success(result);
    }

    @Operation(summary = "巡检知识问答")
    @PostMapping("/ask")
    public JsonResponseEntity<Map<String, String>> ask(@RequestBody AiKnowledgeAskRequest request) {
        String answer = knowledgeService.askKnowledge(request.getQuestion(), request.getTopK());
        return JsonResponseEntity.success(Map.of("answer", answer));
    }

    @Operation(summary = "知识库上传入库（结构化/非结构化）")
    @PostMapping("/ingest")
    public JsonResponseEntity<Map<String, Object>> ingest(@RequestBody AiKnowledgeIngestRequest request) {
        int docs = knowledgeService.ingestKnowledge(request);
        Map<String, Object> result = new HashMap<>();
        result.put("ingested", docs);
        result.put("provider", knowledgeService.providerSummary());
        return JsonResponseEntity.success(result);
    }
}
