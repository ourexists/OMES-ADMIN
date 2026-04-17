package com.ourexists.omes.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.inspection")
public class AiInspectionProperties {

    private boolean enabled = true;

    private int defaultDays = 7;

    private int defaultLimit = 50;

    private int topK = 5;

    /**
     * in-memory | qdrant | milvus | elasticsearch
     */
    private String vectorProvider = "in-memory";

    /**
     * OMES Portal 服务地址，AI 服务通过此地址调用 portal 的业务接口。
     */
    private String portalBaseUrl = "http://127.0.0.1:10010";

    /**
     * 是否启用 agent 工具经由 portal 的 REST 代理调用。
     */
    private boolean agentPortalProxyEnabled = false;

    private MultiAgent multiAgent = new MultiAgent();

    @Getter
    @Setter
    public static class MultiAgent {
        /**
         * 产品化开关：是否允许自定义子agent。
         */
        private boolean allowCustomAgent = true;

        /**
         * 每次会话允许带入历史条数上限。
         */
        private int maxHistory = 20;

        /**
         * 用户输入最大长度。
         */
        private int maxMessageLength = 2000;

        /**
         * 可启用的内置agent。
         */
        private List<String> enabledAgents = new ArrayList<>(List.of(
                "planner-agent",
                "inspection-agent",
                "knowledge-agent"
        ));

        /**
         * 可使用自定义子agent的角色白名单。
         */
        private List<String> customAgentAllowedRoles = new ArrayList<>(List.of(
                "ADMIN",
                "AI_ADMIN"
        ));
    }
}
