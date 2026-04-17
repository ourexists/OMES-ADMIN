package com.ourexists.omes.ai.model.agent;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MultiAgentConfigResponse {

    private boolean allowCustomAgent;

    private int maxHistory;

    private int maxMessageLength;

    private List<String> enabledAgents;
}
