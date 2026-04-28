package com.ourexists.omes.ai.agent.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Component
public class ToolRegistry {

    private final Map<String, AgentTool> tools = new HashMap<>();
    private final Map<String, String> toolDescriptions = new HashMap<>();

    public ToolRegistry(List<AgentTool> toolList) {
        for (AgentTool tool : toolList) {
            tools.put(tool.name(), tool);
            toolDescriptions.put(tool.name(), resolveToolDescription(tool));
        }
    }

    public AgentTool get(String name) {
        return tools.get(name);
    }

    public List<String> names() {
        return tools.keySet().stream().sorted().toList();
    }

    public String describe(String name) {
        return toolDescriptions.getOrDefault(name, "");
    }

    public Map<String, String> describeAll() {
        return new HashMap<>(toolDescriptions);
    }

    private String resolveToolDescription(AgentTool tool) {
        for (Method method : tool.getClass().getMethods()) {
            Tool annotation = method.getAnnotation(Tool.class);
            if (annotation == null) {
                continue;
            }
            StringJoiner joiner = new StringJoiner("; ");
            String[] values = annotation.value();
            if (values != null && values.length > 0 && values[0] != null && !values[0].isBlank()) {
                joiner.add(values[0].trim());
            }
            java.lang.reflect.Parameter[] parameters = method.getParameters();
            if (parameters.length > 0) {
                StringJoiner paramJoiner = new StringJoiner(", ");
                for (java.lang.reflect.Parameter parameter : parameters) {
                    P p = parameter.getAnnotation(P.class);
                    if (p != null && p.value() != null && !p.value().isBlank()) {
                        paramJoiner.add(parameter.getName() + ": " + p.value().trim());
                    } else {
                        paramJoiner.add(parameter.getName());
                    }
                }
                joiner.add("参数: " + paramJoiner);
            }
            return joiner.toString();
        }
        return "";
    }
}
