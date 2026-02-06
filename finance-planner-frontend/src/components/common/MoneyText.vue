<template>
  <span class="money-text tabular-nums">
    {{ formatted }}
  </span>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    value: number | string | null | undefined;
    currency?: string;
    minimumFractionDigits?: number;
  }>(),
  {
    currency: "CNY",
    minimumFractionDigits: 2,
  },
);

const formatted = computed(() => {
  const numeric = Number(props.value ?? 0);
  return new Intl.NumberFormat("zh-CN", {
    style: "currency",
    currency: props.currency,
    minimumFractionDigits: props.minimumFractionDigits,
    maximumFractionDigits: props.minimumFractionDigits,
  }).format(Number.isFinite(numeric) ? numeric : 0);
});
</script>

<style scoped>
.money-text {
  font-variant-numeric: tabular-nums;
}
</style>
