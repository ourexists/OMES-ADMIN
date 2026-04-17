package com.ourexists.omes.ai.model.agent;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AiAgentRunRequest {

    /**
     * User goal/question for the agent.
     */
    private String userPrompt;

    /**
     * Optional explicit tool chain.
     */
    private List<AiAgentToolCall> toolCalls;
}
