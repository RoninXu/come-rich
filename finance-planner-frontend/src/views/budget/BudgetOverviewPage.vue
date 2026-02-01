<template>
  <div class="budget-overview-page" v-loading="loading">
    <div class="page-header">
      <h2>预算管理</h2>
      <div style="display: flex; gap: 8px; align-items: center">
        <el-date-picker
          v-model="selectedMonth"
          type="month"
          placeholder="选择月份"
          format="YYYY年MM月"
          value-format="YYYY-MM"
          @change="fetchData"
        />
        <el-button type="primary" @click="goToEdit">
          <el-icon><Setting /></el-icon>
          设置预算
        </el-button>
        <el-button @click="handleCopyPrevious">
          <el-icon><CopyDocument /></el-icon>
          复制上月
        </el-button>
        <el-button @click="handleAiSuggestions" :loading="aiLoading">
          <el-icon><MagicStick /></el-icon>
          AI优化建议
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" class="summary-cards">
      <el-col :span="6">
        <el-card class="summary-card budget">
          <div class="card-title">总预算</div>
          <div class="card-value">¥ {{ formatNumber(summary?.totalBudget) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card spent">
          <div class="card-title">已支出</div>
          <div class="card-value">¥ {{ formatNumber(summary?.totalSpent) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card remaining">
          <div class="card-title">剩余</div>
          <div class="card-value">¥ {{ formatNumber(summary?.totalRemaining) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card utilization">
          <div class="card-title">使用率</div>
          <div class="card-value">{{ (summary?.overallUtilization ?? 0).toFixed(1) }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="14">
        <el-card>
          <template #header>分类预算执行情况</template>
          <el-empty v-if="!summary?.categories?.length" description="暂无预算数据，请先设置预算" />
          <el-table v-else :data="summary.categories" stripe>
            <el-table-column prop="categoryName" label="分类" width="140">
              <template #default="{ row }">
                <span :style="{ color: row.categoryColor || '#333' }">{{ row.categoryName }}</span>
              </template>
            </el-table-column>
            <el-table-column label="预算" width="120">
              <template #default="{ row }">
                ¥{{ Number(row.budgetAmount).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="实际" width="120">
              <template #default="{ row }">
                ¥{{ Number(row.actualAmount).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="执行进度" min-width="200">
              <template #default="{ row }">
                <el-progress
                  :percentage="Math.min(row.utilizationPercentage, 100)"
                  :color="getProgressColor(row.utilizationPercentage)"
                  :stroke-width="12"
                  :format="() => row.utilizationPercentage.toFixed(1) + '%'"
                />
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.overBudget ? 'danger' : 'success'" size="small">
                  {{ row.overBudget ? '超支' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header>预算 vs 实际</template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-button style="margin-top: 16px" @click="goToTrend">
      <el-icon><DataLine /></el-icon>
      查看预算趋势
    </el-button>

    <!-- AI Suggestion Dialog -->
    <el-dialog v-model="aiDialogVisible" title="AI 预算优化建议" width="600px">
      <div v-if="aiSuggestion">
        <p style="margin-bottom: 16px">{{ aiSuggestion.summary }}</p>
        <el-divider />
        <div v-if="aiSuggestion.suggestions?.length">
          <h4>优化建议</h4>
          <ul>
            <li v-for="(s, i) in aiSuggestion.suggestions" :key="i">{{ s }}</li>
          </ul>
        </div>
        <el-alert
          v-if="aiSuggestion.riskWarning"
          :title="aiSuggestion.riskWarning"
          type="warning"
          :closable="false"
          show-icon
          style="margin-top: 16px"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Setting, CopyDocument, MagicStick, DataLine } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import { getBudgetSummary, copyBudgetFromPreviousMonth, getAiBudgetSuggestions } from '@/api/budget'
import type { BudgetSummary, BudgetAiSuggestion } from '@/types/budget'

const router = useRouter()
const selectedMonth = ref(dayjs().format('YYYY-MM'))
const loading = ref(false)
const aiLoading = ref(false)
const summary = ref<BudgetSummary | null>(null)
const aiDialogVisible = ref(false)
const aiSuggestion = ref<BudgetAiSuggestion | null>(null)

const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getBudgetSummary(selectedMonth.value)
    if (res.data.code === 200) {
      summary.value = res.data.data
    }
    nextTick(() => updateChart())
  } catch {
    ElMessage.error('加载预算数据失败')
  } finally {
    loading.value = false
  }
}

function updateChart() {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const categories = summary.value?.categories || []
  const names = categories.map(c => c.categoryName)
  const budgetData = categories.map(c => Number(c.budgetAmount))
  const actualData = categories.map(c => Number(c.actualAmount))

  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['预算', '实际'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: names.length > 0 ? names : ['暂无数据'],
      axisLabel: { rotate: 30 }
    },
    yAxis: { type: 'value' },
    series: [
      { name: '预算', type: 'bar', data: budgetData, itemStyle: { color: '#409EFF' } },
      { name: '实际', type: 'bar', data: actualData, itemStyle: { color: '#FF7043' } }
    ]
  }
  chart.setOption(option, true)
}

async function handleCopyPrevious() {
  try {
    const res = await copyBudgetFromPreviousMonth(selectedMonth.value)
    if (res.data.code === 200) {
      ElMessage.success('复制成功')
      fetchData()
    }
  } catch {
    ElMessage.error('复制失败')
  }
}

async function handleAiSuggestions() {
  aiLoading.value = true
  try {
    const res = await getAiBudgetSuggestions(selectedMonth.value)
    if (res.data.code === 200) {
      aiSuggestion.value = res.data.data
      aiDialogVisible.value = true
    }
  } catch {
    ElMessage.error('获取AI建议失败')
  } finally {
    aiLoading.value = false
  }
}

function goToEdit() {
  router.push({ path: '/budget/edit', query: { month: selectedMonth.value } })
}

function goToTrend() {
  router.push('/budget/trend')
}

function formatNumber(value: number | undefined | null): string {
  if (value === undefined || value === null) return '0.00'
  return Number(value).toFixed(2)
}

function getProgressColor(percentage: number): string {
  if (percentage >= 100) return '#F56C6C'
  if (percentage >= 80) return '#E6A23C'
  return '#67C23A'
}
</script>

<style scoped lang="scss">
.budget-overview-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 { margin: 0; }
  }

  .summary-cards {
    margin-bottom: 20px;
  }

  .summary-card {
    text-align: center;
    padding: 10px;

    .card-title {
      font-size: 14px;
      color: #999;
      margin-bottom: 8px;
    }

    .card-value {
      font-size: 24px;
      font-weight: bold;
    }

    &.budget .card-value { color: #409EFF; }
    &.spent .card-value { color: #FF7043; }
    &.remaining .card-value { color: #67C23A; }
    &.utilization .card-value { color: #E6A23C; }
  }

  .chart-container {
    height: 350px;
  }
}
</style>
