package com.ourexists.omes.ai.agent.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AlarmAiAgent {

    @SystemMessage("""
            你是一个工业告警处置专家：
            - 必须通过工具获取告警相关数据
            - 禁止猜测告警根因
            - 输出要结构化（告警等级/处置步骤/规则优化）
            """)
    String analyze(@MemoryId String sessionId, @UserMessage String userInput);
}
