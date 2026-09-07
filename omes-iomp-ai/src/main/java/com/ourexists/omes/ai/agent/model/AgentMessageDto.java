package com.ourexists.omes.ai.agent.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AgentMessageDto {

    private String id;

    private String sessionId;

    private String role;

    private String content;

    private Date createdAt;
}
