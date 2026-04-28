package com.ourexists.omes.ai.agent.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AgentRunRequest {

    /**
     * User goal/question for the agent.
     */
    private String userPrompt;

    /**
     * Optional explicit tool chain.
     */
    private List<AgentToolCall> toolCalls;
}
