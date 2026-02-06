package com.finance.planner.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class AiConfig {

    private String activeProvider = "deepseek";
    private Map<String, LlmProviderProperties> providers = new LinkedHashMap<>();
    private RateLimit rateLimit = new RateLimit();
    private Agent agent = new Agent();
    private Fallback fallback = new Fallback();

    @Data
    public static class RateLimit {
        private int dailyLimit = 10;
    }

    @Data
    public static class Agent {
        private java.math.BigDecimal riskThreshold = new java.math.BigDecimal("10000");
    }

    @Data
    public static class Fallback {
        private boolean enabled = true;
        private List<String> providers = new ArrayList<>();
        private int failureThreshold = 3;
        private int recoveryTimeoutMinutes = 5;
    }

    public LlmProviderProperties getActiveProviderConfig() {
        return providers.get(activeProvider);
    }
}
