<template>
  <MetricCard
    :label="label"
    :value="value"
    :delta="change"
    :clickable="clickable"
    :tone="tone"
    @click="$emit('click')"
  >
    <template #icon>
      <slot name="icon" />
    </template>
  </MetricCard>
</template>

<script setup lang="ts">
import { computed } from "vue";
import MetricCard from "@/components/common/MetricCard.vue";

const props = defineProps<{
  label: string;
  value: string | number;
  change?: number;
  iconBg?: string;
  iconColor?: string;
  clickable?: boolean;
}>();

defineEmits<{ click: [] }>();

const tone = computed(() => {
  if (props.change == null) return "neutral";
  return props.change >= 0 ? "positive" : "negative";
});
</script>
