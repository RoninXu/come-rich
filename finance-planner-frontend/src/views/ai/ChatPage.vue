<template>
  <div class="chat-page">
    <PageHeader title="AI 理财顾问" subtitle="提问、追问、并直接执行下一步" dense>
      <template #actions>
        <n-tag type="info" size="small">今日剩余 {{ chatStore.remainingChats }} 次</n-tag>
        <div class="mode-toggle">
          <span>Agent</span>
          <n-switch v-model:value="isAgentMode" size="small" />
        </div>
        <n-select
          v-model:value="selectedProvider"
          size="small"
          placeholder="选择模型"
          :options="providerOptions"
          style="width: 140px"
          @update:value="handleProviderChange"
        />
        <n-button size="small" @click="handleNewSession">新对话</n-button>
      </template>
    </PageHeader>

    <div class="chat-shell">
      <div ref="messageListRef" class="message-list">
        <div v-if="chatStore.messages.length === 0" class="empty-state">
          <h4>从一个明确目标开始</h4>
          <p>例如："我这个月怎么把娱乐支出减少 20%？"</p>
          <div class="quick-actions">
            <n-button v-for="suggestion in suggestions" :key="suggestion" round size="small" @click="handleQuickAction(suggestion)">
              {{ suggestion }}
            </n-button>
          </div>
        </div>

        <div v-for="(msg, index) in chatStore.messages" :key="index">
          <ToolCallCard
            v-if="msg.messageType === 'tool_call' || msg.messageType === 'tool_result'"
            :message="msg"
          />
          <ConfirmationDialog
            v-else-if="msg.messageType === 'confirmation'"
            :message="msg"
            @confirm="handleConfirmation(msg, $event)"
          />
          <div
            v-else
            :class="['message-item', msg.role === 'user' ? 'message-user' : 'message-assistant']"
          >
            <div class="message-bubble">
              <div class="message-content" v-html="renderMarkdown(msg.content)" />
              <div v-if="msg.isStreaming" class="typing-indicator">
                <span /><span /><span />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-input">
        <n-input
          v-model:value="inputMessage"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 5 }"
          placeholder="输入你的财务问题..."
          :disabled="chatStore.isStreaming"
          @keydown.enter.exact.prevent="handleSend"
        />

        <div class="chat-input__controls">
          <div v-if="isAgentMode" class="risk-setting">
            <span>风险阈值</span>
            <n-input-number
              v-model:value="riskThresholdInput"
              size="small"
              :min="0"
              :max="1000000"
              :step="100"
              @update:value="handleRiskThresholdChange"
            />
          </div>

          <n-button
            type="primary"
            :disabled="!inputMessage.trim() || chatStore.isStreaming"
            :loading="chatStore.isStreaming"
            @click="handleSend"
          >
            发送
          </n-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from "vue";
import {
  NButton,
  NTag,
  NSelect,
  NInput,
  NInputNumber,
  NSwitch,
} from "naive-ui";
import { useChatStore } from "@/stores/chat";
import ToolCallCard from "@/components/ai/ToolCallCard.vue";
import ConfirmationDialog from "@/components/ai/ConfirmationDialog.vue";
import PageHeader from "@/components/common/PageHeader.vue";

const chatStore = useChatStore();
const inputMessage = ref("");
const messageListRef = ref<HTMLDivElement | null>(null);
const selectedProvider = ref("");
const riskThresholdInput = ref<number | null>(null);

const isAgentMode = computed({
  get: () => chatStore.isAgentMode,
  set: (val) => {
    chatStore.isAgentMode = val;
  },
});

const providerOptions = computed(() =>
  chatStore.providers.map((p) => ({ label: p, value: p })),
);

const suggestions = [
  "分析我本月的消费",
  "如何提高储蓄率",
  "帮我做一个预算计划",
  "我的财务健康如何？",
];

onMounted(async () => {
  await chatStore.fetchRemainingChats();
  await chatStore.fetchProviders();
  selectedProvider.value = chatStore.currentProvider;
  await chatStore.fetchAgentRiskThreshold();
  riskThresholdInput.value = chatStore.agentRiskThreshold;
});

watch(
  () => chatStore.messages.length,
  () => nextTick(scrollToBottom),
);

watch(
  () => {
    const msgs = chatStore.messages;
    if (!msgs.length) return "";
    return msgs[msgs.length - 1].content;
  },
  () => nextTick(scrollToBottom),
);

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
  }
}

async function handleSend() {
  const msg = inputMessage.value.trim();
  if (!msg || chatStore.isStreaming) return;

  inputMessage.value = "";
  if (isAgentMode.value) {
    await chatStore.sendAgentMessage(msg);
  } else {
    await chatStore.sendMessage(msg);
  }
  await chatStore.fetchRemainingChats();
}

function handleQuickAction(text: string) {
  inputMessage.value = text;
  handleSend();
}

function handleNewSession() {
  chatStore.startNewSession();
}

async function handleProviderChange(name: string) {
  await chatStore.switchProviderAction(name);
}

function handleConfirmation(msg: any, accepted: boolean) {
  if (!msg.confirmationId) return;
  chatStore.respondToConfirmation(msg.confirmationId, accepted);
}

function handleRiskThresholdChange(value: number | null) {
  if (value == null) return;
  chatStore.updateAgentRiskThreshold(value);
}

function renderMarkdown(content: string): string {
  if (!content) return "";
  return content
    .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
    .replace(/`([^`]+)`/g, "<code>$1</code>")
    .replace(/\n/g, "<br>");
}
</script>

<style scoped lang="scss">
.chat-shell {
  border: 1px solid var(--cr-border-light);
  border-radius: var(--cr-radius-lg);
  background: var(--cr-bg-card);
  box-shadow: var(--cr-shadow-sm);
  overflow: hidden;
}

.mode-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--cr-text-secondary);
  font-size: 12px;
}

.message-list {
  min-height: 480px;
  max-height: calc(100vh - 330px);
  overflow-y: auto;
  padding: var(--cr-space-xl);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.empty-state {
  margin: auto;
  text-align: center;
  color: var(--cr-text-secondary);

  h4 {
    margin-bottom: 8px;
    color: var(--cr-text-primary);
  }

  p {
    margin-bottom: 16px;
  }
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.message-item {
  display: flex;

  &.message-user {
    justify-content: flex-end;

    .message-bubble {
      background: var(--cr-primary);
      color: #fff;
      border-color: transparent;

      :deep(code) {
        background: rgba(255, 255, 255, 0.2);
        color: #fff;
      }
    }
  }

  &.message-assistant {
    justify-content: flex-start;

    .message-bubble {
      background: var(--cr-bg-elevated);
      border-color: var(--cr-border-light);
      color: var(--cr-text-primary);
    }
  }
}

.message-bubble {
  max-width: min(78%, 740px);
  border: 1px solid;
  border-radius: 14px;
  padding: 10px 14px;
  line-height: 1.55;

  .message-content {
    word-break: break-word;

    :deep(code) {
      background: var(--cr-bg-input);
      border-radius: 5px;
      padding: 1px 4px;
      font-size: 12px;
    }
  }
}

.typing-indicator {
  display: inline-flex;
  gap: 4px;
  padding-top: 4px;

  span {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--cr-text-tertiary);
    animation: typing 1.2s infinite ease-in-out;

    &:nth-child(2) {
      animation-delay: 0.15s;
    }

    &:nth-child(3) {
      animation-delay: 0.3s;
    }
  }
}

@keyframes typing {
  0%,
  60%,
  100% {
    transform: translateY(0);
  }

  30% {
    transform: translateY(-3px);
  }
}

.chat-input {
  border-top: 1px solid var(--cr-divider);
  padding: var(--cr-space-md) var(--cr-space-xl) var(--cr-space-xl);

  &__controls {
    margin-top: 8px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: var(--cr-space-sm);
  }
}

.risk-setting {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--cr-text-secondary);
  font-size: 12px;

  :deep(.n-input-number) {
    width: 140px;
  }
}

@media (max-width: 900px) {
  .message-bubble {
    max-width: 92%;
  }

  .chat-input__controls {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
