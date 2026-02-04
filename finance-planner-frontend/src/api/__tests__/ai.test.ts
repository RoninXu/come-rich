import { describe, it, expect, vi, beforeEach } from "vitest";

// Mock request utility
vi.mock("@/utils/request", () => ({
  default: {
    post: vi.fn(),
    get: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
}));

// Mock auth utility
vi.mock("@/utils/auth", () => ({
  getToken: vi.fn(() => "test-token"),
}));

import request from "@/utils/request";
import {
  getConversationHistory,
  getRemainingChats,
  getProviders,
  getCurrentProvider,
  switchProvider,
} from "../ai";

describe("ai API", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("getConversationHistory gets /ai/history with sessionId", () => {
    getConversationHistory("session-123");
    expect(request.get).toHaveBeenCalledWith("/ai/history", {
      params: { sessionId: "session-123" },
    });
  });

  it("getRemainingChats gets /ai/remaining", () => {
    getRemainingChats();
    expect(request.get).toHaveBeenCalledWith("/ai/remaining");
  });

  it("getProviders gets /ai/providers", () => {
    getProviders();
    expect(request.get).toHaveBeenCalledWith("/ai/providers");
  });

  it("getCurrentProvider gets /ai/provider", () => {
    getCurrentProvider();
    expect(request.get).toHaveBeenCalledWith("/ai/provider");
  });

  it("switchProvider puts to /ai/provider with name param", () => {
    switchProvider("moonshot");
    expect(request.put).toHaveBeenCalledWith("/ai/provider", null, {
      params: { name: "moonshot" },
    });
  });
});
