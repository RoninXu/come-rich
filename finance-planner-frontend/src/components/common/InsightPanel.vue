<template>
  <div class="insight-panel">
    <h4 class="insight-panel__title">{{ title }}</h4>
    <ul v-if="items.length" class="insight-panel__list">
      <li v-for="(item, index) in items" :key="index">
        {{ item }}
      </li>
    </ul>
    <p v-else class="insight-panel__empty">暂无可展示建议</p>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string;
    items: string[];
  }>(),
  {
    items: () => [],
  },
);
</script>

<style scoped lang="scss">
.insight-panel {
  border: 1px solid color-mix(in srgb, var(--cr-primary) 18%, var(--cr-border-light));
  background: color-mix(in srgb, var(--cr-primary) 6%, var(--cr-bg-card));
  border-radius: var(--cr-radius-md);
  padding: var(--cr-space-md);

  &__title {
    font-size: 14px;
    font-weight: 600;
    color: var(--cr-text-primary);
    margin-bottom: var(--cr-space-sm);
  }

  &__list {
    padding-left: 18px;
    list-style: disc;
    display: grid;
    gap: 8px;
    color: var(--cr-text-secondary);
    font-size: 13px;
  }

  &__empty {
    color: var(--cr-text-tertiary);
    font-size: 13px;
  }
}
</style>
