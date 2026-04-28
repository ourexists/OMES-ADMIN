package com.ourexists.omes.ai.agent.chat.service;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.omes.ai.agent.agent.AgentExecutor;
import com.ourexists.omes.ai.agent.aiservice.AiAgentFactory;
import com.ourexists.omes.ai.agent.aiservice.OrchestratorAggregateAiAgent;
import com.ourexists.omes.ai.agent.aiservice.OrchestratorRouterAiAgent;
import com.ourexists.omes.ai.agent.chat.store.AgentSessionStore;
import com.ourexists.omes.ai.config.AgentProperties;
import com.ourexists.omes.ai.agent.model.AgentChatConfigResponse;
import com.ourexists.omes.ai.agent.model.AgentChatNodeReply;
import com.ourexists.omes.ai.agent.model.AgentChatRequest;
import com.ourexists.omes.ai.agent.model.AgentChatResponse;
import com.ourexists.omes.ai.agent.model.AgentMessageDto;
import com.ourexists.omes.ai.agent.memory.AgentSessionChatMemoryStore;
import com.ourexists.omes.ai.agent.model.AgentSessionDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.lang.reflect.Method;
import java.util.Map;

@Service
public class AgentOrchestrationChatService {

    private static final String DISPATCH_AGENT = "dispatch-agent";
    private static final String DEVICE_AGENT = "device-agent";
    private static final String DATA_AGENT = "data-agent";
    private static final String ALARM_AGENT = "alarm-agent";

    private final AgentProperties properties;
    private final AiAgentFactory aiAgentFactory;
    private final AgentSessionStore sessionStore;
    private final AgentSessionChatMemoryStore chatMemoryStore;
    private final Map<String, AgentExecutor> executorMap;

    public AgentOrchestrationChatService(AgentProperties properties,
                                         AiAgentFactory aiAgentFactory,
                                         AgentSessionStore sessionStore,
                                         AgentSessionChatMemoryStore chatMemoryStore,
                                         List<AgentExecutor> executors) {
        this.properties = properties;
        this.aiAgentFactory = aiAgentFactory;
        this.sessionStore = sessionStore;
        this.chatMemoryStore = chatMemoryStore;
        this.executorMap = new HashMap<>();
        if (executors != null) {
            for (AgentExecutor executor : executors) {
                if (executor == null || executor.id() == null || executor.id().isBlank()) {
                    continue;
                }
                this.executorMap.put(executor.id(), executor);
            }
        }
    }

    public AgentChatResponse chat(AgentChatRequest request) {
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }
        String message = request.getMessage() == null ? "" : request.getMessage().trim();
        if (message.isEmpty()) {
            throw new BusinessException("消息不能为空");
        }
        int maxMessageLength = Math.max(100, properties.getMultiAgent().getMaxMessageLength());
        if (message.length() > maxMessageLength) {
            throw new BusinessException("消息长度超限，最大允许 " + maxMessageLength + " 字符");
        }
        String operator = resolveCurrentOperator();
        String sessionId = ensureSession(request.getSessionId(), operator);
        autoTitleSessionIfNeeded(sessionId, message, operator);

        List<AgentChatNodeReply> replies = new ArrayList<>();
        List<String> usedAgents = new ArrayList<>();
        String historyText = normalizeHistory(sessionId, request.getHistory());
        String provider = "openai";
        String model = "gpt-4o-mini";
        String selectedAgentId = selectAgentByIntent(message, historyText, provider, model);
        AgentExecutor selectedAgent = executorMap.get(selectedAgentId);
        if (selectedAgent == null) {
            selectedAgentId = DISPATCH_AGENT;
            selectedAgent = executorMap.get(selectedAgentId);
        }
        if (selectedAgent == null) {
            throw new BusinessException("未找到可用Agent执行器");
        }
        String agentAnswer = selectedAgent.execute(sessionId, message, historyText, provider, model);
        AgentChatNodeReply routed = new AgentChatNodeReply();
        routed.setAgentName(selectedAgentId);
        routed.setRole(selectedAgent.role());
        routed.setContent(agentAnswer);
        replies.add(routed);
        usedAgents.add(selectedAgentId);

        AgentChatResponse response = new AgentChatResponse();
        response.setNodeReplies(replies);
        String finalAnswer = aggregateFinalAnswer(message, historyText, selectedAgentId, agentAnswer, provider, model);
        response.setFinalAnswer(finalAnswer);
        response.setUsedAgents(usedAgents);
        response.setSessionId(sessionId);

        sessionStore.appendMessage(sessionId, "user", message);
        for (AgentChatNodeReply reply : replies) {
            sessionStore.appendMessage(sessionId, reply.getAgentName(), reply.getContent());
        }
        sessionStore.appendMessage(sessionId, "assistant-final", finalAnswer);
        sessionStore.saveAudit(sessionId, operator, "multi-chat", true, "used agents: " + String.join(",", usedAgents));
        return response;
    }

    public AgentChatConfigResponse config() {
        AgentChatConfigResponse response = new AgentChatConfigResponse();
        response.setMaxHistory(Math.max(1, properties.getMultiAgent().getMaxHistory()));
        int window = properties.getMultiAgent().getMemoryWindowMessages();
        if (window <= 0) {
            window = Math.max(1, properties.getMultiAgent().getMaxHistory());
        }
        response.setMemoryWindowMessages(window);
        response.setShortTermTtlMinutes(Math.max(1, properties.getSession().getShortTermTtlMinutes()));
        response.setMaxMessageLength(Math.max(100, properties.getMultiAgent().getMaxMessageLength()));
        response.setEnabledAgents(List.of(DISPATCH_AGENT, DEVICE_AGENT, DATA_AGENT, ALARM_AGENT));
        return response;
    }

    public String createSession(String title) {
        String actualTitle = (title == null || title.isBlank()) ? "新对话" : title.trim();
        String actualOperator = resolveCurrentOperator();
        String sessionId = sessionStore.createSession(actualTitle, actualOperator);
        sessionStore.saveAudit(sessionId, actualOperator, "create-session", true, actualTitle);
        return sessionId;
    }

    public List<AgentSessionDto> sessions() {
        return sessionStore.listSessions();
    }

    public List<AgentMessageDto> messages(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Collections.emptyList();
        }
        return sessionStore.listMessages(sessionId);
    }

    public boolean deleteSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return false;
        }
        String operator = resolveCurrentOperator();
        boolean deleted = sessionStore.deleteSession(sessionId);
        if (deleted) {
            chatMemoryStore.deleteMessages(sessionId);
        }
        sessionStore.saveAudit(sessionId, operator, "delete-session", deleted, deleted ? "ok" : "session not found");
        return deleted;
    }

    private String normalizeHistory(String sessionId, List<String> history) {
        List<String> source = history;
        if (sessionId != null && !sessionId.isBlank()) {
            List<AgentMessageDto> messages = sessionStore.listMessages(sessionId);
            if (messages != null && !messages.isEmpty()) {
                List<String> dbHistory = new ArrayList<>();
                for (AgentMessageDto item : messages) {
                    dbHistory.add(item.getRole() + ": " + item.getContent());
                }
                source = dbHistory;
            }
        }
        if (source == null || source.isEmpty()) {
            return "";
        }
        int maxHistory = Math.max(1, properties.getMultiAgent().getMaxHistory());
        int start = Math.max(0, source.size() - maxHistory);
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < source.size(); i++) {
            String h = source.get(i);
            if (h != null && !h.isBlank()) {
                sb.append("- ").append(h.trim()).append('\n');
            }
        }
        return sb.toString();
    }

    private String ensureSession(String sessionId, String operator) {
        if (sessionId != null && !sessionId.isBlank()) {
            return sessionId;
        }
        return createSession("新对话");
    }

    private void autoTitleSessionIfNeeded(String sessionId, String userMessage, String operator) {
        if (sessionId == null || sessionId.isBlank() || userMessage == null || userMessage.isBlank()) {
            return;
        }
        AgentSessionDto session = sessionStore.getSession(sessionId);
        if (session == null) {
            return;
        }
        String title = session.getTitle() == null ? "" : session.getTitle().trim();
        if (!title.isEmpty() && !"新对话".equals(title) && !"AI Multi-Agent Session".equals(title)) {
            return;
        }
        List<AgentMessageDto> existing = sessionStore.listMessages(sessionId);
        if (existing != null && !existing.isEmpty()) {
            return;
        }
        String generatedTitle = buildChatLikeTitle(userMessage);
        if (generatedTitle.isEmpty()) {
            return;
        }
        boolean updated = sessionStore.updateSessionTitle(sessionId, generatedTitle);
        if (updated) {
            sessionStore.saveAudit(sessionId, operator, "rename-session", true, generatedTitle);
        }
    }

    private String buildChatLikeTitle(String userMessage) {
        String normalized = userMessage.replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) {
            return "";
        }
        int maxChars = 24;
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, maxChars) + "...";
    }

    private String resolveCurrentOperator() {
        try {
            Object user = UserContext.getUser();
            if (user == null) return "anonymous";
            Method getId = user.getClass().getMethod("getId");
            Object id = getId.invoke(user);
            if (id != null && !String.valueOf(id).isBlank()) {
                return String.valueOf(id);
            }
            Method getUsername = user.getClass().getMethod("getUsername");
            Object username = getUsername.invoke(user);
            if (username != null && !String.valueOf(username).isBlank()) {
                return String.valueOf(username);
            }
        } catch (Exception ignored) {
        }
        return "anonymous";
    }

    private String selectAgentByIntent(String message, String historyText, String provider, String model) {
        OrchestratorRouterAiAgent router = aiAgentFactory.create(OrchestratorRouterAiAgent.class, provider, model);
        String routeInput = """
                历史上下文：
                %s
                用户问题：
                %s
                """.formatted(
                historyText == null ? "" : historyText,
                message == null ? "" : message
        );
        String normalized = router.route(routeInput).trim().toLowerCase();
        if (normalized.contains(DEVICE_AGENT)) {
            return DEVICE_AGENT;
        }
        if (normalized.contains(DATA_AGENT)) {
            return DATA_AGENT;
        }
        if (normalized.contains(ALARM_AGENT)) {
            return ALARM_AGENT;
        }
        return DISPATCH_AGENT;
    }

    private String aggregateFinalAnswer(String message,
                                        String historyText,
                                        String selectedAgentId,
                                        String agentAnswer,
                                        String provider,
                                        String model) {
        OrchestratorAggregateAiAgent aggregator = aiAgentFactory.create(OrchestratorAggregateAiAgent.class, provider, model);
        String prompt = """
                用户问题：
                %s

                历史上下文：
                %s

                已选择Agent：
                %s

                Agent执行结果：
                %s
                """.formatted(
                message == null ? "" : message,
                historyText == null ? "" : historyText,
                selectedAgentId == null ? "" : selectedAgentId,
                agentAnswer == null ? "" : agentAnswer
        );
        return aggregator.aggregate(prompt);
    }

}
