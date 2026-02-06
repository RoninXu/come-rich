<template>
  <div class="empty-state">
    <n-icon :size="32" class="empty-state__icon">
      <DocumentTextOutline />
    </n-icon>
    <p class="empty-state__title">{{ title }}</p>
    <p v-if="description" class="empty-state__desc">{{ description }}</p>
  </div>
</template>

<script setup lang="ts">
import { NIcon } from "naive-ui";
import { DocumentTextOutline } from "@vicons/ionicons5";

withDefaults(
  defineProps<{
    title?: string;
    description?: string;
  }>(),
  {
    title: "暂无数据",
    description: "",
  },
);
</script>

<style scoped lang="scss">
.empty-state {
  padding: var(--cr-space-xxl);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;

  &__icon {
    color: var(--cr-text-tertiary);
    margin-bottom: var(--cr-space-sm);
  }

  &__title {
    font-size: 14px;
    color: var(--cr-text-secondary);
  }

  &__desc {
    margin-top: 4px;
    font-size: 13px;
    color: var(--cr-text-tertiary);
  }
}
</style>
