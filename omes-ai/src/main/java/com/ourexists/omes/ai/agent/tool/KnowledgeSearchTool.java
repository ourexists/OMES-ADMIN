package com.ourexists.omes.ai.agent.tool;

import com.ourexists.omes.ai.knowledge.service.InspectionKnowledgeAppService;
import com.ourexists.omes.ai.agent.model.AgentToolResult;
import com.ourexists.omes.ai.shared.service.PortalKnowledgeProxyService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class KnowledgeSearchTool implements AgentTool {

    private final InspectionKnowledgeAppService knowledgeService;
    private final PortalKnowledgeProxyService portalKnowledgeProxyService;

    public KnowledgeSearchTool(InspectionKnowledgeAppService knowledgeService,
                               PortalKnowledgeProxyService portalKnowledgeProxyService) {
        this.knowledgeService = knowledgeService;
        this.portalKnowledgeProxyService = portalKnowledgeProxyService;
    }

    @Override
    public String name() {
        return "knowledge.search";
    }

    @Override
    public AgentToolResult execute(Map<String, Object> arguments) {
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
        return search(question, topK);
    }

    @Tool("检索知识库并返回最相关内容")
    public AgentToolResult search(
            @P("用户问题") String question,
            @P("返回条数，默认 5") Integer topK
    ) {
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

        AgentToolResult result = new AgentToolResult();
        result.setToolName(name());
        result.setSuccess(true);
        result.setMessage("知识库检索完成");
        result.setData(data);
        return result;
    }
}
