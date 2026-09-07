package com.ourexists.omes.ai.agent.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AgentChatRequest {

    private String message;

    /**
     * 可选对话历史（纯文本）
     */
    private List<String> history;

    /**
     * 前端选择执行的agent列表。
     */
    private List<String> selectedAgents;

    /**
     * 会话ID（用于持久化会话上下文）
     */
    private String sessionId;
}
