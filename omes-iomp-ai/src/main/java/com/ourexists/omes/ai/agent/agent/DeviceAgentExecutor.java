package com.ourexists.omes.ai.agent.agent;

import com.ourexists.omes.ai.agent.aiservice.AiAgentFactory;
import com.ourexists.omes.ai.agent.aiservice.DeviceAiAgent;
import com.ourexists.omes.ai.agent.tool.DeviceDataSummaryTool;
import com.ourexists.omes.ai.agent.tool.DispatchSummaryTool;
import org.springframework.stereotype.Component;

@Component
public class DeviceAgentExecutor implements AgentExecutor {

    private final AiAgentFactory aiAgentFactory;
    private final DeviceDataSummaryTool deviceDataSummaryTool;
    private final DispatchSummaryTool dispatchSummaryTool;

    public DeviceAgentExecutor(AiAgentFactory aiAgentFactory,
                               DeviceDataSummaryTool deviceDataSummaryTool,
                               DispatchSummaryTool dispatchSummaryTool) {
        this.aiAgentFactory = aiAgentFactory;
        this.deviceDataSummaryTool = deviceDataSummaryTool;
        this.dispatchSummaryTool = dispatchSummaryTool;
    }

    @Override
    public String id() {
        return "device-agent";
    }

    @Override
    public String role() {
        return "device";
    }

    @Override
    public String execute(String sessionId, String message, String historyText, String provider, String modelName) {
        DeviceAiAgent agent = aiAgentFactory.createWithSessionMemory(
                DeviceAiAgent.class,
                provider,
                modelName,
                deviceDataSummaryTool,
                dispatchSummaryTool
        );
        String sid = sessionId == null || sessionId.isBlank() ? "_" : sessionId;
        return agent.analyze(sid, message == null ? "" : message);
    }
}
