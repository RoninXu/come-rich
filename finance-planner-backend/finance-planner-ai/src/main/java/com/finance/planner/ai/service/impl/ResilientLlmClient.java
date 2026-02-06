package com.finance.planner.ai.service.impl;

import com.finance.planner.ai.config.AiConfig;
import com.finance.planner.ai.dto.LlmStreamEvent;
import com.finance.planner.ai.service.LlmClient;
import com.finance.planner.ai.service.LlmProviderManager;
import com.finance.planner.ai.service.ProviderHealthTracker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resilient LLM client that wraps the actual client with auto-failover.
 * When the primary provider fails, it automatically tries the next healthy provider.
 */
@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class ResilientLlmClient implements LlmClient {

    private final OpenAiCompatibleLlmClient delegate;
    private final LlmProviderManager providerManager;
    private final ProviderHealthTracker healthTracker;
    private final AiConfig aiConfig;

    @Override
    public Flux<String> streamChat(List<Map<String, String>> messages) {
        if (!aiConfig.getFallback().isEnabled()) {
            return delegate.streamChat(messages);
        }
        List<String> providers = getHealthyProviders();
        if (providers.isEmpty()) {
            return Flux.error(new RuntimeException("所有 AI 服务提供商均不可用"));
        }
        return tryStreamChat(messages, providers, 0);
    }

    @Override
    public Flux<LlmStreamEvent> streamChatWithTools(
            List<Map<String, Object>> messages,
            List<Map<String, Object>> tools,
            String toolChoice
    ) {
        if (!aiConfig.getFallback().isEnabled()) {
            return delegate.streamChatWithTools(messages, tools, toolChoice);
        }
        List<String> providers = getHealthyProviders();
        if (providers.isEmpty()) {
            return Flux.error(new RuntimeException("所有 AI 服务提供商均不可用"));
        }
        return tryStreamChatWithTools(messages, tools, toolChoice, providers, 0);
    }

    private Flux<String> tryStreamChat(List<Map<String, String>> messages, List<String> providers, int index) {
        if (index >= providers.size()) {
            return Flux.error(new RuntimeException("所有 AI 服务提供商均不可用"));
        }
        String provider = providers.get(index);
        providerManager.switchProvider(provider);
        long startTime = System.currentTimeMillis();

        return delegate.streamChat(messages)
                .doOnComplete(() -> {
                    long latency = System.currentTimeMillis() - startTime;
                    healthTracker.recordSuccess(provider, latency);
                })
                .onErrorResume(error -> {
                    healthTracker.recordFailure(provider, error.getMessage());
                    log.warn("Provider {} failed for streamChat, trying next: {}", provider, error.getMessage());
                    return tryStreamChat(messages, providers, index + 1);
                });
    }

    private Flux<LlmStreamEvent> tryStreamChatWithTools(
            List<Map<String, Object>> messages,
            List<Map<String, Object>> tools,
            String toolChoice,
            List<String> providers,
            int index
    ) {
        if (index >= providers.size()) {
            return Flux.error(new RuntimeException("所有 AI 服务提供商均不可用"));
        }
        String provider = providers.get(index);
        providerManager.switchProvider(provider);
        long startTime = System.currentTimeMillis();

        return delegate.streamChatWithTools(messages, tools, toolChoice)
                .doOnComplete(() -> {
                    long latency = System.currentTimeMillis() - startTime;
                    healthTracker.recordSuccess(provider, latency);
                })
                .onErrorResume(error -> {
                    healthTracker.recordFailure(provider, error.getMessage());
                    log.warn("Provider {} failed for streamChatWithTools, trying next: {}", provider, error.getMessage());
                    return tryStreamChatWithTools(messages, tools, toolChoice, providers, index + 1);
                });
    }

    private List<String> getHealthyProviders() {
        List<String> fallbackOrder = providerManager.getFallbackOrder();
        List<String> healthy = fallbackOrder.stream()
                .filter(healthTracker::isHealthy)
                .collect(Collectors.toList());
        if (healthy.isEmpty()) {
            log.warn("No healthy providers found, attempting all providers in fallback order");
            return fallbackOrder;
        }
        return healthy;
    }
}
