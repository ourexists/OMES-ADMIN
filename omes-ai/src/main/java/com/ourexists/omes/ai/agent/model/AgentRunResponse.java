package com.ourexists.omes.ai.agent.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AgentRunResponse {

    private String finalAnswer;

    private List<AgentToolResult> toolResults;
}
