package com.finance.planner.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class AiConfig {

    private String activeProvider = "deepseek";
    private Map<String, LlmProviderProperties> providers = new LinkedHashMap<>();
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class RateLimit {
        private int dailyLimit = 10;
    }

    public LlmProviderProperties getActiveProviderConfig() {
        return providers.get(activeProvider);
    }
}
