package com.ourexists.omes.ai.agent.tool;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ToolRegistry {

    private final Map<String, AiAgentTool> tools = new HashMap<>();

    public ToolRegistry(List<AiAgentTool> toolList) {
        for (AiAgentTool tool : toolList) {
            tools.put(tool.name(), tool);
        }
    }

    public AiAgentTool get(String name) {
        return tools.get(name);
    }

    public List<String> names() {
        return tools.keySet().stream().sorted().toList();
    }
}
