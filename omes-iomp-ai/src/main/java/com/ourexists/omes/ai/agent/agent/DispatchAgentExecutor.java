package com.ourexists.omes.ai.agent.agent;

import com.ourexists.omes.ai.shared.service.MultiProviderLlmService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class DispatchAgentExecutor implements AgentExecutor {
    private static final Set<String> ROUTE_AGENT_IDS = Set.of("device-agent", "data-agent", "alarm-agent");

    private final MultiProviderLlmService llmService;
    private final Map<String, AgentExecutor> subAgentMap;

    public DispatchAgentExecutor(MultiProviderLlmService llmService, List<AgentExecutor> executors) {
        this.llmService = llmService;
        this.subAgentMap = new LinkedHashMap<>();
        if (executors != null) {
            for (AgentExecutor executor : executors) {
                if (executor == null || executor.id() == null || executor.id().isBlank()) {
                    continue;
                }
                if (!id().equals(executor.id()) && ROUTE_AGENT_IDS.contains(executor.id())) {
                    this.subAgentMap.put(executor.id(), executor);
                }
            }
        }
    }

    @Override
    public String id() {
        return "dispatch-agent";
    }

    @Override
    public String role() {
        return "dispatch";
    }

    @Override
    public String execute(String sessionId, String message, String historyText, String provider, String modelName) {
        String heuristicRoute = guessRouteByKeyword(message);
        String routeKeys = subAgentMap.isEmpty() ? "NONE" : String.join(", ", subAgentMap.keySet());
        String routePrompt = """
                你是调度中心(Dispatcher Agent)。
                目标：根据用户问题判断是否需要把任务分发到某个子Agent。

                意图参考：
                - 设备状态、设备清单、工位/产线设备信息 => device-agent
                - 指标趋势、时序分析、统计报表、波动诊断 => data-agent
                - 告警处置、规则命中、阈值异常、告警升级 => alarm-agent

                可选路由：
                %s

                输出要求（只能二选一）：
                1) 若需要分发，严格输出：ROUTE:<agentId>
                2) 若不需要分发，严格输出：ANSWER:<你给用户的直接回答>

                约束：
                - 只输出一行纯文本，不要JSON，不要Markdown，不要额外解释。
                - 只有在可选路由内的agentId才允许使用。
                """.formatted(routeKeys);

        String routeDecisionPrompt = """
                %s

                历史上下文：
                %s

                用户问题：
                %s
                """.formatted(
                routePrompt,
                historyText == null ? "" : historyText,
                message == null ? "" : message
        );
        String raw = llmService.askWithModel(provider, modelName, routeDecisionPrompt);
        String decision = raw == null ? "" : raw.trim();
        if (decision.isEmpty()) {
            if (heuristicRoute != null) {
                AgentExecutor guessedAgent = subAgentMap.get(heuristicRoute);
                if (guessedAgent != null) {
                    return guessedAgent.execute(sessionId, message, historyText, provider, modelName);
                }
            }
            return "我暂时无法判断应调用哪个子Agent，请补充更具体的问题描述。";
        }

        String routePrefix = "ROUTE:";
        String answerPrefix = "ANSWER:";
        String upperDecision = decision.toUpperCase(Locale.ROOT);
        if (upperDecision.startsWith(routePrefix)) {
            String routeId = decision.substring(routePrefix.length()).trim();
            AgentExecutor subAgent = subAgentMap.get(routeId);
            if (subAgent != null) {
                return subAgent.execute(sessionId, message, historyText, provider, modelName);
            }
            if (heuristicRoute != null) {
                AgentExecutor guessedAgent = subAgentMap.get(heuristicRoute);
                if (guessedAgent != null) {
                    return guessedAgent.execute(sessionId, message, historyText, provider, modelName);
                }
            }
            return "未找到可用子Agent(" + routeId + ")，请确认路由配置。";
        }

        if (upperDecision.startsWith(answerPrefix)) {
            String directAnswer = decision.substring(answerPrefix.length()).trim();
            if (!directAnswer.isEmpty()) {
                return directAnswer;
            }
        }

        AgentExecutor directRoute = subAgentMap.get(decision);
        if (directRoute != null) {
            return directRoute.execute(sessionId, message, historyText, provider, modelName);
        }
        if (heuristicRoute != null) {
            AgentExecutor guessedAgent = subAgentMap.get(heuristicRoute);
            if (guessedAgent != null) {
                return guessedAgent.execute(sessionId, message, historyText, provider, modelName);
            }
        }

        String prompt = """
                你是调度中心(Dispatcher Agent)，请直接回答用户问题。
                你可以给出清晰步骤、建议和风险提示，不要输出JSON。

                历史上下文：
                %s

                用户问题：
                %s
                """.formatted(
                historyText == null ? "" : historyText,
                message == null ? "" : message
        );
        return llmService.askWithModel(provider, modelName, prompt);
    }

    private String guessRouteByKeyword(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String text = message.toLowerCase(Locale.ROOT);
        if (containsAny(text, "告警", "报警", "阈值", "规则", "误报", "漏报", "升级", "rule", "alarm", "alert")) {
            return "alarm-agent";
        }
        if (containsAny(text, "趋势", "时序", "统计", "报表", "波动", "曲线", "指标", "同比", "环比", "timeseries", "trend", "metric")) {
            return "data-agent";
        }
        if (containsAny(text, "设备", "工位", "产线", "plc", "机台", "点检", "故障", "停机", "稼动")) {
            return "device-agent";
        }
        return null;
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null || keywords == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (keyword != null && !keyword.isBlank() && text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
