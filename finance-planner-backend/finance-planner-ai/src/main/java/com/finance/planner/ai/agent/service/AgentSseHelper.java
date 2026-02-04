package com.finance.planner.ai.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.planner.ai.agent.dto.AgentConfirmationPayload;
import com.finance.planner.ai.agent.dto.AgentToolCallPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentSseHelper {

    private final ObjectMapper objectMapper;

    public void sendContent(SseEmitter emitter, String sessionId, String content) {
        try {
            Map<String, String> data = new HashMap<>();
            data.put("sessionId", sessionId);
            data.put("content", content);
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(data)));
        } catch (Exception e) {
            log.error("Failed to send content SSE: {}", e.getMessage());
        }
    }

    public void sendToolCallStart(SseEmitter emitter, AgentToolCallPayload payload) {
        sendNamedEvent(emitter, "tool_call_start", payload);
    }

    public void sendToolCallResult(SseEmitter emitter, AgentToolCallPayload payload) {
        sendNamedEvent(emitter, "tool_call_result", payload);
    }

    public void sendConfirmationRequired(SseEmitter emitter, AgentConfirmationPayload payload) {
        sendNamedEvent(emitter, "confirmation_required", payload);
    }

    public void sendConfirmationResolved(SseEmitter emitter, Map<String, Object> payload) {
        sendNamedEvent(emitter, "confirmation_resolved", payload);
    }

    public void sendDone(SseEmitter emitter, String sessionId) {
        Map<String, String> data = new HashMap<>();
        data.put("sessionId", sessionId);
        sendNamedEvent(emitter, "done", data);
    }

    public void sendError(SseEmitter emitter, String message) {
        Map<String, String> data = new HashMap<>();
        data.put("error", message);
        sendNamedEvent(emitter, "error", data);
    }

    private void sendNamedEvent(SseEmitter emitter, String eventName, Object payload) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(objectMapper.writeValueAsString(payload)));
        } catch (Exception e) {
            log.error("Failed to send SSE event {}: {}", eventName, e.getMessage());
        }
    }
}
