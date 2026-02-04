import request from "@/utils/request";
import { getToken } from "@/utils/auth";
import type { AgentStreamEvent, ConversationHistory } from "@/types/ai";
import type { ApiResponse } from "@/types/api";
import { mapAiError } from "@/utils/ai-error";

/**
 * Stream AI chat response using fetch + ReadableStream.
 * Yields text chunks as they arrive via SSE.
 */
export async function* streamChat(
  message: string,
  sessionId?: string,
): AsyncGenerator<{
  sessionId?: string;
  content?: string;
  error?: string;
  done?: boolean;
}> {
  const token = getToken();
  const params = new URLSearchParams({ message });
  if (sessionId) {
    params.append("sessionId", sessionId);
  }

  const response = await fetch(`/api/ai/chat-stream?${params.toString()}`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
      Accept: "text/event-stream",
    },
  });

  const contentType = response.headers.get("content-type") || "";
  if (!response.ok || contentType.includes("application/json")) {
    try {
      const data = await response.json();
      const friendly = mapAiError(data?.code, data?.message);
      yield { error: friendly || data?.message || "AI 服务请求失败" };
    } catch {
      yield { error: "AI 服务请求失败" };
    }
    return;
  }

  const reader = response.body?.getReader();
  if (!reader) {
    yield { error: "无法读取响应流" };
    return;
  }

  let buffer = "";
  const firstChunk = await reader.read();
  if (firstChunk.value) {
    const firstText = new TextDecoder().decode(firstChunk.value, {
      stream: true,
    });
    const trimmed = firstText.trim();
    if (trimmed.startsWith("{")) {
      try {
        const data = JSON.parse(trimmed);
        const friendly = mapAiError(data?.code, data?.message);
        yield { error: friendly || data?.message || "AI 服务请求失败" };
        reader.releaseLock();
        return;
      } catch {
        // fall through to SSE parsing
      }
    }
    // continue SSE parsing with the first chunk content
    buffer = firstText;
  }

  const decoder = new TextDecoder();

  try {
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split("\n");
      buffer = lines.pop() || "";

      for (const line of lines) {
        const trimmed = line.trim();
        if (!trimmed || !trimmed.startsWith("data:")) continue;

        const dataStr = trimmed.substring(5).trim();
        if (dataStr === "[DONE]") {
          yield { done: true };
          return;
        }

        try {
          const data = JSON.parse(dataStr);
          if (data.error) {
            yield { error: data.error };
            return;
          }
          yield { sessionId: data.sessionId, content: data.content };
        } catch {
          // Skip non-JSON lines
        }
      }
    }
  } finally {
    reader.releaseLock();
  }
}

/**
 * Stream AI agent response using fetch + ReadableStream.
 * Supports named SSE events.
 */
export async function* streamAgentChat(
  message: string,
  sessionId?: string,
): AsyncGenerator<AgentStreamEvent> {
  const token = getToken();
  const params = new URLSearchParams({ message });
  if (sessionId) {
    params.append("sessionId", sessionId);
  }

  const response = await fetch(
    `/api/ai/agent/chat-stream?${params.toString()}`,
    {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: "text/event-stream",
      },
    },
  );

  const contentType = response.headers.get("content-type") || "";
  if (!response.ok || contentType.includes("application/json")) {
    try {
      const data = await response.json();
      const friendly = mapAiError(data?.code, data?.message);
      yield {
        event: "error",
        data: { error: friendly || data?.message || "AI Agent 服务请求失败" },
      };
    } catch {
      yield { event: "error", data: { error: "AI Agent 服务请求失败" } };
    }
    return;
  }

  const reader = response.body?.getReader();
  if (!reader) {
    yield { event: "error", data: { error: "无法读取响应流" } };
    return;
  }

  let buffer = "";
  const firstChunk = await reader.read();
  if (firstChunk.value) {
    const firstText = new TextDecoder().decode(firstChunk.value, {
      stream: true,
    });
    const trimmed = firstText.trim();
    if (trimmed.startsWith("{")) {
      try {
        const data = JSON.parse(trimmed);
        const friendly = mapAiError(data?.code, data?.message);
        yield {
          event: "error",
          data: { error: friendly || data?.message || "AI Agent 服务请求失败" },
        };
        reader.releaseLock();
        return;
      } catch {
        // fall through to SSE parsing
      }
    }
    // continue SSE parsing with the first chunk content
    buffer = firstText;
  }

  const decoder = new TextDecoder();
  let eventName = "message";
  let dataLines: string[] = [];

  try {
    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split("\n");
      buffer = lines.pop() || "";

      for (const line of lines) {
        const trimmed = line.replace(/\r$/, "");
        if (trimmed.startsWith("event:")) {
          eventName = trimmed.substring(6).trim() || "message";
          continue;
        }
        if (trimmed.startsWith("data:")) {
          dataLines.push(trimmed.substring(5).trim());
          continue;
        }
        if (trimmed === "") {
          if (dataLines.length === 0) {
            eventName = "message";
            continue;
          }
          const dataStr = dataLines.join("\n");
          dataLines = [];
          let data: any = dataStr;
          try {
            data = JSON.parse(dataStr);
          } catch {
            // keep raw string
          }
          yield { event: eventName as AgentStreamEvent["event"], data };
          eventName = "message";
        }
      }
    }
  } finally {
    reader.releaseLock();
  }
}

export function confirmAgentAction(confirmationId: string, accepted: boolean) {
  return request.post<ApiResponse<void>>("/ai/agent/confirm", {
    confirmationId,
    accepted,
  });
}

export function getAgentRiskThreshold() {
  return request.get<ApiResponse<number>>("/ai/agent/risk-threshold");
}

export function setAgentRiskThreshold(threshold: number) {
  return request.put<ApiResponse<void>>("/ai/agent/risk-threshold", null, {
    params: { threshold },
  });
}

export function getConversationHistory(sessionId: string) {
  return request.get<ApiResponse<ConversationHistory>>("/ai/history", {
    params: { sessionId },
  });
}

export function getRemainingChats() {
  return request.get<ApiResponse<number>>("/ai/remaining");
}

export function getProviders() {
  return request.get<ApiResponse<string[]>>("/ai/providers");
}

export function getCurrentProvider() {
  return request.get<ApiResponse<string>>("/ai/provider");
}

export function switchProvider(name: string) {
  return request.put<ApiResponse<void>>("/ai/provider", null, {
    params: { name },
  });
}
