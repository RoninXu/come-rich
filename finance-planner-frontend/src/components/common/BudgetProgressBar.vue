<template>
  <div class="budget-progress">
    <n-progress
      type="line"
      :percentage="progress"
      :height="10"
      :border-radius="999"
      :color="resolvedColor"
      :indicator-placement="showLabel ? 'inside' : 'outside'"
    />
    <div v-if="showLabel" class="budget-progress__label tabular-nums">
      <span>{{ progress.toFixed(1) }}%</span>
      <span>{{ spentText }} / {{ budgetText }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { NProgress } from "naive-ui";

const props = withDefaults(
  defineProps<{
    budget: number;
    spent: number;
    showLabel?: boolean;
    thresholds?: [number, number];
  }>(),
  {
    showLabel: true,
    thresholds: () => [80, 100],
  },
);

const progress = computed(() => {
  if (!props.budget || props.budget <= 0) return 0;
  return Math.min((props.spent / props.budget) * 100, 100);
});

const resolvedColor = computed(() => {
  const [warn, danger] = props.thresholds;
  if (progress.value >= danger) return "var(--cr-error)";
  if (progress.value >= warn) return "var(--cr-warning)";
  return "var(--cr-success)";
});

const formatter = new Intl.NumberFormat("zh-CN", {
  style: "currency",
  currency: "CNY",
  minimumFractionDigits: 0,
  maximumFractionDigits: 0,
});

const spentText = computed(() => formatter.format(props.spent || 0));
const budgetText = computed(() => formatter.format(props.budget || 0));
</script>

<style scoped lang="scss">
.budget-progress {
  &__label {
    margin-top: 8px;
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: var(--cr-text-secondary);
  }
}
</style>
