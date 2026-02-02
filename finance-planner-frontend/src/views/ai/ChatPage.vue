<template>
  <div class="chat-page">
    <!-- Top Bar -->
    <div class="chat-header">
      <div class="header-left">
        <h3>AI 财务顾问</h3>
        <n-tag type="info" size="small">
          今日剩余 {{ chatStore.remainingChats }} 次
        </n-tag>
      </div>
      <div class="header-right">
        <n-select
          v-model:value="selectedProvider"
          size="small"
          placeholder="选择模型"
          :options="providerOptions"
          style="width: 140px"
          @update:value="handleProviderChange"
        />
        <n-button size="small" @click="handleNewSession">新对话</n-button>
      </div>
    </div>

    <!-- Message List -->
    <div ref="messageListRef" class="message-list">
      <!-- Empty state -->
      <div v-if="chatStore.messages.length === 0" class="empty-state">
        <div class="welcome-icon">
          <n-icon :size="48"><Chatbubbles /></n-icon>
        </div>
        <h4>你好！我是你的 AI 财务顾问</h4>
        <p>我可以帮你分析消费习惯、制定预算计划、评估财务健康。试试下面的问题：</p>
        <div class="quick-actions">
          <n-button
            v-for="suggestion in suggestions"
            :key="suggestion"
            round
            size="small"
            @click="handleQuickAction(suggestion)"
          >
            {{ suggestion }}
          </n-button>
        </div>
      </div>

      <!-- Messages -->
      <div
        v-for="(msg, index) in chatStore.messages"
        :key="index"
        :class="['message-item', msg.role === 'user' ? 'message-user' : 'message-assistant']"
      >
        <div class="message-avatar">
          <n-avatar
            :size="32"
            round
            :style="{
              backgroundColor: msg.role === 'user' ? 'var(--cr-primary)' : 'var(--cr-success)',
              fontSize: '14px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }"
          >
            {{ msg.role === 'user' ? '我' : 'AI' }}
          </n-avatar>
        </div>
        <div class="message-bubble">
          <div class="message-content" v-html="renderMarkdown(msg.content)"></div>
          <div v-if="msg.isStreaming" class="typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>

    <!-- Input Area -->
    <div class="chat-input">
      <n-input
        v-model:value="inputMessage"
        type="textarea"
        :autosize="{ minRows: 2, maxRows: 4 }"
        placeholder="输入你的财务问题..."
        :disabled="chatStore.isStreaming"
        @keydown.enter.exact.prevent="handleSend"
      />
      <n-button
        type="primary"
        :disabled="!inputMessage.trim() || chatStore.isStreaming"
        :loading="chatStore.isStreaming"
        @click="handleSend"
        class="send-btn"
      >
        发送
      </n-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { NButton, NTag, NSelect, NInput, NAvatar, NIcon, useMessage } from 'naive-ui'
import { Chatbubbles } from '@vicons/ionicons5'
import { useChatStore } from '@/stores/chat'

const chatStore = useChatStore()
const message = useMessage()
const inputMessage = ref('')
const messageListRef = ref<HTMLDivElement | null>(null)
const selectedProvider = ref('')

const providerOptions = computed(() =>
  chatStore.providers.map(p => ({ label: p, value: p }))
)

const suggestions = [
  '分析我本月的消费',
  '如何提高储蓄率?',
  '帮我做一个预算计划',
  '我的财务健康如何?'
]

onMounted(async () => {
  await chatStore.fetchRemainingChats()
  await chatStore.fetchProviders()
  selectedProvider.value = chatStore.currentProvider
})

watch(
  () => chatStore.messages.length,
  () => {
    nextTick(() => { scrollToBottom() })
  }
)

watch(
  () => {
    const msgs = chatStore.messages
    if (msgs.length === 0) return ''
    return msgs[msgs.length - 1].content
  },
  () => {
    nextTick(() => { scrollToBottom() })
  }
)

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

async function handleSend() {
  const msg = inputMessage.value.trim()
  if (!msg || chatStore.isStreaming) return

  inputMessage.value = ''
  await chatStore.sendMessage(msg)
  await chatStore.fetchRemainingChats()
}

function handleQuickAction(text: string) {
  inputMessage.value = text
  handleSend()
}

function handleNewSession() {
  chatStore.startNewSession()
}

async function handleProviderChange(name: string) {
  await chatStore.switchProviderAction(name)
}

function renderMarkdown(content: string): string {
  if (!content) return ''
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br>')
}
</script>

<style scoped lang="scss">
.chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px);
  background: var(--cr-bg-card);
  backdrop-filter: blur(var(--cr-blur-md));
  border: 1px solid var(--cr-border-light);
  border-radius: var(--cr-radius-xl);
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid var(--cr-divider);

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    h3 {
      margin: 0;
      font-size: 16px;
      color: var(--cr-text-primary);
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--cr-text-tertiary);

  .welcome-icon {
    margin-bottom: 16px;
    color: var(--cr-primary);
  }

  h4 {
    margin: 0 0 8px;
    color: var(--cr-text-primary);
  }

  p {
    margin: 0 0 20px;
    text-align: center;
    max-width: 400px;
  }

  .quick-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    justify-content: center;
    max-width: 500px;
  }
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  gap: 8px;

  &.message-user {
    flex-direction: row-reverse;

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
    .message-bubble {
      background: var(--cr-bg-elevated, var(--cr-bg-card));
      border-color: var(--cr-border-light);
    }
  }
}

.message-avatar {
  flex-shrink: 0;
}

.message-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: var(--cr-radius-lg);
  border: 1px solid;
  line-height: 1.6;

  .message-content {
    word-break: break-word;

    :deep(code) {
      background: var(--cr-bg-page);
      padding: 1px 4px;
      border-radius: 3px;
      font-size: 13px;
    }

    :deep(strong) {
      font-weight: 600;
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
    background: var(--cr-text-tertiary);
    border-radius: 50%;
    animation: typing 1.4s infinite ease-in-out;

    &:nth-child(1) { animation-delay: 0s; }
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-4px); }
}

.chat-input {
  display: flex;
  gap: 8px;
  padding: 12px 20px;
  border-top: 1px solid var(--cr-divider);
  align-items: flex-end;

  :deep(.n-input) {
    flex: 1;
  }

  .send-btn {
    height: 54px;
  }
}
</style>
