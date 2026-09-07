package com.ourexists.omes.ai.agent.agent;

import com.ourexists.omes.ai.agent.aiservice.AiAgentFactory;
import com.ourexists.omes.ai.agent.aiservice.DataAiAgent;
import com.ourexists.omes.ai.agent.tool.DeviceDataSummaryTool;
import org.springframework.stereotype.Component;

@Component
public class DataAgentExecutor implements AgentExecutor {

    private final AiAgentFactory aiAgentFactory;
    private final DeviceDataSummaryTool deviceDataSummaryTool;

    public DataAgentExecutor(AiAgentFactory aiAgentFactory,
                             DeviceDataSummaryTool deviceDataSummaryTool) {
        this.aiAgentFactory = aiAgentFactory;
        this.deviceDataSummaryTool = deviceDataSummaryTool;
    }

    @Override
    public String id() {
        return "data-agent";
    }

    @Override
    public String role() {
        return "data";
    }

    @Override
    public String execute(String sessionId, String message, String historyText, String provider, String modelName) {
        DataAiAgent agent = aiAgentFactory.createWithSessionMemory(
                DataAiAgent.class,
                provider,
                modelName,
                deviceDataSummaryTool
        );
        String sid = sessionId == null || sessionId.isBlank() ? "_" : sessionId;
        return agent.analyze(sid, message == null ? "" : message);
    }
}
