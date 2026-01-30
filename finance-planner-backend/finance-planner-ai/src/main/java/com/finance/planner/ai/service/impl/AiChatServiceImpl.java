package com.finance.planner.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.planner.ai.dto.ConversationHistoryDto;
import com.finance.planner.ai.service.*;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final RateLimitService rateLimitService;
    private final ConversationService conversationService;
    private final PromptBuilder promptBuilder;
    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    @Override
    public void streamChat(Long userId, String message, String sessionId, SseEmitter emitter) {
        // 1. Rate limit check
        if (!rateLimitService.isAllowed(userId)) {
            throw new BusinessException(ErrorCode.AI_RATE_LIMIT_EXCEEDED);
        }

        // 2. Get or create session
        String finalSessionId = conversationService.createOrGetSessionId(sessionId);

        // 3. Save user message
        conversationService.saveMessage(userId, finalSessionId, "user", message, null);

        // 4. Build messages for LLM
        List<Map<String, String>> messages = promptBuilder.buildMessages(userId, finalSessionId, message);

        // 5. Stream LLM response
        StringBuilder fullResponse = new StringBuilder();

        llmClient.streamChat(messages)
                .publishOn(Schedulers.boundedElastic())
                .subscribe(
                        chunk -> {
                            try {
                                fullResponse.append(chunk);
                                Map<String, String> data = new HashMap<>();
                                data.put("sessionId", finalSessionId);
                                data.put("content", chunk);
                                emitter.send(SseEmitter.event()
                                        .data(objectMapper.writeValueAsString(data)));
                            } catch (Exception e) {
                                log.error("Error sending SSE chunk: {}", e.getMessage());
                            }
                        },
                        error -> {
                            log.error("LLM streaming error for user {}: {}", userId, error.getMessage());
                            try {
                                Map<String, String> errorData = new HashMap<>();
                                errorData.put("error", "AI 服务暂时不可用，请稍后重试");
                                emitter.send(SseEmitter.event()
                                        .name("error")
                                        .data(objectMapper.writeValueAsString(errorData)));
                            } catch (Exception e) {
                                log.error("Error sending error event: {}", e.getMessage());
                            }
                            emitter.complete();
                        },
                        () -> {
                            try {
                                // Save complete AI response
                                String response = fullResponse.toString();
                                if (!response.isEmpty()) {
                                    conversationService.saveMessage(userId, finalSessionId, "assistant", response, null);
                                }
                                // Send done signal
                                emitter.send(SseEmitter.event().data("[DONE]"));
                                emitter.complete();
                                log.debug("Completed AI chat stream for user {} session {}", userId, finalSessionId);
                            } catch (Exception e) {
                                log.error("Error completing SSE stream: {}", e.getMessage());
                                emitter.complete();
                            }
                        }
                );
    }

    @Override
    public ConversationHistoryDto getHistory(Long userId, String sessionId) {
        return conversationService.getConversationHistory(userId, sessionId);
    }

    @Override
    public int getRemainingChats(Long userId) {
        return rateLimitService.getRemainingChats(userId);
    }
}
