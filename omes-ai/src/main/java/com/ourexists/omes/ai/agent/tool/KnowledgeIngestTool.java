package com.ourexists.omes.ai.agent.tool;

import com.ourexists.omes.ai.agent.model.AgentToolResult;
import com.ourexists.omes.ai.knowledge.model.KnowledgeIngestRequest;
import com.ourexists.omes.ai.knowledge.service.InspectionKnowledgeAppService;
import com.ourexists.omes.ai.shared.service.PortalKnowledgeProxyService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KnowledgeIngestTool implements AgentTool {

    private final InspectionKnowledgeAppService knowledgeService;
    private final PortalKnowledgeProxyService portalKnowledgeProxyService;

    public KnowledgeIngestTool(InspectionKnowledgeAppService knowledgeService,
                               PortalKnowledgeProxyService portalKnowledgeProxyService) {
        this.knowledgeService = knowledgeService;
        this.portalKnowledgeProxyService = portalKnowledgeProxyService;
    }

    @Override
    public String name() {
        return "knowledge.ingest";
    }

    @Override
    public AgentToolResult execute(Map<String, Object> arguments) {
        return ingest(
                asString(arguments.get("knowledgeType")),
                asString(arguments.get("sourceName")),
                asString(arguments.get("textContent")),
                asString(arguments.get("structuredContent"))
        );
    }

    @Tool("向知识库写入结构化或非结构化内容")
    public AgentToolResult ingest(
            @P("知识类型，structured 或 unstructured") String knowledgeType,
            @P("知识来源名称，可选") String sourceName,
            @P("非结构化文本内容") String textContent,
            @P("结构化 JSON 内容") String structuredContent
    ) {
        KnowledgeIngestRequest request = new KnowledgeIngestRequest();
        request.setKnowledgeType(knowledgeType);
        request.setSourceName(sourceName);
        request.setTextContent(textContent);
        request.setStructuredContent(structuredContent);

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

        AgentToolResult result = new AgentToolResult();
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
