<template>
  <div :class="['page-header', { 'page-header--dense': dense }]">
    <div class="page-header__left">
      <n-button v-if="showBack" quaternary circle size="small" @click="$router.back()">
        <template #icon>
          <n-icon><ArrowBack /></n-icon>
        </template>
      </n-button>

      <div>
        <h2 class="page-header__title">{{ title }}</h2>
        <p v-if="subtitle" class="page-header__subtitle">{{ subtitle }}</p>
      </div>
    </div>

    <div v-if="$slots.actions" :class="['page-header__actions', `page-header__actions--${actionsAlign}`]">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { NButton, NIcon } from "naive-ui";
import { ArrowBack } from "@vicons/ionicons5";

withDefaults(
  defineProps<{
    title: string;
    subtitle?: string;
    showBack?: boolean;
    dense?: boolean;
    actionsAlign?: "start" | "end";
  }>(),
  {
    showBack: false,
    dense: false,
    actionsAlign: "end",
  },
);
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--cr-space-lg);
  margin-bottom: var(--cr-space-xl);

  &--dense {
    margin-bottom: var(--cr-space-lg);
  }

  &__left {
    display: flex;
    align-items: center;
    gap: var(--cr-space-sm);
  }

  &__title {
    font-size: 26px;
    line-height: 1.2;
    font-weight: 700;
    color: var(--cr-text-primary);
  }

  &__subtitle {
    margin-top: 6px;
    font-size: 14px;
    color: var(--cr-text-secondary);
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: var(--cr-space-sm);
    flex-wrap: wrap;

    &--start {
      justify-content: flex-start;
    }

    &--end {
      justify-content: flex-end;
    }
  }
}

@media (max-width: 900px) {
  .page-header {
    flex-direction: column;
  }
}
</style>
