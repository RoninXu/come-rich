package com.finance.planner.ai.service.impl;

import com.finance.planner.ai.config.AiConfig;
import com.finance.planner.ai.config.LlmProviderProperties;
import com.finance.planner.ai.service.LlmProviderManager;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmProviderManagerImpl implements LlmProviderManager {

    private final AiConfig aiConfig;

    @Override
    public String getActiveProvider() {
        return aiConfig.getActiveProvider();
    }

    @Override
    public LlmProviderProperties getActiveConfig() {
        return aiConfig.getActiveProviderConfig();
    }

    @Override
    public void switchProvider(String providerName) {
        if (!aiConfig.getProviders().containsKey(providerName)) {
            throw new BusinessException(ErrorCode.AI_PROVIDER_NOT_FOUND);
        }
        String previous = aiConfig.getActiveProvider();
        aiConfig.setActiveProvider(providerName);
        log.info("Switched LLM provider from '{}' to '{}'", previous, providerName);
    }

    @Override
    public List<String> listProviders() {
        return new ArrayList<>(aiConfig.getProviders().keySet());
    }
}
