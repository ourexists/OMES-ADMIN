package com.ourexists.omes.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "agent")
public class AgentProperties {

    private int topK = 5;

    /**
     * in-memory | qdrant
     */
    private String vectorProvider = "in-memory";
    private Qdrant qdrant = new Qdrant();

    /**
     * OMES Portal 服务地址，AI 服务通过此地址调用 portal 的业务接口。
     */
    private String toolCallUri = "http://127.0.0.1:10010";

    /**
     * 是否启用 agent 工具经由 portal 的 REST 代理调用。
     */
    private boolean agentProxyEnabled = false;

    private MultiAgent multiAgent = new MultiAgent();
    private Session session = new Session();

    public String getPortalBaseUrl() {
        return toolCallUri;
    }

    public boolean isAgentPortalProxyEnabled() {
        return agentProxyEnabled;
    }

    @Getter
    @Setter
    public static class MultiAgent {
        /**
         * 每次会话允许带入历史条数上限。
         */
        private int maxHistory = 20;

        /**
         * 滑动窗口记忆保留的最大消息条数（短期模型上下文）；不大于 0 时使用 maxHistory。
         */
        private int memoryWindowMessages = 0;

        /**
         * 用户输入最大长度。
         */
        private int maxMessageLength = 2000;

    }

    @Getter
    @Setter
    public static class Session {
        /**
         * Redis 中会话消息列表与 LangChain4j 滑动窗口记忆的 TTL（分钟）。
         */
        private int shortTermTtlMinutes = 120;
    }

    @Getter
    @Setter
    public static class Qdrant {
        private String host = "localhost";
        private int port = 6334;
        private String collectionName = "omes_inspection_kb";
        private String apiKey = "";
        private boolean useTls = false;
    }
}
