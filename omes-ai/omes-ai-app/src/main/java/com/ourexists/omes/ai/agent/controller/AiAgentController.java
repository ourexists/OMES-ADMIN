package com.ourexists.omes.ai.agent.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.ai.agent.service.AiAgentService;
import com.ourexists.omes.ai.agent.service.MultiAgentChatService;
import com.ourexists.omes.ai.model.agent.AiAgentRunRequest;
import com.ourexists.omes.ai.model.agent.AiAgentRunResponse;
import com.ourexists.omes.ai.model.agent.AiAgentMessageDto;
import com.ourexists.omes.ai.model.agent.AiAgentSessionCreateRequest;
import com.ourexists.omes.ai.model.agent.AiAgentSessionDto;
import com.ourexists.omes.ai.model.agent.MultiAgentConfigResponse;
import com.ourexists.omes.ai.model.agent.MultiAgentChatRequest;
import com.ourexists.omes.ai.model.agent.MultiAgentChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "AI巡检报告-Agent工具编排")
@RestController
@RequestMapping("/inspection/ai/agent")
public class AiAgentController {

    private final AiAgentService agentService;
    private final MultiAgentChatService multiAgentChatService;

    public AiAgentController(AiAgentService agentService,
                             MultiAgentChatService multiAgentChatService) {
        this.agentService = agentService;
        this.multiAgentChatService = multiAgentChatService;
    }

    @Operation(summary = "执行Agent Tool链路")
    @PostMapping("/run")
    public JsonResponseEntity<AiAgentRunResponse> run(@RequestBody AiAgentRunRequest request) {
        return JsonResponseEntity.success(agentService.run(request));
    }

    @Operation(summary = "多Agent对话（含自定义子Agent）")
    @PostMapping("/multi-chat")
    public JsonResponseEntity<MultiAgentChatResponse> multiChat(@RequestBody MultiAgentChatRequest request) {
        return JsonResponseEntity.success(multiAgentChatService.chat(request));
    }

    @Operation(summary = "获取多Agent产品化配置")
    @GetMapping("/multi-chat/config")
    public JsonResponseEntity<MultiAgentConfigResponse> multiChatConfig() {
        return JsonResponseEntity.success(multiAgentChatService.config());
    }

    @Operation(summary = "创建多Agent会话")
    @PostMapping("/multi-chat/session/create")
    public JsonResponseEntity<Map<String, String>> createSession(@RequestBody(required = false) AiAgentSessionCreateRequest request) {
        String title = request == null ? null : request.getTitle();
        String sessionId = multiAgentChatService.createSession(title);
        return JsonResponseEntity.success(Map.of("sessionId", sessionId));
    }

    @Operation(summary = "查询多Agent会话列表")
    @GetMapping("/multi-chat/session/list")
    public JsonResponseEntity<List<AiAgentSessionDto>> sessions() {
        return JsonResponseEntity.success(multiAgentChatService.sessions());
    }

    @Operation(summary = "查询会话消息列表")
    @GetMapping("/multi-chat/session/messages")
    public JsonResponseEntity<List<AiAgentMessageDto>> messages(@RequestParam String sessionId) {
        return JsonResponseEntity.success(multiAgentChatService.messages(sessionId));
    }
}
