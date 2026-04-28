package com.ourexists.omes.ai.knowledge.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KnowledgeAskRequest {

    private String question;

    private Integer topK;
}
