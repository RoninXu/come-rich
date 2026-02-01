<template>
  <div class="monthly-report-page" v-loading="loading">
    <div class="page-header">
      <h2>月度报表</h2>
      <div style="display: flex; gap: 8px; align-items: center">
        <el-date-picker
          v-model="selectedMonth"
          type="month"
          placeholder="选择月份"
          format="YYYY年MM月"
          value-format="YYYY-MM"
          @change="fetchData"
        />
        <el-button @click="handleExportReport" :loading="exporting">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
      </div>
    </div>

    <el-row :gutter="20" class="summary-cards">
      <el-col :span="6">
        <el-card class="summary-card income">
          <div class="card-title">总收入</div>
          <div class="card-value">¥ {{ summary.totalIncome.toFixed(2) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card expense">
          <div class="card-title">总支出</div>
          <div class="card-value">¥ {{ summary.totalExpense.toFixed(2) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card balance">
          <div class="card-title">结余</div>
          <div class="card-value">¥ {{ summary.balance.toFixed(2) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="summary-card savings">
          <div class="card-title">储蓄率</div>
          <div class="card-value">{{ summary.savingsRate.toFixed(1) }}%</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>支出分类统计</template>
          <div ref="expensePieRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>每日收支趋势</template>
          <div ref="dailyTrendRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { getMonthlySummary, getCategoryStats, getDailyStats } from '@/api/analysis'
import { exportMonthlyReport } from '@/api/export'
import { downloadBlob } from '@/utils/export'
import type { MonthlySummary, CategoryStat, DailyStat } from '@/types/analysis'

const selectedMonth = ref(dayjs().format('YYYY-MM'))
const loading = ref(false)
const exporting = ref(false)

const summary = reactive({
  totalIncome: 0,
  totalExpense: 0,
  balance: 0,
  savingsRate: 0
})

const categoryStats = ref<CategoryStat[]>([])
const dailyStats = ref<DailyStat[]>([])

const expensePieRef = ref<HTMLElement>()
const dailyTrendRef = ref<HTMLElement>()

let expensePieChart: echarts.ECharts | null = null
let dailyTrendChart: echarts.ECharts | null = null

onMounted(() => {
  initCharts()
  fetchData()
})

function initCharts() {
  nextTick(() => {
    if (expensePieRef.value) {
      expensePieChart = echarts.init(expensePieRef.value)
    }
    if (dailyTrendRef.value) {
      dailyTrendChart = echarts.init(dailyTrendRef.value)
    }
  })
}

async function fetchData() {
  loading.value = true
  try {
    const [year, month] = selectedMonth.value.split('-').map(Number)

    // Fetch all data in parallel
    const [summaryRes, categoryRes, dailyRes] = await Promise.all([
      getMonthlySummary(year, month),
      getCategoryStats(year, month, 2), // type 2 = expense
      getDailyStats(year, month)
    ])

    // Update summary
    if (summaryRes.data.code === 200 && summaryRes.data.data) {
      const data = summaryRes.data.data
      summary.totalIncome = data.totalIncome || 0
      summary.totalExpense = data.totalExpense || 0
      summary.balance = data.balance || 0
      summary.savingsRate = data.savingsRate || 0
    }

    // Update category stats
    if (categoryRes.data.code === 200 && categoryRes.data.data) {
      categoryStats.value = categoryRes.data.data
    }

    // Update daily stats
    if (dailyRes.data.code === 200 && dailyRes.data.data) {
      dailyStats.value = dailyRes.data.data
    }

    // Update charts after data is loaded
    nextTick(() => {
      updateExpensePieChart()
      updateDailyTrendChart()
    })
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

async function handleExportReport() {
  exporting.value = true
  try {
    const [year, month] = selectedMonth.value.split('-').map(Number)
    const res = await exportMonthlyReport(year, month)
    downloadBlob(new Blob([res.data]), `月度报表_${selectedMonth.value}.xlsx`)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

function updateExpensePieChart() {
  if (!expensePieChart) return

  // Transform category stats to chart data
  const chartData = categoryStats.value.map(stat => ({
    value: Number(stat.amount),
    name: stat.categoryName,
    itemStyle: stat.categoryColor ? { color: stat.categoryColor } : undefined
  }))

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        data: chartData.length > 0 ? chartData : [{ value: 0, name: '暂无数据' }]
      }
    ]
  }

  expensePieChart.setOption(option, true)
}

function updateDailyTrendChart() {
  if (!dailyTrendChart) return

  // Transform daily stats to chart data
  const dates = dailyStats.value.map(stat => {
    const day = dayjs(stat.date).date()
    return `${day}日`
  })
  const incomeData = dailyStats.value.map(stat => Number(stat.income))
  const expenseData = dailyStats.value.map(stat => Number(stat.expense))

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['收入', '支出']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates.length > 0 ? dates : ['暂无数据']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '收入',
        type: 'line',
        smooth: true,
        data: incomeData,
        itemStyle: { color: '#66BB6A' }
      },
      {
        name: '支出',
        type: 'line',
        smooth: true,
        data: expenseData,
        itemStyle: { color: '#FF7043' }
      }
    ]
  }

  dailyTrendChart.setOption(option, true)
}
</script>

<style scoped lang="scss">
.monthly-report-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
    }
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

    &.income .card-value {
      color: #66BB6A;
    }

    &.expense .card-value {
      color: #FF7043;
    }

    &.balance .card-value {
      color: #42A5F5;
    }

    &.savings .card-value {
      color: #AB47BC;
    }
  }

  .chart-card {
    .chart-container {
      height: 300px;
    }
  }
}
</style>
