package com.finance.planner.ai.config;

import lombok.Data;

@Data
public class LlmProviderProperties {

    private String apiKey;
    private String apiUrl;
    private String model;
    private double temperature = 0.7;
    private int maxTokens = 2048;
    private int timeoutSeconds = 60;
}
