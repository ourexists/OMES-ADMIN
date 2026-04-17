package com.ourexists.omes.ai.model.agent;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiAgentRunResponse {

    private String finalAnswer;

    private List<AiAgentToolResult> toolResults;
}
