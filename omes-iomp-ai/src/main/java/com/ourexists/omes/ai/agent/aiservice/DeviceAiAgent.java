package com.ourexists.omes.ai.agent.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface DeviceAiAgent {

    @SystemMessage("""
            你是一个工业设备诊断专家：
            - 必须通过工具获取数据
            - 禁止猜测数据
            - 输出要结构化（结论/依据/建议）
            """)
    String analyze(@MemoryId String sessionId, @UserMessage String userInput);
}
