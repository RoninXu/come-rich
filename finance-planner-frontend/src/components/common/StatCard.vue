<template>
  <div
    :class="['stat-card', { 'stat-card--clickable': clickable }]"
    @click="$emit('click')"
  >
    <div
      class="stat-card__icon"
      :style="{ background: iconBg }"
    >
      <n-icon
        :size="22"
        :color="iconColor"
      >
        <slot name="icon" />
      </n-icon>
    </div>
    <div class="stat-card__content">
      <span class="stat-card__label">{{ label }}</span>
      <span class="stat-card__value">{{ value }}</span>
      <span
        v-if="change !== undefined"
        :class="['stat-card__change', changeClass]"
      >
        {{ changeText }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { NIcon } from "naive-ui";

const props = defineProps<{
  label: string;
  value: string | number;
  change?: number;
  iconBg?: string;
  iconColor?: string;
  clickable?: boolean;
}>();

defineEmits<{
  click: [];
}>();

const changeClass = computed(() => {
  if (props.change === undefined) return "";
  return props.change >= 0
    ? "stat-card__change--up"
    : "stat-card__change--down";
});

const changeText = computed(() => {
  if (props.change === undefined) return "";
  const sign = props.change >= 0 ? "+" : "";
  return `${sign}${props.change}%`;
});
</script>

<style scoped lang="scss">
.stat-card {
  display: flex;
  align-items: center;
  gap: var(--cr-space-lg);
  padding: var(--cr-space-xl);
  background: var(--cr-bg-card);
  backdrop-filter: blur(var(--cr-blur-md));
  -webkit-backdrop-filter: blur(var(--cr-blur-md));
  border: 1px solid var(--cr-border-light);
  border-radius: var(--cr-radius-xl);
  box-shadow: var(--cr-shadow-sm);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;

  &--clickable {
    cursor: pointer;

    &:hover {
      transform: translateY(-2px);
      box-shadow: var(--cr-shadow-md);
    }
  }

  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 44px;
    height: 44px;
    border-radius: var(--cr-radius-lg);
    flex-shrink: 0;
  }

  &__content {
    display: flex;
    flex-direction: column;
    min-width: 0;
  }

  &__label {
    font-size: 13px;
    color: var(--cr-text-secondary);
    margin-bottom: 2px;
  }

  &__value {
    font-size: 20px;
    font-weight: 600;
    color: var(--cr-text-primary);
    line-height: 1.3;
  }

  &__change {
    font-size: 12px;
    font-weight: 500;
    margin-top: 2px;

    &--up {
      color: var(--cr-success);
    }

    &--down {
      color: var(--cr-error);
    }
  }
}
</style>
