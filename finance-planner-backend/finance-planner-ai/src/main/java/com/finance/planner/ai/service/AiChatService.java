package com.finance.planner.ai.service;

import com.finance.planner.ai.dto.ConversationHistoryDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiChatService {

    void streamChat(Long userId, String message, String sessionId, SseEmitter emitter);

    ConversationHistoryDto getHistory(Long userId, String sessionId);

    int getRemainingChats(Long userId);
}
