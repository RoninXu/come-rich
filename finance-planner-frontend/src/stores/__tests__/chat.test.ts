import { describe, it, expect, vi, beforeEach } from "vitest";
import { setActivePinia, createPinia } from "pinia";

// Mock API module
vi.mock("@/api/ai", () => ({
  streamChat: vi.fn(),
  streamAgentChat: vi.fn(),
  confirmAgentAction: vi.fn(),
  getConversationHistory: vi.fn(),
  getRemainingChats: vi.fn(),
  getProviders: vi.fn(),
  getCurrentProvider: vi.fn(),
  switchProvider: vi.fn(),
}));

import { useChatStore } from "../chat";
import {
  getRemainingChats,
  getProviders,
  getCurrentProvider,
  switchProvider,
} from "@/api/ai";

describe("chat store", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it("should have correct initial state", () => {
    const store = useChatStore();
    expect(store.messages).toEqual([]);
    expect(store.currentSessionId).toBeNull();
    expect(store.isStreaming).toBe(false);
    expect(store.remainingChats).toBe(10);
    expect(store.error).toBeNull();
    expect(store.currentProvider).toBe("");
    expect(store.providers).toEqual([]);
  });

  it("startNewSession should reset state", () => {
    const store = useChatStore();
    // Simulate some state
    store.messages = [
      { sessionId: "old", role: "user", content: "test" },
      { sessionId: "old", role: "assistant", content: "response" },
    ];
    store.currentSessionId = "old-session";
    store.error = "some error";

    store.startNewSession();

    expect(store.messages).toEqual([]);
    expect(store.currentSessionId).toBeNull();
    expect(store.error).toBeNull();
  });

  it("sendMessage should add user and assistant messages", async () => {
    const store = useChatStore();

    // Mock streamChat to be an async generator that yields nothing (just completes)
    const { streamChat } = await import("@/api/ai");
    (streamChat as any).mockImplementation(async function* () {
      yield { sessionId: "new-session", content: "Hi" };
      yield { content: " there" };
      yield { done: true };
    });

    await store.sendMessage("Hello");

    expect(store.messages).toHaveLength(2);
    expect(store.messages[0].role).toBe("user");
    expect(store.messages[0].content).toBe("Hello");
    expect(store.messages[1].role).toBe("assistant");
    expect(store.messages[1].content).toBe("Hi there");
    expect(store.messages[1].isStreaming).toBe(false);
    expect(store.isStreaming).toBe(false);
    expect(store.currentSessionId).toBe("new-session");
  });

  it("sendMessage should handle stream errors", async () => {
    const store = useChatStore();

    const { streamChat } = await import("@/api/ai");
    (streamChat as any).mockImplementation(async function* () {
      yield { error: "Service unavailable" };
    });

    await store.sendMessage("Hello");

    expect(store.messages).toHaveLength(2);
    expect(store.messages[1].content).toBe("Service unavailable");
    expect(store.error).toBe("Service unavailable");
    expect(store.isStreaming).toBe(false);
  });

  it("fetchRemainingChats should update remaining count", async () => {
    const store = useChatStore();
    (getRemainingChats as any).mockResolvedValue({
      data: { code: 200, data: 7 },
    });

    await store.fetchRemainingChats();

    expect(store.remainingChats).toBe(7);
  });

  it("fetchProviders should update providers and current provider", async () => {
    const store = useChatStore();
    (getProviders as any).mockResolvedValue({
      data: { code: 200, data: ["deepseek", "moonshot", "qwen"] },
    });
    (getCurrentProvider as any).mockResolvedValue({
      data: { code: 200, data: "deepseek" },
    });

    await store.fetchProviders();

    expect(store.providers).toEqual(["deepseek", "moonshot", "qwen"]);
    expect(store.currentProvider).toBe("deepseek");
  });

  it("switchProviderAction should call API and update state", async () => {
    const store = useChatStore();
    (switchProvider as any).mockResolvedValue({
      data: { code: 200 },
    });

    await store.switchProviderAction("moonshot");

    expect(switchProvider).toHaveBeenCalledWith("moonshot");
    expect(store.currentProvider).toBe("moonshot");
  });
});
