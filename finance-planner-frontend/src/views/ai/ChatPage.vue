<template>
  <div class="chat-page">
    <!-- Top Bar -->
    <div class="chat-header">
      <div class="header-left">
        <h3>AI 财务顾问</h3>
        <el-tag type="info" size="small" class="remaining-tag">
          今日剩余 {{ chatStore.remainingChats }} 次
        </el-tag>
      </div>
      <div class="header-right">
        <el-select
          v-model="selectedProvider"
          size="small"
          placeholder="选择模型"
          class="provider-select"
          @change="handleProviderChange"
        >
          <el-option
            v-for="p in chatStore.providers"
            :key="p"
            :label="p"
            :value="p"
          />
        </el-select>
        <el-button size="small" @click="handleNewSession">新对话</el-button>
      </div>
    </div>

    <!-- Message List -->
    <div ref="messageListRef" class="message-list">
      <!-- Empty state -->
      <div v-if="chatStore.messages.length === 0" class="empty-state">
        <div class="welcome-icon">
          <el-icon :size="48"><ChatDotRound /></el-icon>
        </div>
        <h4>你好！我是你的 AI 财务顾问</h4>
        <p>我可以帮你分析消费习惯、制定预算计划、评估财务健康。试试下面的问题：</p>
        <div class="quick-actions">
          <el-button
            v-for="suggestion in suggestions"
            :key="suggestion"
            round
            size="small"
            @click="handleQuickAction(suggestion)"
          >
            {{ suggestion }}
          </el-button>
        </div>
      </div>

      <!-- Messages -->
      <div
        v-for="(msg, index) in chatStore.messages"
        :key="index"
        :class="['message-item', msg.role === 'user' ? 'message-user' : 'message-assistant']"
      >
        <div class="message-avatar">
          <el-avatar
            :size="32"
            :icon="msg.role === 'user' ? 'User' : 'Monitor'"
            :style="{
              backgroundColor: msg.role === 'user' ? '#409EFF' : '#67C23A'
            }"
          />
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
      <el-input
        v-model="inputMessage"
        type="textarea"
        :rows="2"
        placeholder="输入你的财务问题..."
        :disabled="chatStore.isStreaming"
        resize="none"
        @keydown.enter.exact.prevent="handleSend"
      />
      <el-button
        type="primary"
        :disabled="!inputMessage.trim() || chatStore.isStreaming"
        :loading="chatStore.isStreaming"
        @click="handleSend"
      >
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { ChatDotRound } from '@element-plus/icons-vue'
import { useChatStore } from '@/stores/chat'

const chatStore = useChatStore()
const inputMessage = ref('')
const messageListRef = ref<HTMLDivElement | null>(null)
const selectedProvider = ref('')

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
    nextTick(() => {
      scrollToBottom()
    })
  }
)

// Also watch the last message content for streaming updates
watch(
  () => {
    const msgs = chatStore.messages
    if (msgs.length === 0) return ''
    return msgs[msgs.length - 1].content
  },
  () => {
    nextTick(() => {
      scrollToBottom()
    })
  }
)

function scrollToBottom() {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

async function handleSend() {
  const message = inputMessage.value.trim()
  if (!message || chatStore.isStreaming) return

  inputMessage.value = ''
  await chatStore.sendMessage(message)
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
    // Bold: **text**
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    // Inline code: `code`
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    // Line breaks
    .replace(/\n/g, '<br>')
}
</script>

<style scoped lang="scss">
.chat-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 100px);
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid #ebeef5;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    h3 {
      margin: 0;
      font-size: 16px;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 8px;

    .provider-select {
      width: 140px;
    }
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
  color: #909399;

  .welcome-icon {
    margin-bottom: 16px;
    color: #409EFF;
  }

  h4 {
    margin: 0 0 8px;
    color: #303133;
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
      background: #ecf5ff;
      border-color: #d9ecff;
    }
  }

  &.message-assistant {
    .message-bubble {
      background: #f4f4f5;
      border-color: #e9e9eb;
    }
  }
}

.message-avatar {
  flex-shrink: 0;
}

.message-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid;
  line-height: 1.6;

  .message-content {
    word-break: break-word;

    :deep(code) {
      background: #e8e8e8;
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
    background: #909399;
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
  border-top: 1px solid #ebeef5;
  align-items: flex-end;

  .el-textarea {
    flex: 1;
  }

  .el-button {
    height: 54px;
  }
}
</style>
