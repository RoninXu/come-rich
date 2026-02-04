<template>
  <div class="monthly-report-page">
    <n-spin :show="loading">
      <PageHeader title="月度报表">
        <template #actions>
          <n-date-picker
            v-model:formatted-value="selectedMonth"
            type="month"
            value-format="yyyy-MM"
            style="width: 160px"
            @update:formatted-value="fetchData"
          />
          <n-button
            :loading="exporting"
            @click="handleExportReport"
          >
            <template #icon>
              <n-icon><Download /></n-icon>
            </template>
            导出报表
          </n-button>
        </template>
      </PageHeader>

      <n-grid
        :x-gap="16"
        :y-gap="16"
        :cols="4"
        class="summary-cards"
      >
        <n-gi>
          <div class="summary-card income">
            <div class="card-title">
              总收入
            </div>
            <div class="card-value">
              ¥ {{ summary.totalIncome.toFixed(2) }}
            </div>
          </div>
        </n-gi>
        <n-gi>
          <div class="summary-card expense">
            <div class="card-title">
              总支出
            </div>
            <div class="card-value">
              ¥ {{ summary.totalExpense.toFixed(2) }}
            </div>
          </div>
        </n-gi>
        <n-gi>
          <div class="summary-card balance">
            <div class="card-title">
              结余
            </div>
            <div class="card-value">
              ¥ {{ summary.balance.toFixed(2) }}
            </div>
          </div>
        </n-gi>
        <n-gi>
          <div class="summary-card savings">
            <div class="card-title">
              储蓄率
            </div>
            <div class="card-value">
              {{ summary.savingsRate.toFixed(1) }}%
            </div>
          </div>
        </n-gi>
      </n-grid>

      <n-grid
        :x-gap="16"
        :y-gap="16"
        :cols="2"
      >
        <n-gi>
          <GlassCard>
            <template #header>
              支出分类统计
            </template>
            <div
              ref="expensePieRef"
              class="chart-container"
            />
          </GlassCard>
        </n-gi>
        <n-gi>
          <GlassCard>
            <template #header>
              每日收支趋势
            </template>
            <div
              ref="dailyTrendRef"
              class="chart-container"
            />
          </GlassCard>
        </n-gi>
      </n-grid>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from "vue";
import dayjs from "dayjs";
import * as echarts from "echarts";
import {
  NSpin,
  NGrid,
  NGi,
  NButton,
  NIcon,
  NDatePicker,
  useMessage,
} from "naive-ui";
import { Download } from "@vicons/ionicons5";
import {
  getMonthlySummary,
  getCategoryStats,
  getDailyStats,
} from "@/api/analysis";
import { exportMonthlyReport } from "@/api/export";
import { downloadBlob } from "@/utils/export";
import type { MonthlySummary, CategoryStat, DailyStat } from "@/types/analysis";
import GlassCard from "@/components/common/GlassCard.vue";
import PageHeader from "@/components/common/PageHeader.vue";

const message = useMessage();
const selectedMonth = ref(dayjs().format("YYYY-MM"));
const loading = ref(false);
const exporting = ref(false);

const summary = reactive({
  totalIncome: 0,
  totalExpense: 0,
  balance: 0,
  savingsRate: 0,
});

const categoryStats = ref<CategoryStat[]>([]);
const dailyStats = ref<DailyStat[]>([]);

const expensePieRef = ref<HTMLElement>();
const dailyTrendRef = ref<HTMLElement>();

let expensePieChart: echarts.ECharts | null = null;
let dailyTrendChart: echarts.ECharts | null = null;

onMounted(() => {
  initCharts();
  fetchData();
});

function initCharts() {
  nextTick(() => {
    if (expensePieRef.value)
      expensePieChart = echarts.init(expensePieRef.value);
    if (dailyTrendRef.value)
      dailyTrendChart = echarts.init(dailyTrendRef.value);
  });
}

async function fetchData() {
  loading.value = true;
  try {
    const [year, month] = selectedMonth.value.split("-").map(Number);

    const [summaryRes, categoryRes, dailyRes] = await Promise.all([
      getMonthlySummary(year, month),
      getCategoryStats(year, month, 2),
      getDailyStats(year, month),
    ]);

    if (summaryRes.data.code === 200 && summaryRes.data.data) {
      const data = summaryRes.data.data;
      summary.totalIncome = data.totalIncome || 0;
      summary.totalExpense = data.totalExpense || 0;
      summary.balance = data.balance || 0;
      summary.savingsRate = data.savingsRate || 0;
    }

    if (categoryRes.data.code === 200 && categoryRes.data.data) {
      categoryStats.value = categoryRes.data.data;
    }

    if (dailyRes.data.code === 200 && dailyRes.data.data) {
      dailyStats.value = dailyRes.data.data;
    }

    nextTick(() => {
      updateExpensePieChart();
      updateDailyTrendChart();
    });
  } catch {
    message.error("加载数据失败");
  } finally {
    loading.value = false;
  }
}

async function handleExportReport() {
  exporting.value = true;
  try {
    const [year, month] = selectedMonth.value.split("-").map(Number);
    const res = await exportMonthlyReport(year, month);
    downloadBlob(new Blob([res.data]), `月度报表_${selectedMonth.value}.xlsx`);
    message.success("导出成功");
  } catch {
    message.error("导出失败");
  } finally {
    exporting.value = false;
  }
}

function updateExpensePieChart() {
  if (!expensePieChart) return;

  const chartData = categoryStats.value.map((stat) => ({
    value: Number(stat.amount),
    name: stat.categoryName,
    itemStyle: stat.categoryColor ? { color: stat.categoryColor } : undefined,
  }));

  expensePieChart.setOption(
    {
      tooltip: { trigger: "item", formatter: "{b}: ¥{c} ({d}%)" },
      legend: { orient: "vertical", right: 10, top: "center" },
      series: [
        {
          type: "pie",
          radius: ["40%", "70%"],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: "var(--cr-bg-card)",
            borderWidth: 2,
          },
          label: { show: false },
          data:
            chartData.length > 0 ? chartData : [{ value: 0, name: "暂无数据" }],
        },
      ],
    },
    true,
  );
}

function updateDailyTrendChart() {
  if (!dailyTrendChart) return;

  const dates = dailyStats.value.map((stat) => `${dayjs(stat.date).date()}日`);
  const incomeData = dailyStats.value.map((stat) => Number(stat.income));
  const expenseData = dailyStats.value.map((stat) => Number(stat.expense));

  dailyTrendChart.setOption(
    {
      tooltip: { trigger: "axis" },
      legend: { data: ["收入", "支出"] },
      grid: { left: "3%", right: "4%", bottom: "3%", containLabel: true },
      xAxis: {
        type: "category",
        boundaryGap: false,
        data: dates.length > 0 ? dates : ["暂无数据"],
      },
      yAxis: { type: "value" },
      series: [
        {
          name: "收入",
          type: "line",
          smooth: true,
          data: incomeData,
          itemStyle: { color: "#34C759" },
        },
        {
          name: "支出",
          type: "line",
          smooth: true,
          data: expenseData,
          itemStyle: { color: "#FF3B30" },
        },
      ],
    },
    true,
  );
}
</script>

<style scoped lang="scss">
.monthly-report-page {
  max-width: 1200px;
  margin: 0 auto;

  .summary-cards {
    margin-bottom: 16px;
  }

  .summary-card {
    text-align: center;
    padding: 20px;
    background: var(--cr-bg-card);
    backdrop-filter: blur(var(--cr-blur-md));
    border: 1px solid var(--cr-border-light);
    border-radius: var(--cr-radius-xl);
    box-shadow: var(--cr-shadow-sm);

    .card-title {
      font-size: 14px;
      color: var(--cr-text-secondary);
      margin-bottom: 8px;
    }
    .card-value {
      font-size: 24px;
      font-weight: 700;
    }

    &.income .card-value {
      color: var(--cr-success);
    }
    &.expense .card-value {
      color: var(--cr-error);
    }
    &.balance .card-value {
      color: var(--cr-primary);
    }
    &.savings .card-value {
      color: var(--cr-warning);
    }
  }

  .chart-container {
    height: 300px;
  }
}
</style>
