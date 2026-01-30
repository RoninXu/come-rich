package com.finance.planner.ai.service.impl;

import com.finance.planner.ai.config.AiConfig;
import com.finance.planner.ai.config.LlmProviderProperties;
import com.finance.planner.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LlmProviderManagerImpl Unit Tests")
class LlmProviderManagerImplTest {

    @Mock
    private AiConfig aiConfig;

    @InjectMocks
    private LlmProviderManagerImpl providerManager;

    private Map<String, LlmProviderProperties> providers;

    @BeforeEach
    void setUp() {
        providers = new LinkedHashMap<>();
        providers.put("deepseek", new LlmProviderProperties());
        providers.put("moonshot", new LlmProviderProperties());
    }

    @Test
    @DisplayName("getActiveProvider - should return current active provider")
    void getActiveProvider() {
        when(aiConfig.getActiveProvider()).thenReturn("deepseek");

        String active = providerManager.getActiveProvider();

        assertThat(active).isEqualTo("deepseek");
    }

    @Test
    @DisplayName("getActiveConfig - should delegate to AiConfig")
    void getActiveConfig() {
        LlmProviderProperties expected = new LlmProviderProperties();
        expected.setModel("deepseek-chat");
        when(aiConfig.getActiveProviderConfig()).thenReturn(expected);

        LlmProviderProperties config = providerManager.getActiveConfig();

        assertThat(config.getModel()).isEqualTo("deepseek-chat");
    }

    @Test
    @DisplayName("switchProvider - should switch to valid provider")
    void switchProvider_valid() {
        when(aiConfig.getProviders()).thenReturn(providers);
        when(aiConfig.getActiveProvider()).thenReturn("deepseek");

        providerManager.switchProvider("moonshot");

        verify(aiConfig).setActiveProvider("moonshot");
    }

    @Test
    @DisplayName("switchProvider - should throw for invalid provider")
    void switchProvider_invalid() {
        when(aiConfig.getProviders()).thenReturn(providers);

        assertThatThrownBy(() -> providerManager.switchProvider("nonexistent"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AI 模型提供商不存在");
    }

    @Test
    @DisplayName("listProviders - should return all provider names")
    void listProviders() {
        when(aiConfig.getProviders()).thenReturn(providers);

        List<String> names = providerManager.listProviders();

        assertThat(names).containsExactly("deepseek", "moonshot");
    }
}
