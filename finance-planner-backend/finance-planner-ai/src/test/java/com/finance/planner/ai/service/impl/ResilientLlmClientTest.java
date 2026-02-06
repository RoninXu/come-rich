package com.finance.planner.ai.service.impl;

import com.finance.planner.ai.config.AiConfig;
import com.finance.planner.ai.dto.LlmStreamEvent;
import com.finance.planner.ai.service.LlmProviderManager;
import com.finance.planner.ai.service.ProviderHealthTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResilientLlmClient Unit Tests")
class ResilientLlmClientTest {

    @Mock
    private OpenAiCompatibleLlmClient delegate;

    @Mock
    private LlmProviderManager providerManager;

    @Mock
    private ProviderHealthTracker healthTracker;

    @Mock
    private AiConfig aiConfig;

    @InjectMocks
    private ResilientLlmClient resilientClient;

    private AiConfig.Fallback fallback;

    @BeforeEach
    void setUp() {
        fallback = new AiConfig.Fallback();
        fallback.setEnabled(true);
        fallback.setProviders(List.of("deepseek", "moonshot", "qwen"));
        lenient().when(aiConfig.getFallback()).thenReturn(fallback);
        lenient().when(providerManager.getFallbackOrder()).thenReturn(List.of("deepseek", "moonshot", "qwen"));
        lenient().when(healthTracker.isHealthy(anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("Should fallback to next provider when primary fails")
    void fallbackOnPrimaryFailure() {
        LlmStreamEvent event = LlmStreamEvent.builder().content("response").finishReason("stop").build();

        when(delegate.streamChatWithTools(any(), any(), any()))
                .thenReturn(Flux.error(new RuntimeException("deepseek down")))
                .thenReturn(Flux.just(event));

        StepVerifier.create(resilientClient.streamChatWithTools(Collections.emptyList(), Collections.emptyList(), "auto"))
                .expectNext(event)
                .verifyComplete();

        verify(healthTracker).recordFailure("deepseek", "deepseek down");
        verify(providerManager).switchProvider("moonshot");
    }

    @Test
    @DisplayName("Should throw when all providers fail")
    void allProvidersFail() {
        when(delegate.streamChatWithTools(any(), any(), any()))
                .thenReturn(Flux.error(new RuntimeException("error")));

        StepVerifier.create(resilientClient.streamChatWithTools(Collections.emptyList(), Collections.emptyList(), "auto"))
                .expectErrorMessage("所有 AI 服务提供商均不可用")
                .verify();
    }

    @Test
    @DisplayName("Should skip unhealthy providers")
    void skipUnhealthyProviders() {
        when(healthTracker.isHealthy("deepseek")).thenReturn(false);
        when(healthTracker.isHealthy("moonshot")).thenReturn(true);
        when(healthTracker.isHealthy("qwen")).thenReturn(true);

        LlmStreamEvent event = LlmStreamEvent.builder().content("ok").finishReason("stop").build();
        when(delegate.streamChatWithTools(any(), any(), any())).thenReturn(Flux.just(event));

        StepVerifier.create(resilientClient.streamChatWithTools(Collections.emptyList(), Collections.emptyList(), "auto"))
                .expectNext(event)
                .verifyComplete();

        verify(providerManager).switchProvider("moonshot");
        verify(providerManager, never()).switchProvider("deepseek");
    }

    @Test
    @DisplayName("Should bypass fallback when disabled")
    void bypassWhenDisabled() {
        fallback.setEnabled(false);

        LlmStreamEvent event = LlmStreamEvent.builder().content("direct").finishReason("stop").build();
        when(delegate.streamChatWithTools(any(), any(), any())).thenReturn(Flux.just(event));

        StepVerifier.create(resilientClient.streamChatWithTools(Collections.emptyList(), Collections.emptyList(), "auto"))
                .expectNext(event)
                .verifyComplete();

        verify(providerManager, never()).switchProvider(anyString());
    }
}
