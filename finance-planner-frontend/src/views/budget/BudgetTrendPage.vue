<template>
  <div class="budget-trend-page" v-loading="loading">
    <div class="page-header">
      <h2>预算趋势</h2>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <el-card>
      <template #header>近6个月预算使用率趋势</template>
      <div ref="trendChartRef" class="chart-container"></div>
    </el-card>

    <el-card style="margin-top: 16px">
      <template #header>月度预算对比</template>
      <el-table :data="trendData" stripe>
        <el-table-column prop="yearMonth" label="月份" width="120" />
        <el-table-column label="总预算" width="150">
          <template #default="{ row }">
            ¥{{ Number(row.totalBudget).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="总支出" width="150">
          <template #default="{ row }">
            ¥{{ Number(row.totalSpent).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="使用率" width="200">
          <template #default="{ row }">
            <el-progress
              :percentage="Math.min(Number(row.overallUtilization), 100)"
              :color="getProgressColor(Number(row.overallUtilization))"
              :format="() => Number(row.overallUtilization).toFixed(1) + '%'"
            />
          </template>
        </el-table-column>
        <el-table-column label="剩余" width="150">
          <template #default="{ row }">
            <span :style="{ color: Number(row.totalRemaining) < 0 ? '#F56C6C' : '#67C23A' }">
              ¥{{ Number(row.totalRemaining).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getBudgetTrend } from '@/api/budget'
import type { BudgetSummary } from '@/types/budget'

const router = useRouter()
const loading = ref(false)
const trendData = ref<BudgetSummary[]>([])
const trendChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null

onMounted(() => {
  fetchTrend()
})

async function fetchTrend() {
  loading.value = true
  try {
    const res = await getBudgetTrend(6)
    if (res.data.code === 200) {
      trendData.value = res.data.data || []
    }
    nextTick(() => updateChart())
  } catch {
    ElMessage.error('加载趋势数据失败')
  } finally {
    loading.value = false
  }
}

function updateChart() {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const months = trendData.value.map(d => d.yearMonth)
  const budgets = trendData.value.map(d => Number(d.totalBudget))
  const spents = trendData.value.map(d => Number(d.totalSpent))
  const utils = trendData.value.map(d => Number(d.overallUtilization))

  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['预算', '支出', '使用率(%)'] },
    grid: { left: '3%', right: '10%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: months.length > 0 ? months : ['暂无数据'] },
    yAxis: [
      { type: 'value', name: '金额(元)' },
      { type: 'value', name: '使用率(%)', max: 150, axisLabel: { formatter: '{value}%' } }
    ],
    series: [
      { name: '预算', type: 'bar', data: budgets, itemStyle: { color: '#409EFF' } },
      { name: '支出', type: 'bar', data: spents, itemStyle: { color: '#FF7043' } },
      { name: '使用率(%)', type: 'line', yAxisIndex: 1, data: utils, itemStyle: { color: '#E6A23C' }, smooth: true }
    ]
  }
  trendChart.setOption(option, true)
}

function getProgressColor(percentage: number): string {
  if (percentage >= 100) return '#F56C6C'
  if (percentage >= 80) return '#E6A23C'
  return '#67C23A'
}
</script>

<style scoped lang="scss">
.budget-trend-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 { margin: 0; }
  }

  .chart-container {
    height: 350px;
  }
}
</style>
