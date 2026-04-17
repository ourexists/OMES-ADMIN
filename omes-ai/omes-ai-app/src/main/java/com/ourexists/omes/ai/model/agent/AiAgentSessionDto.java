package com.ourexists.omes.ai.model.agent;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AiAgentSessionDto {

    private String sessionId;

    private String title;

    private String operatorId;

    private Date createdAt;

    private Date updatedAt;
}
