export interface ChatMessage {
  id?: number;
  sessionId: string;
  role: "user" | "assistant" | "tool" | "system";
  content: string;
  tokens?: number;
  createdAt?: string;
  isStreaming?: boolean;
  messageType?: "text" | "tool_call" | "tool_result" | "confirmation";
  toolCallId?: string;
  toolName?: string;
  toolArguments?: string;
  toolResult?: string;
  toolStatus?: "running" | "done" | "error";
  confirmationId?: string;
  confirmationStatus?: "required" | "resolved";
  confirmationAccepted?: boolean;
  riskLevel?: "LOW" | "MEDIUM" | "HIGH";
}

export interface ConversationHistory {
  sessionId: string;
  messages: ChatMessage[];
  totalMessages: number;
}

export type AgentEventType =
  | "message"
  | "tool_call_start"
  | "tool_call_result"
  | "confirmation_required"
  | "confirmation_resolved"
  | "done"
  | "error";

export interface AgentStreamEvent {
  event: AgentEventType;
  data: any;
}
