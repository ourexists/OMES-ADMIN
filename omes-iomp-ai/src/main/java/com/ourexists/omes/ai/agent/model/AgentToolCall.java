package com.ourexists.omes.ai.agent.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class AgentToolCall {

    /**
     * Tool name, e.g. inspection.query / knowledge.search / knowledge.ingest
     */
    private String toolName;

    /**
     * Tool arguments in JSON object format.
     */
    private Map<String, Object> arguments;
}
