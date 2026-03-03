<template>
  <div class="agent-metrics-page">
    <PageHeader title="Agent Metrics" subtitle="Tool quality, latency, cache, and error trends" dense>
      <template #actions>
        <n-select
          v-model:value="rangeMode"
          size="small"
          :options="rangeOptions"
          style="width: 120px"
        />
        <n-date-picker
          v-if="rangeMode === 'custom'"
          v-model:value="customRange"
          type="daterange"
          clearable
          size="small"
          style="width: 280px"
        />
        <n-button size="small" type="primary" :loading="loading" @click="fetchMetrics">
          Refresh
        </n-button>
      </template>
    </PageHeader>

    <n-spin :show="loading">
      <n-grid :x-gap="16" :y-gap="16" :cols="4" class="metric-grid">
        <n-gi>
          <MetricCard label="Total Calls" :value="overview.totalCalls" />
        </n-gi>
        <n-gi>
          <MetricCard label="Success Rate" :value="`${overview.successRate.toFixed(2)}%`" />
        </n-gi>
        <n-gi>
          <MetricCard label="Avg Latency" :value="`${overview.averageLatencyMs.toFixed(2)} ms`" />
        </n-gi>
        <n-gi>
          <MetricCard label="Cache Hit Rate" :value="`${overview.cacheHitRate.toFixed(2)}%`" />
        </n-gi>
      </n-grid>

      <n-grid :x-gap="16" :y-gap="16" :cols="24">
        <n-gi :span="14">
          <GlassCard>
            <template #header>
              <SectionHeader title="Timeline" description="Calls, success rate, and latency over time" />
            </template>
            <div ref="timelineChartRef" class="chart-container" />
          </GlassCard>
        </n-gi>
        <n-gi :span="10">
          <GlassCard>
            <template #header>
              <SectionHeader title="Error Distribution" description="Top failure reasons" />
            </template>
            <div ref="errorChartRef" class="chart-container chart-container--small" />
          </GlassCard>
        </n-gi>
      </n-grid>

      <GlassCard class="tool-card">
        <template #header>
          <SectionHeader title="Tool Ranking" description="Top 10 by invocation count" />
        </template>
        <n-data-table :columns="toolColumns" :data="toolMetrics" :bordered="false" striped />
      </GlassCard>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import {
  NButton,
  NDataTable,
  NDatePicker,
  NGi,
  NGrid,
  NSelect,
  NSpin,
  useMessage,
  type DataTableColumns,
} from "naive-ui";
import * as echarts from "echarts";
import dayjs from "dayjs";
import {
  getAgentErrorMetrics,
  getAgentMetricsOverview,
  getAgentMetricsTimeline,
  getAgentToolMetrics,
} from "@/api/ai";
import type {
  AgentErrorMetric,
  AgentMetricsOverview,
  AgentMetricsTimelinePoint,
  AgentToolMetric,
} from "@/types/agent-metrics";
import GlassCard from "@/components/common/GlassCard.vue";
import MetricCard from "@/components/common/MetricCard.vue";
import PageHeader from "@/components/common/PageHeader.vue";
import SectionHeader from "@/components/common/SectionHeader.vue";

type RangeMode = "7d" | "30d" | "custom";

const message = useMessage();
const loading = ref(false);

const rangeMode = ref<RangeMode>("7d");
const customRange = ref<[number, number] | null>(null);

const overview = ref<AgentMetricsOverview>({
  totalCalls: 0,
  successfulCalls: 0,
  failedCalls: 0,
  totalSessions: 0,
  successRate: 0,
  averageLatencyMs: 0,
  cacheHitRate: 0,
});
const toolMetrics = ref<AgentToolMetric[]>([]);
const timeline = ref<AgentMetricsTimelinePoint[]>([]);
const errors = ref<AgentErrorMetric[]>([]);

const rangeOptions = [
  { label: "Last 7d", value: "7d" },
  { label: "Last 30d", value: "30d" },
  { label: "Custom", value: "custom" },
];

const timelineChartRef = ref<HTMLElement | null>(null);
const errorChartRef = ref<HTMLElement | null>(null);
let timelineChart: echarts.ECharts | null = null;
let errorChart: echarts.ECharts | null = null;

const toolColumns: DataTableColumns<AgentToolMetric> = [
  { title: "Tool", key: "toolName", width: 240 },
  { title: "Calls", key: "totalCalls", width: 120 },
  {
    title: "Success Rate",
    key: "successRate",
    width: 120,
    render: (row) => `${Number(row.successRate || 0).toFixed(2)}%`,
  },
  {
    title: "Avg Latency",
    key: "averageLatencyMs",
    width: 140,
    render: (row) => `${Number(row.averageLatencyMs || 0).toFixed(2)} ms`,
  },
  {
    title: "Cache Hit",
    key: "cacheHitRate",
    width: 140,
    render: (row) => `${Number(row.cacheHitRate || 0).toFixed(2)}%`,
  },
  { title: "Failures", key: "failedCalls", width: 120 },
];

onMounted(fetchMetrics);
onBeforeUnmount(() => {
  window.removeEventListener("resize", handleResize);
  timelineChart?.dispose();
  errorChart?.dispose();
});

async function fetchMetrics() {
  const params = buildQueryParams.value;
  if (rangeMode.value === "custom" && !params.startDate) {
    message.warning("Please select a full custom date range.");
    return;
  }

  loading.value = true;
  try {
    const [overviewRes, toolRes, timelineRes, errorRes] = await Promise.all([
      getAgentMetricsOverview(params),
      getAgentToolMetrics({ ...params, limit: 10 }),
      getAgentMetricsTimeline(params),
      getAgentErrorMetrics({ ...params, limit: 10 }),
    ]);

    if (overviewRes.data.code === 200 && overviewRes.data.data) {
      overview.value = overviewRes.data.data;
    }
    if (toolRes.data.code === 200) {
      toolMetrics.value = toolRes.data.data || [];
    }
    if (timelineRes.data.code === 200) {
      timeline.value = timelineRes.data.data || [];
    }
    if (errorRes.data.code === 200) {
      errors.value = errorRes.data.data || [];
    }

    await nextTick();
    renderCharts();
    window.addEventListener("resize", handleResize);
  } catch {
    message.error("Failed to load agent metrics.");
  } finally {
    loading.value = false;
  }
}

const buildQueryParams = computed(() => {
  if (rangeMode.value === "7d") return { days: 7 };
  if (rangeMode.value === "30d") return { days: 30 };

  if (!customRange.value || customRange.value.length !== 2) {
    return {};
  }

  return {
    startDate: dayjs(customRange.value[0]).format("YYYY-MM-DD"),
    endDate: dayjs(customRange.value[1]).format("YYYY-MM-DD"),
  };
});

function renderCharts() {
  renderTimelineChart();
  renderErrorChart();
}

function renderTimelineChart() {
  if (!timelineChartRef.value) return;
  if (!timelineChart) timelineChart = echarts.init(timelineChartRef.value);

  const dates = timeline.value.map((item) => item.date);
  const calls = timeline.value.map((item) => Number(item.totalCalls || 0));
  const successRates = timeline.value.map((item) => Number(item.successRate || 0));
  const latencies = timeline.value.map((item) => Number(item.averageLatencyMs || 0));

  timelineChart.setOption(
    {
      tooltip: { trigger: "axis" },
      legend: { data: ["Calls", "Success Rate (%)", "Avg Latency (ms)"] },
      grid: { left: "4%", right: "6%", bottom: "4%", containLabel: true },
      xAxis: {
        type: "category",
        data: dates.length ? dates : ["No data"],
      },
      yAxis: [
        { type: "value", name: "Calls" },
        { type: "value", name: "Percent", axisLabel: { formatter: "{value}%" } },
        { type: "value", name: "Latency (ms)" },
      ],
      series: [
        {
          name: "Calls",
          type: "bar",
          data: calls,
          itemStyle: { color: "#2f6cf6" },
        },
        {
          name: "Success Rate (%)",
          type: "line",
          yAxisIndex: 1,
          smooth: true,
          data: successRates,
          itemStyle: { color: "#19be6b" },
        },
        {
          name: "Avg Latency (ms)",
          type: "line",
          yAxisIndex: 2,
          smooth: true,
          data: latencies,
          itemStyle: { color: "#ff7d00" },
        },
      ],
    },
    true,
  );
}

function renderErrorChart() {
  if (!errorChartRef.value) return;
  if (!errorChart) errorChart = echarts.init(errorChartRef.value);

  const pieData = errors.value.map((item) => ({
    value: Number(item.count || 0),
    name: item.errorMessage || "Unknown error",
  }));

  errorChart.setOption(
    {
      tooltip: { trigger: "item" },
      legend: { top: "bottom" },
      series: [
        {
          name: "Error Count",
          type: "pie",
          radius: ["45%", "72%"],
          avoidLabelOverlap: true,
          data: pieData.length ? pieData : [{ value: 1, name: "No error data" }],
          label: { formatter: "{b}: {c}" },
        },
      ],
    },
    true,
  );
}

function handleResize() {
  timelineChart?.resize();
  errorChart?.resize();
}
</script>

<style scoped lang="scss">
.metric-grid {
  margin-bottom: var(--cr-space-lg);
}

.tool-card {
  margin-top: var(--cr-space-lg);
}

.chart-container {
  height: 360px;

  &--small {
    height: 320px;
  }
}

@media (max-width: 1200px) {
  :deep(.n-grid-item) {
    grid-column: span 24 !important;
  }
}
</style>

