package com.finance.planner.ai.service.impl;

import com.finance.planner.ai.dto.ChatMessageDto;
import com.finance.planner.ai.dto.ConversationHistoryDto;
import com.finance.planner.ai.entity.AiConversation;
import com.finance.planner.ai.repository.AiConversationRepository;
import com.finance.planner.ai.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final AiConversationRepository aiConversationRepository;

    @Override
    public String createOrGetSessionId(String sessionId) {
        if (StringUtils.hasText(sessionId)) {
            return sessionId;
        }
        String newSessionId = UUID.randomUUID().toString().replace("-", "");
        log.debug("Created new session: {}", newSessionId);
        return newSessionId;
    }

    @Override
    public void saveMessage(Long userId, String sessionId, String role, String content, Integer tokens) {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setSessionId(sessionId);
        conversation.setRole(role);
        conversation.setContent(content);
        conversation.setTokens(tokens);
        aiConversationRepository.save(conversation);
        log.debug("Saved {} message for user {} in session {}", role, userId, sessionId);
    }

    @Override
    public List<AiConversation> getRecentMessages(Long userId, String sessionId, int rounds) {
        // Get recent messages ordered by createdAt DESC, then reverse
        List<AiConversation> recent = aiConversationRepository
                .findTop10ByUserIdAndSessionIdOrderByCreatedAtDesc(userId, sessionId);
        if (recent.isEmpty()) {
            return Collections.emptyList();
        }
        // Limit to the desired number of rounds (each round = 1 user + 1 assistant message = 2 entries)
        int maxMessages = rounds * 2;
        List<AiConversation> limited = recent.size() > maxMessages
                ? recent.subList(0, maxMessages)
                : recent;
        // Reverse to chronological order
        List<AiConversation> result = new ArrayList<>(limited);
        Collections.reverse(result);
        return result;
    }

    @Override
    public ConversationHistoryDto getConversationHistory(Long userId, String sessionId) {
        List<AiConversation> conversations = aiConversationRepository
                .findByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);
        List<ChatMessageDto> messages = conversations.stream()
                .map(ChatMessageDto::fromEntity)
                .toList();
        return ConversationHistoryDto.builder()
                .sessionId(sessionId)
                .messages(messages)
                .totalMessages(messages.size())
                .build();
    }
}
