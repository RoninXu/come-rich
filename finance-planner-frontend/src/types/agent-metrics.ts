export interface AgentMetricsOverview {
  totalCalls: number;
  successfulCalls: number;
  failedCalls: number;
  totalSessions: number;
  successRate: number;
  averageLatencyMs: number;
  cacheHitRate: number;
}

export interface AgentToolMetric {
  toolName: string;
  totalCalls: number;
  successfulCalls: number;
  failedCalls: number;
  successRate: number;
  averageLatencyMs: number;
  cacheHitRate: number;
}

export interface AgentMetricsTimelinePoint {
  date: string;
  totalCalls: number;
  successfulCalls: number;
  failedCalls: number;
  successRate: number;
  averageLatencyMs: number;
}

export interface AgentErrorMetric {
  errorMessage: string;
  count: number;
}

