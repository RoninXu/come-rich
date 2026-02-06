package com.finance.planner.ai.agent.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConfirmationStore cancelAllForUser Tests")
class ConfirmationStoreCancelTest {

    @Test
    @DisplayName("cancelAllForUser - should cancel all pending confirmations for user")
    void cancelAllForUser_cancelsAll() throws Exception {
        ConfirmationStore store = new ConfirmationStore();
        String id1 = store.create(1L, "call-1", "tool-1");
        String id2 = store.create(1L, "call-2", "tool-2");
        String id3 = store.create(2L, "call-3", "tool-3");

        store.cancelAllForUser(1L);

        // User 1's confirmations should resolve as false (entries removed, awaitDecision returns false)
        assertThat(store.awaitDecision(id1, 1)).isFalse();
        assertThat(store.awaitDecision(id2, 1)).isFalse();

        // User 2's confirmation should still be resolvable
        CompletableFuture<Boolean> resolved = CompletableFuture.supplyAsync(() -> store.awaitDecision(id3, 5));
        // Ensure awaitDecision has started before resolving
        Thread.sleep(100);
        store.resolve(2L, id3, true);
        assertThat(resolved.get(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    @DisplayName("cancelAllForUser - should handle no pending confirmations")
    void cancelAllForUser_noPending() {
        ConfirmationStore store = new ConfirmationStore();
        // Should not throw
        store.cancelAllForUser(1L);
    }
}
