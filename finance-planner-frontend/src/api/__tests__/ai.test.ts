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
  getAgentErrorMetrics,
  getAgentMetricsOverview,
  getAgentMetricsTimeline,
  getAgentToolMetrics,
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

  it("getAgentMetricsOverview gets /ai/agent/metrics/overview", () => {
    getAgentMetricsOverview({ days: 7 });
    expect(request.get).toHaveBeenCalledWith("/ai/agent/metrics/overview", {
      params: { days: 7 },
    });
  });

  it("getAgentToolMetrics gets /ai/agent/metrics/tools", () => {
    getAgentToolMetrics({ startDate: "2026-03-01", endDate: "2026-03-03", limit: 10 });
    expect(request.get).toHaveBeenCalledWith("/ai/agent/metrics/tools", {
      params: { startDate: "2026-03-01", endDate: "2026-03-03", limit: 10 },
    });
  });

  it("getAgentMetricsTimeline gets /ai/agent/metrics/timeline", () => {
    getAgentMetricsTimeline({ days: 30 });
    expect(request.get).toHaveBeenCalledWith("/ai/agent/metrics/timeline", {
      params: { days: 30 },
    });
  });

  it("getAgentErrorMetrics gets /ai/agent/metrics/errors", () => {
    getAgentErrorMetrics({ days: 7, limit: 5 });
    expect(request.get).toHaveBeenCalledWith("/ai/agent/metrics/errors", {
      params: { days: 7, limit: 5 },
    });
  });
});
