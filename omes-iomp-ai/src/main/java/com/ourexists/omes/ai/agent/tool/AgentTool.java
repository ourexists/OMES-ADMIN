package com.ourexists.omes.ai.agent.tool;

import com.ourexists.omes.ai.agent.model.AgentToolResult;

import java.util.Map;

public interface AgentTool {

    String name();

    AgentToolResult execute(Map<String, Object> arguments);
}
