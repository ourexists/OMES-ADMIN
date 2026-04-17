package com.ourexists.omes.ai.agent.service;

import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.omes.ai.agent.store.AiAgentSessionStore;
import com.ourexists.omes.ai.config.AiInspectionProperties;
import com.ourexists.omes.ai.knowledge.service.AiInspectionKnowledgeService;
import com.ourexists.omes.ai.model.agent.AiAgentMessageDto;
import com.ourexists.omes.ai.model.agent.AiAgentSessionDto;
import com.ourexists.omes.ai.model.agent.MultiAgentConfigResponse;
import com.ourexists.omes.ai.model.agent.MultiAgentChatRequest;
import com.ourexists.omes.ai.model.agent.MultiAgentChatResponse;
import com.ourexists.omes.ai.model.agent.MultiAgentNodeReply;
import com.ourexists.omes.ai.shared.service.AiLlmService;
import com.ourexists.omes.ai.shared.service.InspectionRecordQueryService;
import com.ourexists.omes.ai.shared.service.PortalKnowledgeProxyService;
import com.ourexists.omes.ai.model.AiInspectionReportRequest;
import com.ourexists.omes.ai.model.inspection.InspectRecordDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.lang.reflect.Method;

@Service
public class MultiAgentChatService {

    private static final String PLANNER_AGENT = "planner-agent";
    private static final String INSPECTION_AGENT = "inspection-agent";
    private static final String KNOWLEDGE_AGENT = "knowledge-agent";

    private final AiInspectionProperties properties;
    private final AiInspectionKnowledgeService knowledgeService;
    private final AiLlmService llmService;
    private final AiAgentSessionStore sessionStore;
    private final InspectionRecordQueryService inspectionRecordQueryService;
    private final PortalKnowledgeProxyService portalKnowledgeProxyService;

    public MultiAgentChatService(AiInspectionProperties properties,
                                 AiInspectionKnowledgeService knowledgeService,
                                 AiLlmService llmService,
                                 AiAgentSessionStore sessionStore,
                                 InspectionRecordQueryService inspectionRecordQueryService,
                                 PortalKnowledgeProxyService portalKnowledgeProxyService) {
        this.properties = properties;
        this.knowledgeService = knowledgeService;
        this.llmService = llmService;
        this.sessionStore = sessionStore;
        this.inspectionRecordQueryService = inspectionRecordQueryService;
        this.portalKnowledgeProxyService = portalKnowledgeProxyService;
    }

    public MultiAgentChatResponse chat(MultiAgentChatRequest request) {
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

        String customName = request.getCustomAgentName() == null || request.getCustomAgentName().isBlank()
                ? "custom-sub-agent" : request.getCustomAgentName().trim();
        String customPrompt = request.getCustomAgentPrompt() == null || request.getCustomAgentPrompt().isBlank()
                ? "你是一个制造业巡检协作子agent，请给出可执行建议。"
                : request.getCustomAgentPrompt().trim();
        String operator = resolveCurrentOperator();
        String operatorRole = resolveCurrentRole();
        boolean allowCustom = properties.getMultiAgent().isAllowCustomAgent();
        String sessionId = ensureSession(request.getSessionId(), operator);

        List<MultiAgentNodeReply> replies = new ArrayList<>();
        List<String> usedAgents = new ArrayList<>();
        Set<String> selected = resolveSelectedAgents(request.getSelectedAgents());
        String historyText = normalizeHistory(sessionId, request.getHistory());

        if (selected.contains("custom-sub-agent") && !hasCustomAgentPermission(operatorRole)) {
            sessionStore.saveAudit(sessionId, operator, "multi-chat", false, "custom sub agent permission denied");
            throw new BusinessException("当前角色无权使用自定义子agent");
        }

        if (selected.contains(PLANNER_AGENT)) {
            MultiAgentNodeReply planner = new MultiAgentNodeReply();
            planner.setAgentName(PLANNER_AGENT);
            planner.setRole("planner");
            planner.setContent(llmService.ask("你是任务分解agent，请将用户问题拆成3条子任务：\n历史上下文:\n" + historyText + "\n当前问题:\n" + message));
            replies.add(planner);
            usedAgents.add(PLANNER_AGENT);
        }

        if (selected.contains(INSPECTION_AGENT)) {
            MultiAgentNodeReply inspection = new MultiAgentNodeReply();
            inspection.setAgentName(INSPECTION_AGENT);
            inspection.setRole("domain");
            inspection.setContent(buildInspectionAgentContent(message));
            replies.add(inspection);
            usedAgents.add(INSPECTION_AGENT);
        }

        if (selected.contains(KNOWLEDGE_AGENT)) {
            MultiAgentNodeReply knowledge = new MultiAgentNodeReply();
            knowledge.setAgentName(KNOWLEDGE_AGENT);
            knowledge.setRole("retrieval");
            knowledge.setContent(buildKnowledgeAgentContent(message));
            replies.add(knowledge);
            usedAgents.add(KNOWLEDGE_AGENT);
        }

        if (allowCustom && selected.contains("custom-sub-agent")) {
            MultiAgentNodeReply custom = new MultiAgentNodeReply();
            custom.setAgentName(customName);
            custom.setRole("custom-sub-agent");
            custom.setContent(llmService.ask(customPrompt + "\n\n历史上下文:\n" + historyText + "\n用户问题:\n" + message));
            replies.add(custom);
            usedAgents.add(customName);
        }

        if (replies.isEmpty()) {
            throw new BusinessException("未启用任何可用agent，请检查系统配置或前端选择。");
        }

        StringBuilder finalContext = new StringBuilder();
        finalContext.append("用户问题: ").append(message).append("\n");
        if (!historyText.isBlank()) {
            finalContext.append("历史上下文:\n").append(historyText).append("\n");
        }
        for (MultiAgentNodeReply reply : replies) {
            finalContext.append("[").append(reply.getAgentName()).append("]").append(reply.getContent()).append("\n\n");
        }
        finalContext.append("请作为总控agent整合以上多agent信息，输出最终答复，要求条理化、可执行。");

        MultiAgentChatResponse response = new MultiAgentChatResponse();
        response.setNodeReplies(replies);
        String finalAnswer = llmService.ask(finalContext.toString());
        response.setFinalAnswer(finalAnswer);
        response.setUsedAgents(usedAgents);
        response.setSessionId(sessionId);

        sessionStore.appendMessage(sessionId, "user", message);
        for (MultiAgentNodeReply reply : replies) {
            sessionStore.appendMessage(sessionId, reply.getAgentName(), reply.getContent());
        }
        sessionStore.appendMessage(sessionId, "assistant-final", finalAnswer);
        sessionStore.saveAudit(sessionId, operator, "multi-chat", true, "used agents: " + String.join(",", usedAgents));
        return response;
    }

    public MultiAgentConfigResponse config() {
        MultiAgentConfigResponse response = new MultiAgentConfigResponse();
        response.setAllowCustomAgent(properties.getMultiAgent().isAllowCustomAgent());
        response.setMaxHistory(Math.max(1, properties.getMultiAgent().getMaxHistory()));
        response.setMaxMessageLength(Math.max(100, properties.getMultiAgent().getMaxMessageLength()));
        response.setEnabledAgents(properties.getMultiAgent().getEnabledAgents() == null
                ? Collections.emptyList() : properties.getMultiAgent().getEnabledAgents());
        return response;
    }

    public String createSession(String title) {
        String actualTitle = (title == null || title.isBlank()) ? "AI Multi-Agent Session" : title.trim();
        String actualOperator = resolveCurrentOperator();
        String sessionId = sessionStore.createSession(actualTitle, actualOperator);
        sessionStore.saveAudit(sessionId, actualOperator, "create-session", true, actualTitle);
        return sessionId;
    }

    public List<AiAgentSessionDto> sessions() {
        return sessionStore.listSessions();
    }

    public List<AiAgentMessageDto> messages(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Collections.emptyList();
        }
        return sessionStore.listMessages(sessionId);
    }

    private Set<String> resolveSelectedAgents(List<String> selectedAgents) {
        List<String> enabled = properties.getMultiAgent().getEnabledAgents();
        Set<String> enabledSet = new HashSet<>(enabled == null ? Collections.emptyList() : enabled);
        if (selectedAgents == null || selectedAgents.isEmpty()) {
            Set<String> defaults = new HashSet<>(enabledSet);
            if (properties.getMultiAgent().isAllowCustomAgent()) {
                defaults.add("custom-sub-agent");
            }
            return defaults;
        }
        Set<String> selected = new HashSet<>();
        for (String item : selectedAgents) {
            if (item == null || item.isBlank()) {
                continue;
            }
            String normalized = item.trim();
            if (enabledSet.contains(normalized) || ("custom-sub-agent".equals(normalized) && properties.getMultiAgent().isAllowCustomAgent())) {
                selected.add(normalized);
            }
        }
        return selected;
    }

    private String normalizeHistory(String sessionId, List<String> history) {
        List<String> source = history;
        if (sessionId != null && !sessionId.isBlank()) {
            List<AiAgentMessageDto> messages = sessionStore.listMessages(sessionId);
            if (messages != null && !messages.isEmpty()) {
                List<String> dbHistory = new ArrayList<>();
                for (AiAgentMessageDto item : messages) {
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

    private boolean hasCustomAgentPermission(String operatorRole) {
        if (!properties.getMultiAgent().isAllowCustomAgent()) {
            return false;
        }
        List<String> roles = properties.getMultiAgent().getCustomAgentAllowedRoles();
        if (roles == null || roles.isEmpty()) {
            return true;
        }
        if (operatorRole == null || operatorRole.isBlank()) {
            // If user role is unavailable from context, degrade to allow.
            return true;
        }
        for (String role : roles) {
            if (role != null && operatorRole.equals(role.trim().toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private String ensureSession(String sessionId, String operator) {
        if (sessionId != null && !sessionId.isBlank()) {
            return sessionId;
        }
        return createSession("AI Multi-Agent Session");
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

    private String resolveCurrentRole() {
        try {
            Object user = UserContext.getUser();
            if (user == null) return "";
            Method getRole = user.getClass().getMethod("getRole");
            Object role = getRole.invoke(user);
            return role == null ? "" : String.valueOf(role).trim().toUpperCase();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String buildInspectionAgentContent(String message) {
        AiInspectionReportRequest reportRequest = new AiInspectionReportRequest();
        reportRequest.setDays(properties.getDefaultDays());
        reportRequest.setLimit(Math.min(50, properties.getDefaultLimit()));
        reportRequest.setIncludeOnlyAbnormal(true);
        if (message != null && !message.isBlank()) {
            reportRequest.setEquipName(message.length() > 64 ? message.substring(0, 64) : message);
        }
        List<InspectRecordDto> records = inspectionRecordQueryService.listRecords(reportRequest);
        List<InspectRecordDto> abnormal = inspectionRecordQueryService.filterAbnormal(records, true);
        int abnormalCount = inspectionRecordQueryService.countAbnormal(abnormal);
        return "巡检接口调用完成（REST->omes-portal）：近" + reportRequest.getDays() + "天共检索 " + records.size()
                + " 条，异常 " + abnormalCount + " 条。建议优先处理高频异常设备并生成专项复检任务。";
    }

    private String buildKnowledgeAgentContent(String message) {
        if (portalKnowledgeProxyService.enabled()) {
            String answer = portalKnowledgeProxyService.ask(message, 5);
            return (answer == null || answer.isBlank()) ? "知识库暂无匹配。" : answer;
        }
        List<String> docs = knowledgeService.searchKnowledge(message, 5);
        return (docs == null || docs.isEmpty()) ? "知识库暂无匹配。" : String.join("\n---\n", docs);
    }
}
