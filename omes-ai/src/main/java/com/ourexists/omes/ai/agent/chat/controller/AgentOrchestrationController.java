package com.ourexists.omes.ai.agent.controller;

import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.omes.ai.agent.chat.service.AgentOrchestrationChatService;
import com.ourexists.omes.ai.agent.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Tag(name = "AI巡检报告-Agent工具编排")
@RestController
@RequestMapping("/inspection/ai/agent")
public class AgentOrchestrationController {

    private final AgentOrchestrationChatService agentOrchestrationChatService;

    public AgentOrchestrationController(AgentOrchestrationChatService agentOrchestrationChatService) {
        this.agentOrchestrationChatService = agentOrchestrationChatService;
    }

    @Operation(summary = "多Agent对话（含自定义子Agent）")
    @PostMapping("/multi-chat")
    public JsonResponseEntity<AgentChatResponse> multiChat(@RequestBody AgentChatRequest request) {
        return JsonResponseEntity.success(agentOrchestrationChatService.chat(request));
    }

    @Operation(summary = "多Agent对话流式输出（SSE）")
    @PostMapping(value = "/multi-chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter multiChatStream(@RequestBody AgentChatRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);
        CompletableFuture.runAsync(() -> {
            try {
                AgentChatResponse response = agentOrchestrationChatService.chat(request);
                emitter.send(SseEmitter.event().name("session").data(Map.of("sessionId", response.getSessionId())));
                if (response.getNodeReplies() != null) {
                    for (int i = 0; i < response.getNodeReplies().size(); i++) {
                        emitter.send(SseEmitter.event().name("node_reply").data(response.getNodeReplies().get(i)));
                    }
                }
                emitter.send(SseEmitter.event().name("final").data(Map.of("finalAnswer", response.getFinalAnswer())));
                emitter.send(SseEmitter.event().name("done").data(Map.of("ok", true)));
                emitter.complete();
            } catch (Exception ex) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(Map.of("message", ex.getMessage())));
                } catch (Exception ignored) {
                }
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    @Operation(summary = "获取多Agent产品化配置")
    @GetMapping("/multi-chat/config")
    public JsonResponseEntity<AgentChatConfigResponse> multiChatConfig() {
        return JsonResponseEntity.success(agentOrchestrationChatService.config());
    }

    @Operation(summary = "创建多Agent会话")
    @PostMapping("/multi-chat/session/create")
    public JsonResponseEntity<Map<String, String>> createSession(@RequestBody(required = false) AgentSessionCreateRequest request) {
        String title = request == null ? null : request.getTitle();
        String sessionId = agentOrchestrationChatService.createSession(title);
        return JsonResponseEntity.success(Map.of("sessionId", sessionId));
    }

    @Operation(summary = "查询多Agent会话列表")
    @GetMapping("/multi-chat/session/list")
    public JsonResponseEntity<List<AgentSessionDto>> sessions() {
        return JsonResponseEntity.success(agentOrchestrationChatService.sessions());
    }

    @Operation(summary = "查询会话消息列表")
    @GetMapping("/multi-chat/session/messages")
    public JsonResponseEntity<List<AgentMessageDto>> messages(@RequestParam String sessionId) {
        return JsonResponseEntity.success(agentOrchestrationChatService.messages(sessionId));
    }

    @Operation(summary = "删除会话")
    @PostMapping("/multi-chat/session/delete")
    public JsonResponseEntity<Map<String, Object>> deleteSession(@RequestParam String sessionId) {
        boolean deleted = agentOrchestrationChatService.deleteSession(sessionId);
        return JsonResponseEntity.success(Map.of("deleted", deleted, "sessionId", sessionId));
    }
}
