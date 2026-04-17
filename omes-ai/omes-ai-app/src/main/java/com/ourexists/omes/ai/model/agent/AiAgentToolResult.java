package com.ourexists.omes.ai.model.agent;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class AiAgentToolResult {

    private String toolName;

    private boolean success;

    private String message;

    private Map<String, Object> data;
}
