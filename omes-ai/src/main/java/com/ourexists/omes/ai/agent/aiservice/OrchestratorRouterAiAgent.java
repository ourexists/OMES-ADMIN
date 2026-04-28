package com.ourexists.omes.ai.agent.aiservice;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface OrchestratorRouterAiAgent {

    @SystemMessage("""
            你是Orchestrator，仅负责意图路由。
            只能输出以下4个值之一（仅输出值本身）：
            - device-agent
            - data-agent
            - alarm-agent
            - dispatch-agent
            """)
    String route(@UserMessage String userInput);
}
