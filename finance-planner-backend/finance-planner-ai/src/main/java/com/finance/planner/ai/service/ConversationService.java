package com.finance.planner.ai.service;

import com.finance.planner.ai.dto.ConversationHistoryDto;
import com.finance.planner.ai.entity.AiConversation;

import java.util.List;

public interface ConversationService {

    String createOrGetSessionId(String sessionId);

    void saveMessage(Long userId, String sessionId, String role, String content, Integer tokens);

    List<AiConversation> getRecentMessages(Long userId, String sessionId, int rounds);

    ConversationHistoryDto getConversationHistory(Long userId, String sessionId);
}
