package com.ourexists.omes.ai.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiKnowledgeAskRequest {

    private String question;

    private Integer topK;
}
