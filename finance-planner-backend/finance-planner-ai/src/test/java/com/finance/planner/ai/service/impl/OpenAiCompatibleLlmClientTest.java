package com.finance.planner.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.planner.ai.config.AiConfig;
import com.finance.planner.ai.config.LlmProviderProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OpenAiCompatibleLlmClient Unit Tests")
class OpenAiCompatibleLlmClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private AiConfig aiConfig;

    private OpenAiCompatibleLlmClient llmClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        llmClient = new OpenAiCompatibleLlmClient(webClient, aiConfig, objectMapper);
    }

    @Test
    @DisplayName("streamChat - should return error when no provider configured")
    void streamChat_noProvider() {
        when(aiConfig.getActiveProviderConfig()).thenReturn(null);

        List<Map<String, String>> messages = List.of(createMessage("user", "Hello"));

        StepVerifier.create(llmClient.streamChat(messages))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("streamChat - should parse SSE response chunks")
    void streamChat_parsesChunks() {
        LlmProviderProperties config = new LlmProviderProperties();
        config.setApiKey("test-key");
        config.setApiUrl("https://api.example.com/v1/chat/completions");
        config.setModel("test-model");
        config.setTemperature(0.7);
        config.setMaxTokens(2048);
        when(aiConfig.getActiveProviderConfig()).thenReturn(config);

        // Mock WebClient chain
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Simulate SSE responses
        String chunk1 = "{\"choices\":[{\"delta\":{\"content\":\"Hello\"}}]}";
        String chunk2 = "{\"choices\":[{\"delta\":{\"content\":\" world\"}}]}";
        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.just(chunk1, chunk2));

        List<Map<String, String>> messages = List.of(createMessage("user", "Hi"));

        StepVerifier.create(llmClient.streamChat(messages))
                .expectNext("Hello")
                .expectNext(" world")
                .verifyComplete();
    }

    @Test
    @DisplayName("streamChat - should filter out empty and DONE chunks")
    void streamChat_filtersSpecialChunks() {
        LlmProviderProperties config = new LlmProviderProperties();
        config.setApiKey("test-key");
        config.setApiUrl("https://api.example.com/v1/chat/completions");
        config.setModel("test-model");
        when(aiConfig.getActiveProviderConfig()).thenReturn(config);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        String validChunk = "{\"choices\":[{\"delta\":{\"content\":\"Hi\"}}]}";
        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.just(validChunk, "", "[DONE]", "  "));

        List<Map<String, String>> messages = List.of(createMessage("user", "Hello"));

        StepVerifier.create(llmClient.streamChat(messages))
                .expectNext("Hi")
                .verifyComplete();
    }

    @Test
    @DisplayName("streamChat - should handle malformed JSON gracefully")
    void streamChat_handlesMalformedJson() {
        LlmProviderProperties config = new LlmProviderProperties();
        config.setApiKey("test-key");
        config.setApiUrl("https://api.example.com/v1/chat/completions");
        config.setModel("test-model");
        when(aiConfig.getActiveProviderConfig()).thenReturn(config);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        String validChunk = "{\"choices\":[{\"delta\":{\"content\":\"OK\"}}]}";
        String malformedChunk = "not json at all";
        when(responseSpec.bodyToFlux(String.class))
                .thenReturn(Flux.just(malformedChunk, validChunk));

        List<Map<String, String>> messages = List.of(createMessage("user", "Hello"));

        StepVerifier.create(llmClient.streamChat(messages))
                .expectNext("OK")
                .verifyComplete();
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> msg = new HashMap<>();
        msg.put("role", role);
        msg.put("content", content);
        return msg;
    }
}
