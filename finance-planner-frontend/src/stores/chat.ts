import { defineStore } from "pinia";
import { ref } from "vue";
import {
  streamChat,
  streamAgentChat,
  confirmAgentAction,
  getAgentRiskThreshold,
  setAgentRiskThreshold,
  getRemainingChats,
  getConversationHistory,
  getProviders,
  getCurrentProvider,
  switchProvider as switchProviderApi,
} from "@/api/ai";
import type { AgentStreamEvent, ChatMessage } from "@/types/ai";
import { mapAiError } from "@/utils/ai-error";

export const useChatStore = defineStore("chat", () => {
  const messages = ref<ChatMessage[]>([]);
  const currentSessionId = ref<string | null>(null);
  const isStreaming = ref(false);
  const isAgentMode = ref(false);
  const remainingChats = ref<number>(10);
  const error = ref<string | null>(null);
  const currentProvider = ref<string>("");
  const providers = ref<string[]>([]);
  const agentRiskThreshold = ref<number>(10000);

  async function sendMessage(content: string) {
    error.value = null;

    // Add user message
    const userMessage: ChatMessage = {
      sessionId: currentSessionId.value || "",
      role: "user",
      content,
    };
    messages.value.push(userMessage);

    // Add placeholder for AI response
    const aiMessage: ChatMessage = {
      sessionId: currentSessionId.value || "",
      role: "assistant",
      content: "",
      isStreaming: true,
    };
    messages.value.push(aiMessage);

    isStreaming.value = true;

    try {
      const generator = streamChat(
        content,
        currentSessionId.value || undefined,
      );
      for await (const chunk of generator) {
        if (chunk.error) {
          const friendly = mapAiError(undefined, chunk.error);
          error.value = friendly || chunk.error;
          aiMessage.content = friendly || chunk.error;
          aiMessage.isStreaming = false;
          break;
        }
        if (chunk.done) {
          aiMessage.isStreaming = false;
          break;
        }
        if (chunk.sessionId && !currentSessionId.value) {
          currentSessionId.value = chunk.sessionId;
          userMessage.sessionId = chunk.sessionId;
          aiMessage.sessionId = chunk.sessionId;
        }
        if (chunk.content) {
          aiMessage.content += chunk.content;
        }
      }
    } catch (e: any) {
      error.value = e.message || "Failed to get AI response";
      aiMessage.content = error.value || "Error";
      aiMessage.isStreaming = false;
    } finally {
      isStreaming.value = false;
      aiMessage.isStreaming = false;
    }
  }

  async function sendAgentMessage(content: string) {
    error.value = null;

    const userMessage: ChatMessage = {
      sessionId: currentSessionId.value || "",
      role: "user",
      content,
      messageType: "text",
    };
    messages.value.push(userMessage);

    const aiMessage: ChatMessage = {
      sessionId: currentSessionId.value || "",
      role: "assistant",
      content: "",
      isStreaming: true,
      messageType: "text",
    };
    messages.value.push(aiMessage);

    isStreaming.value = true;

    try {
      const generator = streamAgentChat(
        content,
        currentSessionId.value || undefined,
      );
      for await (const evt of generator) {
        handleAgentEvent(evt, userMessage, aiMessage);
        if (evt.event === "done") {
          aiMessage.isStreaming = false;
          break;
        }
      }
    } catch (e: any) {
      error.value = e.message || "Failed to get AI response";
      aiMessage.content = error.value || "Error";
      aiMessage.isStreaming = false;
    } finally {
      isStreaming.value = false;
      aiMessage.isStreaming = false;
    }
  }

  async function loadHistory(sessionId: string) {
    try {
      const response = await getConversationHistory(sessionId);
      const history = response.data.data;
      currentSessionId.value = history.sessionId;
      messages.value = history.messages.map((msg) => ({
        ...msg,
        isStreaming: false,
      }));
    } catch (e: any) {
      error.value = e.message || "Failed to load history";
    }
  }

  async function fetchRemainingChats() {
    try {
      const response = await getRemainingChats();
      remainingChats.value = response.data.data;
    } catch {
      // Silently fail
    }
  }

  function startNewSession() {
    messages.value = [];
    currentSessionId.value = null;
    error.value = null;
  }

  async function respondToConfirmation(
    confirmationId: string,
    accepted: boolean,
  ) {
    try {
      await confirmAgentAction(confirmationId, accepted);
      const target = messages.value.find(
        (m) => m.confirmationId === confirmationId,
      );
      if (target) {
        target.confirmationStatus = "resolved";
        target.confirmationAccepted = accepted;
      }
    } catch (e: any) {
      error.value = e.message || "Failed to confirm action";
    }
  }

  async function fetchAgentRiskThreshold() {
    try {
      const response = await getAgentRiskThreshold();
      agentRiskThreshold.value = Number(response.data.data) || 10000;
    } catch {
      // Silently fail
    }
  }

  async function updateAgentRiskThreshold(value: number) {
    try {
      await setAgentRiskThreshold(value);
      agentRiskThreshold.value = value;
    } catch (e: any) {
      error.value = e.message || "Failed to update risk threshold";
    }
  }

  async function fetchProviders() {
    try {
      const [providersRes, currentRes] = await Promise.all([
        getProviders(),
        getCurrentProvider(),
      ]);
      providers.value = providersRes.data.data;
      currentProvider.value = currentRes.data.data;
    } catch {
      // Silently fail
    }
  }

  async function switchProviderAction(name: string) {
    try {
      await switchProviderApi(name);
      currentProvider.value = name;
    } catch (e: any) {
      error.value = e.message || "Failed to switch provider";
    }
  }

  function handleAgentEvent(
    evt: AgentStreamEvent,
    userMessage: ChatMessage,
    aiMessage: ChatMessage,
  ) {
    if (evt.event === "message") {
      const data = evt.data || {};
      if (data.sessionId && !currentSessionId.value) {
        currentSessionId.value = data.sessionId;
        userMessage.sessionId = data.sessionId;
        aiMessage.sessionId = data.sessionId;
      }
      if (data.content) {
        aiMessage.content += data.content;
      }
      return;
    }

    if (evt.event === "tool_call_start") {
      const data = evt.data || {};
      const toolMessage: ChatMessage = {
        sessionId: aiMessage.sessionId || "",
        role: "tool",
        content: "",
        messageType: "tool_call",
        toolCallId: data.toolCallId,
        toolName: data.toolName,
        toolArguments: data.arguments,
        toolStatus: "running",
      };
      aiMessage.isStreaming = false;
      userMessage.isStreaming = false;
      aiMessage.content = aiMessage.content || "";
      messages.value.push(toolMessage);
      return;
    }

    if (evt.event === "tool_call_result") {
      const data = evt.data || {};
      const target = messages.value.find(
        (m) =>
          m.toolCallId === data.toolCallId && m.messageType === "tool_call",
      );
      const resultStr = data.result ? JSON.stringify(data.result, null, 2) : "";
      if (target) {
        target.toolResult = resultStr;
        target.toolStatus = data.result?.success ? "done" : "error";
        target.messageType = "tool_result";
      } else {
        messages.value.push({
          sessionId: aiMessage.sessionId || "",
          role: "tool",
          content: "",
          messageType: "tool_result",
          toolCallId: data.toolCallId,
          toolName: data.toolName,
          toolArguments: data.arguments,
          toolResult: resultStr,
          toolStatus: data.result?.success ? "done" : "error",
        });
      }
      return;
    }

    if (evt.event === "confirmation_required") {
      const data = evt.data || {};
      messages.value.push({
        sessionId: aiMessage.sessionId || "",
        role: "system",
        content: "",
        messageType: "confirmation",
        confirmationId: data.confirmationId,
        toolCallId: data.toolCallId,
        toolName: data.toolName,
        riskLevel: data.riskLevel,
        confirmationStatus: "required",
      });
      return;
    }

    if (evt.event === "confirmation_resolved") {
      const data = evt.data || {};
      const target = messages.value.find(
        (m) => m.confirmationId === data.confirmationId,
      );
      if (target) {
        target.confirmationStatus = "resolved";
        target.confirmationAccepted = data.accepted;
      }
      return;
    }

    if (evt.event === "error") {
      const data = evt.data || {};
      if (typeof data === "string") {
        const friendly = mapAiError(undefined, data);
        aiMessage.content = friendly || data || "AI Agent 服务异常";
      } else {
        const friendly = mapAiError(data.code, data.error);
        aiMessage.content = friendly || data.error || "AI Agent 服务异常";
      }
      aiMessage.isStreaming = false;
    }
  }
  return {
    messages,
    currentSessionId,
    isStreaming,
    isAgentMode,
    remainingChats,
    error,
    currentProvider,
    providers,
    agentRiskThreshold,
    sendMessage,
    sendAgentMessage,
    loadHistory,
    fetchRemainingChats,
    startNewSession,
    fetchProviders,
    switchProviderAction,
    respondToConfirmation,
    fetchAgentRiskThreshold,
    updateAgentRiskThreshold,
  };
});
