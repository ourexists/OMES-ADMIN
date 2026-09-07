package com.ourexists.omes.ai.agent.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AgentChatNodeReply {

    private String agentName;

    private String role;

    private String content;
}
