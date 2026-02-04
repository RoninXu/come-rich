<template>
  <div class="page-header">
    <div class="page-header__left">
      <n-button
        v-if="showBack"
        quaternary
        circle
        size="small"
        @click="$router.back()"
      >
        <template #icon>
          <n-icon><ArrowBack /></n-icon>
        </template>
      </n-button>
      <h2 class="page-header__title">
        {{ title }}
      </h2>
      <span
        v-if="subtitle"
        class="page-header__subtitle"
      >{{ subtitle }}</span>
    </div>
    <div
      v-if="$slots.actions"
      class="page-header__actions"
    >
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { NButton, NIcon } from "naive-ui";
import { ArrowBack } from "@vicons/ionicons5";

defineProps<{
  title: string;
  subtitle?: string;
  showBack?: boolean;
}>();
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--cr-space-xxl);

  &__left {
    display: flex;
    align-items: center;
    gap: var(--cr-space-sm);
  }

  &__title {
    font-size: 22px;
    font-weight: 700;
    color: var(--cr-text-primary);
    letter-spacing: -0.02em;
  }

  &__subtitle {
    font-size: 14px;
    color: var(--cr-text-secondary);
    margin-left: var(--cr-space-sm);
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: var(--cr-space-sm);
  }
}
</style>
