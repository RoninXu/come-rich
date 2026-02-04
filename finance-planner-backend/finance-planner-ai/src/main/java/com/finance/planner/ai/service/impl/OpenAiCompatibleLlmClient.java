package com.finance.planner.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.planner.ai.config.AiConfig;
import com.finance.planner.ai.config.LlmProviderProperties;
import com.finance.planner.ai.dto.LlmStreamEvent;
import com.finance.planner.ai.dto.ToolCallChunk;
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

    @Override
    public Flux<LlmStreamEvent> streamChatWithTools(
            List<Map<String, Object>> messages,
            List<Map<String, Object>> tools,
            String toolChoice
    ) {
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
        if (tools != null && !tools.isEmpty()) {
            requestBody.put("tools", tools);
            requestBody.put("tool_choice", toolChoice == null ? "auto" : toolChoice);
        }

        log.debug("Calling LLM API with tools: {} model: {}", apiUrl, config.getModel());

        return webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> !line.isBlank() && !line.equals("[DONE]"))
                .flatMap(line -> {
                    LlmStreamEvent event = extractEvent(line);
                    return event != null ? Flux.just(event) : Flux.empty();
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

    private LlmStreamEvent extractEvent(String data) {
        try {
            String jsonStr = data.startsWith("data: ") ? data.substring(6).trim() : data.trim();
            if (jsonStr.equals("[DONE]") || jsonStr.isEmpty()) {
                return null;
            }
            JsonNode root = objectMapper.readTree(jsonStr);
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return null;
            }
            JsonNode choice = choices.get(0);
            JsonNode delta = choice.path("delta");
            String finishReason = choice.hasNonNull("finish_reason") ? choice.get("finish_reason").asText() : null;

            String content = null;
            if (delta.hasNonNull("content")) {
                content = delta.get("content").asText();
            }

            List<ToolCallChunk> toolCalls = null;
            JsonNode toolCallsNode = delta.path("tool_calls");
            if (toolCallsNode.isArray() && !toolCallsNode.isEmpty()) {
                toolCalls = new java.util.ArrayList<>();
                for (JsonNode callNode : toolCallsNode) {
                    ToolCallChunk chunk = ToolCallChunk.builder()
                            .index(callNode.hasNonNull("index") ? callNode.get("index").asInt() : null)
                            .id(callNode.hasNonNull("id") ? callNode.get("id").asText() : null)
                            .name(callNode.path("function").hasNonNull("name") ? callNode.get("function").get("name").asText() : null)
                            .arguments(callNode.path("function").hasNonNull("arguments") ? callNode.get("function").get("arguments").asText() : null)
                            .build();
                    toolCalls.add(chunk);
                }
            }

            if ((content == null || content.isEmpty())
                    && (toolCalls == null || toolCalls.isEmpty())
                    && finishReason == null) {
                return null;
            }

            return LlmStreamEvent.builder()
                    .content(content)
                    .toolCalls(toolCalls)
                    .finishReason(finishReason)
                    .build();
        } catch (Exception e) {
            log.trace("Failed to parse SSE tool chunk: {}", data);
            return null;
        }
    }
}
