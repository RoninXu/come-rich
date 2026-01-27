<template>
  <div class="monthly-report-page">
    <div class="page-header">
      <h2>月度报表</h2>
      <el-date-picker
        v-model="selectedMonth"
        type="month"
        placeholder="选择月份"
        format="YYYY年MM月"
        value-format="YYYY-MM"
        @change="fetchData"
      />
    </div>

    <el-row :gutter="20" class="summary-cards">
      <el-col :span="8">
        <el-card class="summary-card income">
          <div class="card-title">总收入</div>
          <div class="card-value">¥ {{ summary.totalIncome.toFixed(2) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="summary-card expense">
          <div class="card-title">总支出</div>
          <div class="card-value">¥ {{ summary.totalExpense.toFixed(2) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="summary-card balance">
          <div class="card-title">结余</div>
          <div class="card-value">¥ {{ (summary.totalIncome - summary.totalExpense).toFixed(2) }}</div>
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

const selectedMonth = ref(dayjs().format('YYYY-MM'))

const summary = reactive({
  totalIncome: 0,
  totalExpense: 0
})

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

function fetchData() {
  // TODO: Fetch data from API

  // Mock data
  summary.totalIncome = 15000
  summary.totalExpense = 8500

  updateExpensePieChart()
  updateDailyTrendChart()
}

function updateExpensePieChart() {
  if (!expensePieChart) return

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
        data: [
          { value: 3000, name: '餐饮' },
          { value: 1500, name: '交通' },
          { value: 2000, name: '购物' },
          { value: 1200, name: '居住' },
          { value: 800, name: '娱乐' }
        ]
      }
    ]
  }

  expensePieChart.setOption(option)
}

function updateDailyTrendChart() {
  if (!dailyTrendChart) return

  const days = Array.from({ length: 30 }, (_, i) => `${i + 1}日`)

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
      data: days
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '收入',
        type: 'line',
        smooth: true,
        data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 500)),
        itemStyle: { color: '#66BB6A' }
      },
      {
        name: '支出',
        type: 'line',
        smooth: true,
        data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 400)),
        itemStyle: { color: '#FF7043' }
      }
    ]
  }

  dailyTrendChart.setOption(option)
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
  }

  .chart-card {
    .chart-container {
      height: 300px;
    }
  }
}
</style>
