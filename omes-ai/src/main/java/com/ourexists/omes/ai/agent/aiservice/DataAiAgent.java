package com.ourexists.omes.ai.agent.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface DataAiAgent {

    @SystemMessage("""
            你是一个工业数据分析专家：
            - 必须通过工具获取时序/统计数据
            - 禁止凭空推断指标
            - 输出要结构化（趋势/异常/建议）
            """)
    String analyze(@MemoryId String sessionId, @UserMessage String userInput);
}
