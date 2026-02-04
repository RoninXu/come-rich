<template>
  <div class="confirm-card">
    <div class="confirm-header">
      <div class="confirm-title">
        <span>需要确认</span>
        <n-tag
          size="small"
          :type="riskType"
        >
          {{ riskLabel }}
        </n-tag>
      </div>
      <span
        v-if="message.toolName"
        class="confirm-tool"
      >{{
        message.toolName
      }}</span>
    </div>
    <div class="confirm-body">
      该操作属于高风险操作，确认后会立即执行。
    </div>
    <div
      v-if="message.confirmationStatus === 'required'"
      class="confirm-actions"
    >
      <n-button
        size="small"
        @click="emitConfirm(false)"
      >
        拒绝
      </n-button>
      <n-button
        size="small"
        type="primary"
        @click="emitConfirm(true)"
      >
        确认执行
      </n-button>
    </div>
    <div
      v-else
      class="confirm-result"
    >
      {{ message.confirmationAccepted ? "已确认执行" : "已拒绝执行" }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { NButton, NTag } from "naive-ui";
import type { ChatMessage } from "@/types/ai";

const props = defineProps<{
  message: ChatMessage;
}>();

const emit = defineEmits<{
  (e: "confirm", accepted: boolean): void;
}>();

const riskLabel = computed(() => props.message.riskLevel || "HIGH");
const riskType = computed(() =>
  props.message.riskLevel === "HIGH" ? "error" : "warning",
);

function emitConfirm(accepted: boolean) {
  emit("confirm", accepted);
}
</script>

<style scoped lang="scss">
.confirm-card {
  background: rgba(255, 231, 230, 0.5);
  border: 1px solid rgba(239, 68, 68, 0.35);
  border-radius: var(--cr-radius-lg);
  padding: 12px 14px;
  margin: 10px 0 14px;
}

.confirm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.confirm-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.confirm-tool {
  color: var(--cr-text-tertiary);
  font-size: 12px;
}

.confirm-body {
  color: var(--cr-text-secondary);
  margin-bottom: 10px;
}

.confirm-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.confirm-result {
  font-size: 12px;
  color: var(--cr-text-tertiary);
  text-align: right;
}
</style>
