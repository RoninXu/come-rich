<template>
  <div :class="['metric-card', { 'metric-card--clickable': clickable }]" @click="$emit('click')">
    <div class="metric-card__label-wrap">
      <span class="metric-card__label">{{ label }}</span>
      <span v-if="delta !== undefined" :class="['metric-card__delta', deltaClass]">
        {{ deltaText }}
      </span>
    </div>

    <div class="metric-card__value-wrap">
      <div class="metric-card__value tabular-nums">{{ value }}</div>
      <div v-if="$slots.icon" class="metric-card__icon">
        <slot name="icon" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    label: string;
    value: string | number;
    delta?: number;
    tone?: "neutral" | "positive" | "negative";
    clickable?: boolean;
  }>(),
  {
    tone: "neutral",
    clickable: false,
  },
);

defineEmits<{ click: [] }>();

const deltaClass = computed(() => {
  if (props.delta == null) return "";
  if (props.delta > 0) return "metric-card__delta--up";
  if (props.delta < 0) return "metric-card__delta--down";
  return "";
});

const deltaText = computed(() => {
  if (props.delta == null) return "";
  const sign = props.delta > 0 ? "+" : "";
  return `${sign}${props.delta.toFixed(1)}%`;
});
</script>

<style scoped lang="scss">
.metric-card {
  border: 1px solid var(--cr-border-light);
  border-radius: var(--cr-radius-lg);
  background: var(--cr-bg-card);
  box-shadow: var(--cr-shadow-sm);
  padding: var(--cr-space-lg);
  min-height: 120px;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    border-color 0.18s ease;

  &--clickable {
    cursor: pointer;

    &:hover {
      transform: translateY(-2px);
      box-shadow: var(--cr-shadow-md);
      border-color: color-mix(in srgb, var(--cr-primary) 26%, var(--cr-border-light));
    }
  }

  &__label-wrap {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: var(--cr-space-sm);
    margin-bottom: var(--cr-space-xl);
  }

  &__label {
    font-size: 13px;
    color: var(--cr-text-secondary);
  }

  &__delta {
    font-size: 12px;
    font-weight: 600;
    color: var(--cr-text-tertiary);

    &--up {
      color: var(--cr-success);
    }

    &--down {
      color: var(--cr-error);
    }
  }

  &__value-wrap {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--cr-space-md);
  }

  &__value {
    font-size: 28px;
    font-weight: 700;
    line-height: 1;
    color: var(--cr-text-primary);
  }

  &__icon {
    color: var(--cr-primary);
    font-size: 22px;
  }
}
</style>
