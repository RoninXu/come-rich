package com.finance.planner.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.planner.ai.config.AiConfig;
import com.finance.planner.ai.config.LlmProviderProperties;
import com.finance.planner.ai.service.LlmClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiCompatibleLlmClient implements LlmClient {

    @Qualifier("llmWebClient")
    private final WebClient webClient;
    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper;

    @Override
    public Flux<String> streamChat(List<Map<String, String>> messages) {
        LlmProviderProperties config = aiConfig.getActiveProviderConfig();
        if (config == null) {
            return Flux.error(new RuntimeException("No active LLM provider configured"));
        }

        String apiUrl = config.getApiUrl();
        String apiKey = config.getApiKey();

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModel());
        requestBody.put("messages", messages);
        requestBody.put("stream", true);
        requestBody.put("temperature", config.getTemperature());
        requestBody.put("max_tokens", config.getMaxTokens());

        log.debug("Calling LLM API: {} with model: {}", apiUrl, config.getModel());

        return webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> !line.isBlank() && !line.equals("[DONE]"))
                .flatMap(line -> {
                    String content = extractContent(line);
                    return (content != null && !content.isEmpty())
                            ? Flux.just(content) : Flux.empty();
                })
                .onErrorResume(e -> {
                    log.error("LLM API call failed: {}", e.getMessage());
                    return Flux.error(new RuntimeException("AI service call failed: " + e.getMessage()));
                });
    }

    private String extractContent(String data) {
        try {
            // Handle SSE format: may already be just JSON or may have "data: " prefix
            String jsonStr = data.startsWith("data: ") ? data.substring(6).trim() : data.trim();
            if (jsonStr.equals("[DONE]") || jsonStr.isEmpty()) {
                return null;
            }
            JsonNode root = objectMapper.readTree(jsonStr);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode delta = choices.get(0).path("delta");
                JsonNode content = delta.path("content");
                if (!content.isMissingNode() && !content.isNull()) {
                    return content.asText();
                }
            }
            return null;
        } catch (Exception e) {
            log.trace("Failed to parse SSE chunk: {}", data);
            return null;
        }
    }
}
