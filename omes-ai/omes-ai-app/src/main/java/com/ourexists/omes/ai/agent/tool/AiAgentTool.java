package com.ourexists.omes.ai.agent.tool;

import com.ourexists.omes.ai.model.agent.AiAgentToolResult;

import java.util.Map;

public interface AiAgentTool {

    String name();

    AiAgentToolResult execute(Map<String, Object> arguments);
}
