package com.ourexists.omes.ai.model.agent;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MultiAgentChatRequest {

    private String message;

    /**
     * 自定义子agent名称
     */
    private String customAgentName;

    /**
     * 自定义子agent系统提示词
     */
    private String customAgentPrompt;

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
