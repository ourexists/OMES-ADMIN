package com.ourexists.omes.ai.agent.agent;

public interface AgentExecutor {
    String id();

    String role();

    String execute(String sessionId, String message, String historyText, String provider, String modelName);
}
