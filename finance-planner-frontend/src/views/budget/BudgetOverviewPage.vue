<template>
  <div class="budget-overview-page">
    <n-spin :show="loading">
      <PageHeader title="预算总览" subtitle="观察预算消耗，快速处理超支风险">
        <template #actions>
          <n-date-picker
            v-model:formatted-value="selectedMonth"
            type="month"
            value-format="yyyy-MM"
            style="width: 160px"
            @update:formatted-value="fetchData"
          />
          <n-button type="primary" @click="goToEdit">设置预算</n-button>
          <n-button @click="handleCopyPrevious">复制上月</n-button>
          <n-button :loading="aiLoading" @click="handleAiSuggestions">获取 AI 建议</n-button>
        </template>
      </PageHeader>

      <n-grid :x-gap="16" :y-gap="16" :cols="4" class="metric-grid">
        <n-gi>
          <MetricCard label="总预算" :value="formatCurrency(summary?.totalBudget)">
            <template #icon><Wallet /></template>
          </MetricCard>
        </n-gi>
        <n-gi>
          <MetricCard label="已支出" :value="formatCurrency(summary?.totalSpent)" tone="negative">
            <template #icon><TrendingDown /></template>
          </MetricCard>
        </n-gi>
        <n-gi>
          <MetricCard label="剩余预算" :value="formatCurrency(summary?.totalRemaining)" tone="positive">
            <template #icon><Leaf /></template>
          </MetricCard>
        </n-gi>
        <n-gi>
          <MetricCard
            label="预算使用率"
            :value="`${(summary?.overallUtilization ?? 0).toFixed(1)}%`"
          >
            <template #icon><Pulse /></template>
          </MetricCard>
        </n-gi>
      </n-grid>

      <n-grid :x-gap="16" :y-gap="16" :cols="24">
        <n-gi :span="16">
          <GlassCard>
            <template #header>
              <SectionHeader title="分类预算执行" description="识别即将超支或已超支的类别" />
            </template>

            <EmptyState
              v-if="!summary?.categories?.length"
              title="暂无预算数据"
              description="先设置预算，系统才能给出风险提示"
            />

            <div v-else class="budget-list">
              <div v-for="row in summary.categories" :key="row.categoryId" class="budget-row">
                <div class="budget-row__head">
                  <span class="budget-row__name">{{ row.categoryName }}</span>
                  <span class="budget-row__amount tabular-nums">
                    {{ formatCurrency(row.actualAmount) }} / {{ formatCurrency(row.budgetAmount) }}
                  </span>
                </div>
                <BudgetProgressBar :budget="Number(row.budgetAmount)" :spent="Number(row.actualAmount)" :show-label="false" />
              </div>
            </div>
          </GlassCard>
        </n-gi>

        <n-gi :span="8">
          <GlassCard>
            <template #header>
              <SectionHeader title="预算 vs 实际" description="按分类查看偏差" />
            </template>
            <div ref="chartRef" class="chart-container" />
          </GlassCard>

          <GlassCard style="margin-top: 16px" variant="soft">
            <template #header>
              <SectionHeader title="AI 优化建议" />
            </template>

            <InsightPanel
              :title="aiSuggestion?.summary || '暂无建议'"
              :items="aiSuggestion?.suggestions || []"
            />

            <n-alert
              v-if="aiSuggestion?.riskWarning"
              :title="aiSuggestion.riskWarning"
              type="warning"
              style="margin-top: 12px"
            />
          </GlassCard>
        </n-gi>
      </n-grid>

      <div class="trend-link">
        <n-button @click="goToTrend">查看预算趋势</n-button>
      </div>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from "vue";
import { useRouter } from "vue-router";
import {
  NSpin,
  NGrid,
  NGi,
  NButton,
  NDatePicker,
  NAlert,
  useMessage,
} from "naive-ui";
import { Wallet, TrendingDown, Leaf, Pulse } from "@vicons/ionicons5";
import dayjs from "dayjs";
import * as echarts from "echarts";
import {
  getBudgetSummary,
  copyBudgetFromPreviousMonth,
  getAiBudgetSuggestions,
} from "@/api/budget";
import type { BudgetSummary, BudgetAiSuggestion } from "@/types/budget";
import GlassCard from "@/components/common/GlassCard.vue";
import PageHeader from "@/components/common/PageHeader.vue";
import SectionHeader from "@/components/common/SectionHeader.vue";
import MetricCard from "@/components/common/MetricCard.vue";
import BudgetProgressBar from "@/components/common/BudgetProgressBar.vue";
import InsightPanel from "@/components/common/InsightPanel.vue";
import EmptyState from "@/components/common/EmptyState.vue";

const router = useRouter();
const message = useMessage();
const selectedMonth = ref(dayjs().format("YYYY-MM"));
const loading = ref(false);
const aiLoading = ref(false);
const summary = ref<BudgetSummary | null>(null);
const aiSuggestion = ref<BudgetAiSuggestion | null>(null);

const chartRef = ref<HTMLElement>();
let chart: echarts.ECharts | null = null;

onMounted(fetchData);

async function fetchData() {
  loading.value = true;
  try {
    const res = await getBudgetSummary(selectedMonth.value);
    if (res.data.code === 200) {
      summary.value = res.data.data;
      nextTick(updateChart);
    }
  } catch {
    message.error("加载预算数据失败");
  } finally {
    loading.value = false;
  }
}

function updateChart() {
  if (!chartRef.value) return;
  if (!chart) chart = echarts.init(chartRef.value);

  const categories = summary.value?.categories || [];
  chart.setOption(
    {
      tooltip: { trigger: "axis" },
      legend: { data: ["预算", "实际"], textStyle: { color: "var(--cr-text-secondary)" } },
      grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
      xAxis: {
        type: "category",
        data: categories.map((item) => item.categoryName),
        axisLabel: { color: "var(--cr-text-secondary)", rotate: 24 },
      },
      yAxis: {
        type: "value",
        axisLabel: { color: "var(--cr-text-secondary)" },
        splitLine: { lineStyle: { color: "var(--cr-border-light)" } },
      },
      series: [
        {
          name: "预算",
          type: "bar",
          data: categories.map((item) => Number(item.budgetAmount)),
          itemStyle: { color: "#1463ff" },
        },
        {
          name: "实际",
          type: "bar",
          data: categories.map((item) => Number(item.actualAmount)),
          itemStyle: { color: "#f59e0b" },
        },
      ],
    },
    true,
  );
}

async function handleCopyPrevious() {
  try {
    const res = await copyBudgetFromPreviousMonth(selectedMonth.value);
    if (res.data.code === 200) {
      message.success("复制成功");
      fetchData();
    }
  } catch {
    message.error("复制失败");
  }
}

async function handleAiSuggestions() {
  aiLoading.value = true;
  try {
    const res = await getAiBudgetSuggestions(selectedMonth.value);
    if (res.data.code === 200) {
      aiSuggestion.value = res.data.data;
    }
  } catch {
    message.error("获取 AI 建议失败");
  } finally {
    aiLoading.value = false;
  }
}

function goToEdit() {
  router.push({ path: "/budget/edit", query: { month: selectedMonth.value } });
}

function goToTrend() {
  router.push("/budget/trend");
}

function formatCurrency(value: number | undefined | null) {
  return new Intl.NumberFormat("zh-CN", {
    style: "currency",
    currency: "CNY",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(Number(value ?? 0));
}
</script>

<style scoped lang="scss">
.metric-grid {
  margin-bottom: var(--cr-space-lg);
}

.budget-list {
  display: grid;
  gap: 14px;
}

.budget-row {
  border: 1px solid var(--cr-border-light);
  border-radius: var(--cr-radius-md);
  padding: var(--cr-space-md);

  &__head {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
  }

  &__name {
    font-size: 13px;
    font-weight: 600;
    color: var(--cr-text-primary);
  }

  &__amount {
    font-size: 12px;
    color: var(--cr-text-secondary);
  }
}

.chart-container {
  height: 320px;
}

.trend-link {
  margin-top: 16px;
}

@media (max-width: 1200px) {
  :deep(.n-grid-item) {
    grid-column: span 24 !important;
  }
}
</style>
