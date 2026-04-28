package com.ourexists.omes.ai.agent.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AgentChatResponse {

    private String finalAnswer;

    private List<AgentChatNodeReply> nodeReplies;

    private List<String> usedAgents;

    private String sessionId;
}
