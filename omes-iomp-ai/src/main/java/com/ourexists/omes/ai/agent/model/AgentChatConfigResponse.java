package com.ourexists.omes.ai.agent.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AgentChatConfigResponse {

    private int maxHistory;

    /**
     * LangChain4j 滑动窗口记忆条数（短期上下文）。
     */
    private int memoryWindowMessages;

    /**
     * Redis / 进程内短期记忆 TTL（分钟）。
     */
    private int shortTermTtlMinutes;

    private int maxMessageLength;

    private List<String> enabledAgents;
}
