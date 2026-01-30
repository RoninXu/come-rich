package com.finance.planner.ai.service;

import com.finance.planner.ai.config.LlmProviderProperties;

import java.util.List;

public interface LlmProviderManager {

    String getActiveProvider();

    LlmProviderProperties getActiveConfig();

    void switchProvider(String providerName);

    List<String> listProviders();
}
