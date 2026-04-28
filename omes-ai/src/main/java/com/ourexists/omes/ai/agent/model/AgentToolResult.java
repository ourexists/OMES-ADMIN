package com.ourexists.omes.ai.agent.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class AgentToolResult {

    private String toolName;

    private boolean success;

    private String message;

    private Map<String, Object> data;
}
