<template>
  <div class="section-header">
    <div>
      <h3 class="section-header__title">{{ title }}</h3>
      <p v-if="description" class="section-header__desc">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="section-header__actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string;
  description?: string;
}>();
</script>

<style scoped lang="scss">
.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--cr-space-md);
  margin-bottom: var(--cr-space-md);

  &__title {
    font-size: 17px;
    font-weight: 650;
    color: var(--cr-text-primary);
  }

  &__desc {
    margin-top: 3px;
    font-size: 13px;
    color: var(--cr-text-secondary);
  }

  &__actions {
    display: flex;
    gap: var(--cr-space-sm);
    align-items: center;
  }
}
</style>
