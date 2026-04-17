package com.ourexists.omes.ai.agent.service;

import com.ourexists.omes.ai.agent.tool.AiAgentTool;
import com.ourexists.omes.ai.agent.tool.ToolRegistry;
import com.ourexists.omes.ai.model.agent.AiAgentRunRequest;
import com.ourexists.omes.ai.model.agent.AiAgentRunResponse;
import com.ourexists.omes.ai.model.agent.AiAgentToolCall;
import com.ourexists.omes.ai.model.agent.AiAgentToolResult;
import com.ourexists.omes.ai.shared.service.AiLlmService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiAgentService {

    private final ToolRegistry toolRegistry;
    private final AiLlmService llmService;

    public AiAgentService(ToolRegistry toolRegistry, AiLlmService llmService) {
        this.toolRegistry = toolRegistry;
        this.llmService = llmService;
    }

    public AiAgentRunResponse run(AiAgentRunRequest request) {
        List<AiAgentToolCall> toolCalls = request.getToolCalls();
        if (toolCalls == null || toolCalls.isEmpty()) {
            toolCalls = inferToolChain(request.getUserPrompt());
        }

        List<AiAgentToolResult> results = new ArrayList<>();
        for (AiAgentToolCall call : toolCalls) {
            AiAgentTool tool = toolRegistry.get(call.getToolName());
            if (tool == null) {
                AiAgentToolResult missing = new AiAgentToolResult();
                missing.setToolName(call.getToolName());
                missing.setSuccess(false);
                missing.setMessage("未找到工具: " + call.getToolName() + "，可用工具: " + String.join(", ", toolRegistry.names()));
                missing.setData(Map.of());
                results.add(missing);
                continue;
            }
            Map<String, Object> args = call.getArguments() == null ? Map.of() : call.getArguments();
            results.add(tool.execute(args));
        }

        String answer = buildFinalAnswer(request.getUserPrompt(), results);
        AiAgentRunResponse response = new AiAgentRunResponse();
        response.setToolResults(results);
        response.setFinalAnswer(answer);
        return response;
    }

    private List<AiAgentToolCall> inferToolChain(String prompt) {
        String text = prompt == null ? "" : prompt;
        List<AiAgentToolCall> chain = new ArrayList<>();
        AiAgentToolCall call = new AiAgentToolCall();
        Map<String, Object> args = new HashMap<>();

        if (text.contains("入库") || text.contains("上传")) {
            call.setToolName("knowledge.ingest");
            args.put("knowledgeType", "unstructured");
            args.put("sourceName", "agent-input");
            args.put("textContent", text);
        } else if (text.contains("手册") || text.contains("知识") || text.contains("案例")) {
            call.setToolName("knowledge.search");
            args.put("question", text);
            args.put("topK", 5);
        } else {
            call.setToolName("inspection.query");
            args.put("days", 7);
            args.put("limit", 50);
            args.put("includeOnlyAbnormal", false);
        }
        call.setArguments(args);
        chain.add(call);
        return chain;
    }

    private String buildFinalAnswer(String userPrompt, List<AiAgentToolResult> results) {
        StringBuilder context = new StringBuilder();
        context.append("用户目标: ").append(userPrompt == null ? "" : userPrompt).append("\n");
        context.append("工具执行结果:\n");
        for (AiAgentToolResult result : results) {
            context.append("- ").append(result.getToolName())
                    .append(" | success=").append(result.isSuccess())
                    .append(" | message=").append(result.getMessage())
                    .append(" | data=").append(result.getData())
                    .append("\n");
        }
        context.append("\n请基于工具结果给出简洁的执行结论和下一步建议。");
        return llmService.ask(context.toString());
    }
}
