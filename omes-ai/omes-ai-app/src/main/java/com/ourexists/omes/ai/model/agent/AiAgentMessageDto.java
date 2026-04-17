package com.ourexists.omes.ai.model.agent;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AiAgentMessageDto {

    private String id;

    private String sessionId;

    private String role;

    private String content;

    private Date createdAt;
}
