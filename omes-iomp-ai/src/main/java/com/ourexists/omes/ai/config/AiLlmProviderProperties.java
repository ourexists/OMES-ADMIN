package com.ourexists.omes.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.llm")
public class AiLlmProviderProperties {

    private String defaultProvider = "openai";
    private ProviderConfig openai = new ProviderConfig();
    private ProviderConfig deepseek = new ProviderConfig();
    private ProviderConfig qwen = new ProviderConfig();
    private ProviderConfig anthropic = new ProviderConfig();
    private ProviderConfig gemini = new ProviderConfig();

    @Getter
    @Setter
    public static class ProviderConfig {
        private String apiKey = "";
        private String baseUrl = "";
        private String defaultModel = "";
        private String embeddingModel = "";
    }
}
