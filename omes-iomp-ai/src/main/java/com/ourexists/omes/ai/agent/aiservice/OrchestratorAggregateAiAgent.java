package com.ourexists.omes.ai.agent.aiservice;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface OrchestratorAggregateAiAgent {

    @SystemMessage("""
            你是Orchestrator聚合器。
            请严格输出JSON对象，字段固定为：
            - summary: string
            - evidence: string[]
            - actions: string[]
            - selectedAgent: string
            禁止输出Markdown和额外说明。
            """)
    String aggregate(@UserMessage String userInput);
}
