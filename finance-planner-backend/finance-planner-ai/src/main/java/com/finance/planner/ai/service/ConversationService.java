package com.finance.planner.ai.service;

import com.finance.planner.ai.dto.ConversationHistoryDto;
import com.finance.planner.ai.entity.AiConversation;

import java.util.List;

public interface ConversationService {

    String createOrGetSessionId(String sessionId);

    void saveMessage(Long userId, String sessionId, String role, String content, Integer tokens);

    void saveMessage(Long userId, String sessionId, String role, String content, Integer tokens,
                     String messageType, String toolCalls, String toolCallId);

    List<AiConversation> getRecentMessages(Long userId, String sessionId, int rounds);

    List<AiConversation> getRecentMessagesWithLimit(Long userId, String sessionId, int limit);

    ConversationHistoryDto getConversationHistory(Long userId, String sessionId);
}
