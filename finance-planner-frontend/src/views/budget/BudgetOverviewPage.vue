<template>
  <div class="budget-overview-page">
    <n-spin :show="loading">
      <PageHeader title="预算管理">
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
          <n-button :loading="aiLoading" @click="handleAiSuggestions">AI优化建议</n-button>
        </template>
      </PageHeader>

      <n-grid :x-gap="16" :y-gap="16" :cols="4" class="summary-cards">
        <n-gi>
          <div class="summary-card budget">
            <div class="card-title">总预算</div>
            <div class="card-value">¥ {{ formatNumber(summary?.totalBudget) }}</div>
          </div>
        </n-gi>
        <n-gi>
          <div class="summary-card spent">
            <div class="card-title">已支出</div>
            <div class="card-value">¥ {{ formatNumber(summary?.totalSpent) }}</div>
          </div>
        </n-gi>
        <n-gi>
          <div class="summary-card remaining">
            <div class="card-title">剩余</div>
            <div class="card-value">¥ {{ formatNumber(summary?.totalRemaining) }}</div>
          </div>
        </n-gi>
        <n-gi>
          <div class="summary-card utilization">
            <div class="card-title">使用率</div>
            <div class="card-value">{{ (summary?.overallUtilization ?? 0).toFixed(1) }}%</div>
          </div>
        </n-gi>
      </n-grid>

      <n-grid :x-gap="16" :y-gap="16" :cols="24">
        <n-gi :span="14">
          <GlassCard>
            <template #header>分类预算执行情况</template>
            <n-empty v-if="!summary?.categories?.length" description="暂无预算数据，请先设置预算" />
            <n-data-table v-else :columns="columns" :data="summary.categories" :bordered="false" striped />
          </GlassCard>
        </n-gi>
        <n-gi :span="10">
          <GlassCard>
            <template #header>预算 vs 实际</template>
            <div ref="chartRef" class="chart-container"></div>
          </GlassCard>
        </n-gi>
      </n-grid>

      <n-button style="margin-top: 16px" @click="goToTrend">
        <template #icon><n-icon><TrendingUp /></n-icon></template>
        查看预算趋势
      </n-button>

      <n-modal v-model:show="aiDialogVisible" preset="card" title="AI 预算优化建议" style="width: 600px">
        <div v-if="aiSuggestion">
          <p style="margin-bottom: 16px">{{ aiSuggestion.summary }}</p>
          <n-divider />
          <div v-if="aiSuggestion.suggestions?.length">
            <h4>优化建议</h4>
            <ul style="padding-left: 20px">
              <li v-for="(s, i) in aiSuggestion.suggestions" :key="i">{{ s }}</li>
            </ul>
          </div>
          <n-alert v-if="aiSuggestion.riskWarning" :title="aiSuggestion.riskWarning" type="warning" style="margin-top: 16px" />
        </div>
      </n-modal>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, h } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NGrid, NGi, NDataTable, NButton, NIcon,
  NDatePicker, NModal, NDivider, NAlert, NEmpty, NTag, NProgress,
  useMessage, type DataTableColumns,
} from 'naive-ui'
import { TrendingUp } from '@vicons/ionicons5'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import { getBudgetSummary, copyBudgetFromPreviousMonth, getAiBudgetSuggestions } from '@/api/budget'
import type { BudgetSummary, BudgetAiSuggestion } from '@/types/budget'
import GlassCard from '@/components/common/GlassCard.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const router = useRouter()
const message = useMessage()
const selectedMonth = ref(dayjs().format('YYYY-MM'))
const loading = ref(false)
const aiLoading = ref(false)
const summary = ref<BudgetSummary | null>(null)
const aiDialogVisible = ref(false)
const aiSuggestion = ref<BudgetAiSuggestion | null>(null)

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const columns: DataTableColumns<any> = [
  {
    title: '分类', key: 'categoryName', width: 140,
    render: (row) => h('span', { style: { color: row.categoryColor || 'var(--cr-text-primary)' } }, row.categoryName)
  },
  { title: '预算', key: 'budgetAmount', width: 120, render: (row) => `¥${Number(row.budgetAmount).toFixed(2)}` },
  { title: '实际', key: 'actualAmount', width: 120, render: (row) => `¥${Number(row.actualAmount).toFixed(2)}` },
  {
    title: '执行进度', key: 'progress', minWidth: 200,
    render: (row) => h(NProgress, {
      type: 'line',
      percentage: Math.min(row.utilizationPercentage, 100),
      color: getProgressColor(row.utilizationPercentage),
      height: 12,
      borderRadius: 6,
    })
  },
  {
    title: '状态', key: 'status', width: 80,
    render: (row) => h(NTag, { type: row.overBudget ? 'error' : 'success', size: 'small' }, () => row.overBudget ? '超支' : '正常')
  },
]

onMounted(() => { fetchData() })

async function fetchData() {
  loading.value = true
  try {
    const res = await getBudgetSummary(selectedMonth.value)
    if (res.data.code === 200) summary.value = res.data.data
    nextTick(() => updateChart())
  } catch {
    message.error('加载预算数据失败')
  } finally {
    loading.value = false
  }
}

function updateChart() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)

  const categories = summary.value?.categories || []
  const names = categories.map(c => c.categoryName)
  const budgetData = categories.map(c => Number(c.budgetAmount))
  const actualData = categories.map(c => Number(c.actualAmount))

  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['预算', '实际'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: names.length > 0 ? names : ['暂无数据'], axisLabel: { rotate: 30 } },
    yAxis: { type: 'value' },
    series: [
      { name: '预算', type: 'bar', data: budgetData, itemStyle: { color: '#007AFF' } },
      { name: '实际', type: 'bar', data: actualData, itemStyle: { color: '#FF9500' } }
    ]
  }, true)
}

async function handleCopyPrevious() {
  try {
    const res = await copyBudgetFromPreviousMonth(selectedMonth.value)
    if (res.data.code === 200) { message.success('复制成功'); fetchData() }
  } catch { message.error('复制失败') }
}

async function handleAiSuggestions() {
  aiLoading.value = true
  try {
    const res = await getAiBudgetSuggestions(selectedMonth.value)
    if (res.data.code === 200) { aiSuggestion.value = res.data.data; aiDialogVisible.value = true }
  } catch { message.error('获取AI建议失败') }
  finally { aiLoading.value = false }
}

function goToEdit() { router.push({ path: '/budget/edit', query: { month: selectedMonth.value } }) }
function goToTrend() { router.push('/budget/trend') }
function formatNumber(v: number | undefined | null): string { return v == null ? '0.00' : Number(v).toFixed(2) }
function getProgressColor(p: number): string { return p >= 100 ? '#FF3B30' : p >= 80 ? '#FF9500' : '#34C759' }
</script>

<style scoped lang="scss">
.budget-overview-page {
  .summary-cards { margin-bottom: 16px; }

  .summary-card {
    text-align: center;
    padding: 20px;
    background: var(--cr-bg-card);
    backdrop-filter: blur(var(--cr-blur-md));
    border: 1px solid var(--cr-border-light);
    border-radius: var(--cr-radius-xl);
    box-shadow: var(--cr-shadow-sm);

    .card-title { font-size: 14px; color: var(--cr-text-secondary); margin-bottom: 8px; }
    .card-value { font-size: 24px; font-weight: 700; }

    &.budget .card-value { color: var(--cr-primary); }
    &.spent .card-value { color: var(--cr-error); }
    &.remaining .card-value { color: var(--cr-success); }
    &.utilization .card-value { color: var(--cr-warning); }
  }

  .chart-container { height: 350px; }
}
</style>
