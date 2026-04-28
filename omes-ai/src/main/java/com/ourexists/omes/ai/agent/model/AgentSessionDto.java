package com.ourexists.omes.ai.agent.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AgentSessionDto {

    private String sessionId;

    private String title;

    private String operatorId;

    private Date createdAt;

    private Date updatedAt;
}
