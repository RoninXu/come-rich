<template>
  <n-card
    v-bind="$attrs"
    :class="['surface-card', `surface-card--${variant}`, { 'surface-card--hoverable': hoverable }]"
  >
    <template v-if="$slots.header" #header>
      <slot name="header" />
    </template>
    <template v-if="$slots['header-extra']" #header-extra>
      <slot name="header-extra" />
    </template>
    <slot />
    <template v-if="$slots.action" #action>
      <slot name="action" />
    </template>
  </n-card>
</template>

<script setup lang="ts">
import { NCard } from "naive-ui";

withDefaults(
  defineProps<{
    hoverable?: boolean;
    variant?: "default" | "soft" | "compact";
  }>(),
  {
    variant: "default",
  },
);
</script>

<style scoped lang="scss">
.surface-card {
  border: 1px solid var(--cr-border-light) !important;
  border-radius: var(--cr-radius-lg) !important;
  background: var(--cr-bg-card) !important;
  box-shadow: var(--cr-shadow-sm);
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    border-color 0.18s ease;

  &--soft {
    background: color-mix(in srgb, var(--cr-bg-card) 88%, var(--cr-primary) 12%) !important;
  }

  &--compact {
    :deep(.n-card__content) {
      padding-top: 14px;
      padding-bottom: 14px;
    }
  }

  &--hoverable:hover {
    transform: translateY(-2px);
    box-shadow: var(--cr-shadow-md);
    border-color: color-mix(in srgb, var(--cr-primary) 28%, var(--cr-border-light));
  }
}
</style>
