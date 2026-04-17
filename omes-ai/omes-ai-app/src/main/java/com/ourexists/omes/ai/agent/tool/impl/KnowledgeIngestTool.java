package com.ourexists.omes.ai.agent.tool.impl;

import com.ourexists.omes.ai.agent.tool.AiAgentTool;
import com.ourexists.omes.ai.knowledge.service.AiInspectionKnowledgeService;
import com.ourexists.omes.ai.model.AiKnowledgeIngestRequest;
import com.ourexists.omes.ai.model.agent.AiAgentToolResult;
import com.ourexists.omes.ai.shared.service.PortalKnowledgeProxyService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KnowledgeIngestTool implements AiAgentTool {

    private final AiInspectionKnowledgeService knowledgeService;
    private final PortalKnowledgeProxyService portalKnowledgeProxyService;

    public KnowledgeIngestTool(AiInspectionKnowledgeService knowledgeService,
                               PortalKnowledgeProxyService portalKnowledgeProxyService) {
        this.knowledgeService = knowledgeService;
        this.portalKnowledgeProxyService = portalKnowledgeProxyService;
    }

    @Override
    public String name() {
        return "knowledge.ingest";
    }

    @Override
    public AiAgentToolResult execute(Map<String, Object> arguments) {
        AiKnowledgeIngestRequest request = new AiKnowledgeIngestRequest();
        request.setKnowledgeType(asString(arguments.get("knowledgeType")));
        request.setSourceName(asString(arguments.get("sourceName")));
        request.setTextContent(asString(arguments.get("textContent")));
        request.setStructuredContent(asString(arguments.get("structuredContent")));

        int ingested;
        String provider;
        String source;
        if (portalKnowledgeProxyService.enabled()) {
            Map<String, Object> portalResult = portalKnowledgeProxyService.ingest(request);
            Object ingestedVal = portalResult.get("ingested");
            ingested = ingestedVal instanceof Number ? ((Number) ingestedVal).intValue() : 0;
            provider = String.valueOf(portalResult.getOrDefault("provider", "portal"));
            source = "portal-rest";
        } else {
            ingested = knowledgeService.ingestKnowledge(request);
            provider = knowledgeService.providerSummary();
            source = "local";
        }
        Map<String, Object> data = new HashMap<>();
        data.put("ingested", ingested);
        data.put("provider", provider);
        data.put("source", source);

        AiAgentToolResult result = new AiAgentToolResult();
        result.setToolName(name());
        result.setSuccess(true);
        result.setMessage("知识入库完成");
        result.setData(data);
        return result;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
