package com.finance.planner.ai.agent.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AgentService {

    void streamAgentChat(Long userId, String message, String sessionId, SseEmitter emitter);

    boolean respondToConfirmation(Long userId, String confirmationId, boolean accepted);
}
