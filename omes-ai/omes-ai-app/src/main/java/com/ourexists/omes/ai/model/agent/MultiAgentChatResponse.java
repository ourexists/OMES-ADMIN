package com.ourexists.omes.ai.model.agent;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MultiAgentChatResponse {

    private String finalAnswer;

    private List<MultiAgentNodeReply> nodeReplies;

    private List<String> usedAgents;

    private String sessionId;
}
