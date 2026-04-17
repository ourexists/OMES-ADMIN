package com.ourexists.omes.ai.agent.tool.impl;

import com.ourexists.omes.ai.agent.tool.AiAgentTool;
import com.ourexists.omes.ai.knowledge.service.AiInspectionKnowledgeService;
import com.ourexists.omes.ai.model.agent.AiAgentToolResult;
import com.ourexists.omes.ai.shared.service.PortalKnowledgeProxyService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class KnowledgeSearchTool implements AiAgentTool {

    private final AiInspectionKnowledgeService knowledgeService;
    private final PortalKnowledgeProxyService portalKnowledgeProxyService;

    public KnowledgeSearchTool(AiInspectionKnowledgeService knowledgeService,
                               PortalKnowledgeProxyService portalKnowledgeProxyService) {
        this.knowledgeService = knowledgeService;
        this.portalKnowledgeProxyService = portalKnowledgeProxyService;
    }

    @Override
    public String name() {
        return "knowledge.search";
    }

    @Override
    public AiAgentToolResult execute(Map<String, Object> arguments) {
        String question = arguments.get("question") == null ? "" : String.valueOf(arguments.get("question"));
        Integer topK = null;
        Object topKValue = arguments.get("topK");
        if (topKValue instanceof Number n) {
            topK = n.intValue();
        } else if (topKValue != null) {
            try {
                topK = Integer.parseInt(String.valueOf(topKValue));
            } catch (Exception ignored) {
            }
        }

        List<String> docs;
        String source;
        if (portalKnowledgeProxyService.enabled()) {
            String answer = portalKnowledgeProxyService.ask(question, topK);
            docs = answer == null || answer.isBlank() ? List.of() : List.of(answer);
            source = "portal-rest";
        } else {
            docs = knowledgeService.searchKnowledge(question, topK);
            source = "local";
        }
        Map<String, Object> data = new HashMap<>();
        data.put("question", question);
        data.put("topK", topK);
        data.put("matches", docs);
        data.put("source", source);

        AiAgentToolResult result = new AiAgentToolResult();
        result.setToolName(name());
        result.setSuccess(true);
        result.setMessage("知识库检索完成");
        result.setData(data);
        return result;
    }
}
