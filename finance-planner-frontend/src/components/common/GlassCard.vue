<template>
  <n-card
    v-bind="$attrs"
    :class="['glass-card', { 'glass-card--hoverable': hoverable }]"
  >
    <template
      v-if="$slots.header"
      #header
    >
      <slot name="header" />
    </template>
    <template
      v-if="$slots['header-extra']"
      #header-extra
    >
      <slot name="header-extra" />
    </template>
    <slot />
    <template
      v-if="$slots.action"
      #action
    >
      <slot name="action" />
    </template>
  </n-card>
</template>

<script setup lang="ts">
import { NCard } from "naive-ui";

defineProps<{
  hoverable?: boolean;
}>();
</script>

<style scoped lang="scss">
.glass-card {
  background: var(--cr-bg-card) !important;
  backdrop-filter: blur(var(--cr-blur-md));
  -webkit-backdrop-filter: blur(var(--cr-blur-md));
  border: 1px solid var(--cr-border-light) !important;
  border-radius: var(--cr-radius-xl) !important;
  box-shadow: var(--cr-shadow-md);
  transition:
    transform 0.2s cubic-bezier(0.25, 0.46, 0.45, 0.94),
    box-shadow 0.2s cubic-bezier(0.25, 0.46, 0.45, 0.94),
    background 0.3s ease;

  &--hoverable:hover {
    transform: translateY(-2px);
    box-shadow: var(--cr-shadow-lg);
  }
}
</style>
