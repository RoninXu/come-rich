<template>
  <div class="budget-trend-page">
    <n-spin :show="loading">
      <PageHeader
        title="预算趋势"
        show-back
      />

      <GlassCard>
        <template #header>
          近6个月预算使用率趋势
        </template>
        <div
          ref="trendChartRef"
          class="chart-container"
        />
      </GlassCard>

      <GlassCard style="margin-top: 16px">
        <template #header>
          月度预算对比
        </template>
        <n-data-table
          :columns="columns"
          :data="trendData"
          :bordered="false"
          striped
        />
      </GlassCard>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, h } from "vue";
import {
  NSpin,
  NDataTable,
  NProgress,
  useMessage,
  type DataTableColumns,
} from "naive-ui";
import * as echarts from "echarts";
import { getBudgetTrend } from "@/api/budget";
import type { BudgetSummary } from "@/types/budget";
import GlassCard from "@/components/common/GlassCard.vue";
import PageHeader from "@/components/common/PageHeader.vue";

const message = useMessage();
const loading = ref(false);
const trendData = ref<BudgetSummary[]>([]);
const trendChartRef = ref<HTMLElement>();
let trendChart: echarts.ECharts | null = null;

function getProgressColor(p: number): string {
  return p >= 100 ? "#FF3B30" : p >= 80 ? "#FF9500" : "#34C759";
}

const columns: DataTableColumns<BudgetSummary> = [
  { title: "月份", key: "yearMonth", width: 120 },
  {
    title: "总预算",
    key: "totalBudget",
    width: 150,
    render: (row) => `¥${Number(row.totalBudget).toFixed(2)}`,
  },
  {
    title: "总支出",
    key: "totalSpent",
    width: 150,
    render: (row) => `¥${Number(row.totalSpent).toFixed(2)}`,
  },
  {
    title: "使用率",
    key: "utilization",
    width: 200,
    render: (row) =>
      h(NProgress, {
        type: "line",
        percentage: Math.min(Number(row.overallUtilization), 100),
        color: getProgressColor(Number(row.overallUtilization)),
      }),
  },
  {
    title: "剩余",
    key: "totalRemaining",
    width: 150,
    render: (row) =>
      h(
        "span",
        {
          style: {
            color:
              Number(row.totalRemaining) < 0
                ? "var(--cr-error)"
                : "var(--cr-success)",
          },
        },
        `¥${Number(row.totalRemaining).toFixed(2)}`,
      ),
  },
];

onMounted(() => {
  fetchTrend();
});

async function fetchTrend() {
  loading.value = true;
  try {
    const res = await getBudgetTrend(6);
    if (res.data.code === 200) trendData.value = res.data.data || [];
    nextTick(() => updateChart());
  } catch {
    message.error("加载趋势数据失败");
  } finally {
    loading.value = false;
  }
}

function updateChart() {
  if (!trendChartRef.value) return;
  if (!trendChart) trendChart = echarts.init(trendChartRef.value);

  const months = trendData.value.map((d) => d.yearMonth);
  const budgets = trendData.value.map((d) => Number(d.totalBudget));
  const spents = trendData.value.map((d) => Number(d.totalSpent));
  const utils = trendData.value.map((d) => Number(d.overallUtilization));

  trendChart.setOption(
    {
      tooltip: { trigger: "axis" },
      legend: { data: ["预算", "支出", "使用率(%)"] },
      grid: { left: "3%", right: "10%", bottom: "3%", containLabel: true },
      xAxis: {
        type: "category",
        data: months.length > 0 ? months : ["暂无数据"],
      },
      yAxis: [
        { type: "value", name: "金额(元)" },
        {
          type: "value",
          name: "使用率(%)",
          max: 150,
          axisLabel: { formatter: "{value}%" },
        },
      ],
      series: [
        {
          name: "预算",
          type: "bar",
          data: budgets,
          itemStyle: { color: "#007AFF" },
        },
        {
          name: "支出",
          type: "bar",
          data: spents,
          itemStyle: { color: "#FF9500" },
        },
        {
          name: "使用率(%)",
          type: "line",
          yAxisIndex: 1,
          data: utils,
          itemStyle: { color: "#FF9F0A" },
          smooth: true,
        },
      ],
    },
    true,
  );
}
</script>

<style scoped lang="scss">
.budget-trend-page {
  .chart-container {
    height: 350px;
  }
}
</style>
