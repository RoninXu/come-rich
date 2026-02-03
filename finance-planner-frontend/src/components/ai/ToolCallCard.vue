<template>
  <div
    class="tool-card"
    :class="statusClass"
  >
    <div class="tool-header">
      <div class="tool-title">
        <span class="tool-name">{{ message.toolName }}</span>
        <n-tag
          size="small"
          :type="tagType"
        >
          {{ statusText }}
        </n-tag>
      </div>
      <span
        v-if="message.toolCallId"
        class="tool-id"
      >#{{ message.toolCallId }}</span>
    </div>
    <div
      v-if="message.toolArguments"
      class="tool-section"
    >
      <div class="tool-label">
        参数
      </div>
      <pre class="tool-body">{{ prettyJson(message.toolArguments) }}</pre>
    </div>
    <div
      v-if="message.toolResult"
      class="tool-section"
    >
      <div class="tool-label">
        结果
      </div>
      <pre class="tool-body">{{ prettyJson(message.toolResult) }}</pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { NTag } from "naive-ui";
import type { ChatMessage } from "@/types/ai";

const props = defineProps<{
  message: ChatMessage;
}>();

const statusText = computed(() => {
  if (props.message.toolStatus === "running") return "执行中";
  if (props.message.toolStatus === "error") return "失败";
  return "完成";
});

const tagType = computed(() => {
  if (props.message.toolStatus === "running") return "info";
  if (props.message.toolStatus === "error") return "error";
  return "success";
});

const statusClass = computed(() => props.message.toolStatus || "done");

function prettyJson(input?: string) {
  if (!input) return "";
  try {
    const parsed = JSON.parse(input);
    return JSON.stringify(parsed, null, 2);
  } catch {
    return input;
  }
}
</script>

<style scoped lang="scss">
.tool-card {
  background: var(--cr-bg-elevated, var(--cr-bg-card));
  border: 1px solid var(--cr-border-light);
  border-radius: var(--cr-radius-lg);
  padding: 12px 14px;
  margin: 10px 0 14px;

  &.running {
    border-color: rgba(34, 122, 255, 0.4);
  }

  &.error {
    border-color: rgba(239, 68, 68, 0.5);
  }
}

.tool-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.tool-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tool-name {
  font-weight: 600;
  color: var(--cr-text-primary);
}

.tool-id {
  color: var(--cr-text-tertiary);
  font-size: 12px;
}

.tool-section {
  margin-top: 8px;
}

.tool-label {
  font-size: 12px;
  color: var(--cr-text-tertiary);
  margin-bottom: 4px;
}

.tool-body {
  background: var(--cr-bg-page);
  border-radius: 8px;
  padding: 8px;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
}
</style>
