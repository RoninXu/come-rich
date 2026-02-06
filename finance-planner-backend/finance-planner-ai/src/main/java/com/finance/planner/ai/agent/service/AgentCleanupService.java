package com.finance.planner.ai.agent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentCleanupService {

    private final ConfirmationStore confirmationStore;

    public void cleanupSession(Long userId, String sessionId, String reason) {
        try {
            confirmationStore.cancelAllForUser(userId);
            log.info("Agent session cleaned: userId={} sessionId={} reason={}", userId, sessionId, reason);
        } catch (Exception e) {
            log.error("Error during agent session cleanup: userId={} sessionId={}", userId, sessionId, e);
        }
    }
}
