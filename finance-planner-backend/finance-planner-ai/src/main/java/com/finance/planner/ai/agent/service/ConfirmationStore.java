package com.finance.planner.ai.agent.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class ConfirmationStore {

    private final Map<String, ConfirmationEntry> store = new ConcurrentHashMap<>();

    public String create(Long userId, String toolCallId, String toolName) {
        String id = UUID.randomUUID().toString().replace("-", "");
        ConfirmationEntry entry = new ConfirmationEntry();
        entry.setUserId(userId);
        entry.setToolCallId(toolCallId);
        entry.setToolName(toolName);
        entry.setCreatedAt(Instant.now());
        entry.setFuture(new CompletableFuture<>());
        store.put(id, entry);
        return id;
    }

    public boolean resolve(Long userId, String confirmationId, boolean accepted) {
        ConfirmationEntry entry = store.get(confirmationId);
        if (entry == null || !entry.getUserId().equals(userId)) {
            return false;
        }
        entry.getFuture().complete(accepted);
        store.remove(confirmationId);
        return true;
    }

    public boolean awaitDecision(String confirmationId, long timeoutSeconds) {
        ConfirmationEntry entry = store.get(confirmationId);
        if (entry == null) {
            return false;
        }
        try {
            Boolean accepted = entry.getFuture().get(timeoutSeconds, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(accepted);
        } catch (Exception e) {
            entry.getFuture().complete(false);
            store.remove(confirmationId);
            return false;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConfirmationEntry {
        private Long userId;
        private String toolCallId;
        private String toolName;
        private Instant createdAt;
        private CompletableFuture<Boolean> future;
    }
}
