<template>
  <div class="health-score-page" v-loading="loading">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="score-card">
          <div class="score-display">
            <div ref="gaugeRef" class="gauge-chart"></div>
            <div class="score-text">
              <span class="score">{{ healthScore.totalScore }}</span>
              <span class="label">{{ scoreLevel }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="breakdown-card">
          <template #header>评分明细</template>
          <div class="breakdown-list">
            <div class="breakdown-item">
              <div class="item-header">
                <span class="name">储蓄能力</span>
                <span class="score">{{ healthScore.savingAbility }}/30</span>
              </div>
              <el-progress
                :percentage="(healthScore.savingAbility / 30) * 100"
                :stroke-width="8"
                :show-text="false"
                :color="getProgressColor(healthScore.savingDetail?.status)"
              />
              <div v-if="healthScore.savingDetail" class="item-desc">{{ healthScore.savingDetail.description }}</div>
            </div>
            <div class="breakdown-item">
              <div class="item-header">
                <span class="name">收支平衡</span>
                <span class="score">{{ healthScore.balanceRatio }}/25</span>
              </div>
              <el-progress
                :percentage="(healthScore.balanceRatio / 25) * 100"
                :stroke-width="8"
                :show-text="false"
                :color="getProgressColor(healthScore.balanceDetail?.status)"
              />
              <div v-if="healthScore.balanceDetail" class="item-desc">{{ healthScore.balanceDetail.description }}</div>
            </div>
            <div class="breakdown-item">
              <div class="item-header">
                <span class="name">消费结构</span>
                <span class="score">{{ healthScore.consumptionStructure }}/20</span>
              </div>
              <el-progress
                :percentage="(healthScore.consumptionStructure / 20) * 100"
                :stroke-width="8"
                :show-text="false"
                :color="getProgressColor(healthScore.consumptionDetail?.status)"
              />
              <div v-if="healthScore.consumptionDetail" class="item-desc">{{ healthScore.consumptionDetail.description }}</div>
            </div>
            <div class="breakdown-item">
              <div class="item-header">
                <span class="name">资产增长</span>
                <span class="score">{{ healthScore.assetGrowth }}/15</span>
              </div>
              <el-progress
                :percentage="(healthScore.assetGrowth / 15) * 100"
                :stroke-width="8"
                :show-text="false"
                :color="getProgressColor(healthScore.growthDetail?.status)"
              />
              <div v-if="healthScore.growthDetail" class="item-desc">{{ healthScore.growthDetail.description }}</div>
            </div>
            <div class="breakdown-item">
              <div class="item-header">
                <span class="name">记账习惯</span>
                <span class="score">{{ healthScore.recordingHabit }}/10</span>
              </div>
              <el-progress
                :percentage="(healthScore.recordingHabit / 10) * 100"
                :stroke-width="8"
                :show-text="false"
                :color="getProgressColor(healthScore.habitDetail?.status)"
              />
              <div v-if="healthScore.habitDetail" class="item-desc">{{ healthScore.habitDetail.description }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="adviceList.length > 0" class="advice-card">
      <template #header>改进建议</template>
      <div class="advice-list">
        <el-alert
          v-for="(advice, index) in adviceList"
          :key="index"
          :title="advice.title"
          :description="advice.description"
          :type="advice.type"
          :closable="false"
          show-icon
          class="advice-item"
        />
      </div>
    </el-card>

    <el-card v-else class="advice-card">
      <template #header>改进建议</template>
      <el-empty description="暂无建议，请先记录一些交易数据" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getHealthScore } from '@/api/analysis'
import type { HealthScore, ScoreDetail } from '@/types/analysis'

const gaugeRef = ref<HTMLElement>()
let gaugeChart: echarts.ECharts | null = null
const loading = ref(false)

const healthScore = reactive({
  totalScore: 0,
  grade: '',
  savingAbility: 0,
  balanceRatio: 0,
  assetGrowth: 0,
  consumptionStructure: 0,
  recordingHabit: 0,
  savingDetail: null as ScoreDetail | null,
  balanceDetail: null as ScoreDetail | null,
  consumptionDetail: null as ScoreDetail | null,
  growthDetail: null as ScoreDetail | null,
  habitDetail: null as ScoreDetail | null,
  suggestions: [] as string[]
})

const scoreLevel = computed(() => {
  if (healthScore.grade) return healthScore.grade
  const score = healthScore.totalScore
  if (score >= 90) return '优秀'
  if (score >= 70) return '良好'
  if (score >= 50) return '一般'
  return '较差'
})

interface Advice {
  type: 'warning' | 'info' | 'success'
  title: string
  description: string
}

const adviceList = computed<Advice[]>(() => {
  if (healthScore.suggestions.length > 0) {
    return healthScore.suggestions.map((suggestion, index) => ({
      type: index === 0 ? 'warning' : 'info',
      title: `建议 ${index + 1}`,
      description: suggestion
    }))
  }
  return []
})

onMounted(() => {
  fetchHealthScoreData()
})

async function fetchHealthScoreData() {
  loading.value = true
  try {
    const res = await getHealthScore()
    if (res.data.code === 200 && res.data.data) {
      const data = res.data.data
      healthScore.totalScore = data.totalScore
      healthScore.grade = data.grade
      healthScore.savingAbility = data.savingAbility
      healthScore.balanceRatio = data.balanceRatio
      healthScore.consumptionStructure = data.consumptionStructure
      healthScore.assetGrowth = data.assetGrowth
      healthScore.recordingHabit = data.recordingHabit
      healthScore.savingDetail = data.savingDetail
      healthScore.balanceDetail = data.balanceDetail
      healthScore.consumptionDetail = data.consumptionDetail
      healthScore.growthDetail = data.growthDetail
      healthScore.habitDetail = data.habitDetail
      healthScore.suggestions = data.suggestions || []
    }

    nextTick(() => {
      initGaugeChart()
    })
  } catch (error) {
    ElMessage.error('加载健康评分失败')
  } finally {
    loading.value = false
  }
}

function initGaugeChart() {
  if (!gaugeRef.value) return

  gaugeChart = echarts.init(gaugeRef.value)

  const option: echarts.EChartsOption = {
    series: [
      {
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max: 100,
        splitNumber: 10,
        axisLine: {
          lineStyle: {
            width: 20,
            color: [
              [0.5, '#FF6B6B'],
              [0.7, '#FFA726'],
              [0.9, '#66BB6A'],
              [1, '#43A047']
            ]
          }
        },
        pointer: {
          itemStyle: {
            color: '#333'
          }
        },
        axisTick: {
          show: false
        },
        splitLine: {
          show: false
        },
        axisLabel: {
          show: false
        },
        detail: {
          show: false
        },
        data: [
          {
            value: healthScore.totalScore
          }
        ]
      }
    ]
  }

  gaugeChart.setOption(option)
}

function getProgressColor(status?: string): string {
  switch (status) {
    case 'good':
      return '#66BB6A'
    case 'average':
      return '#FFA726'
    case 'poor':
      return '#FF6B6B'
    default:
      return '#409EFF'
  }
}
</script>

<style scoped lang="scss">
.health-score-page {
  .score-card {
    .score-display {
      position: relative;
      display: flex;
      flex-direction: column;
      align-items: center;

      .gauge-chart {
        width: 100%;
        height: 200px;
      }

      .score-text {
        position: absolute;
        bottom: 30px;
        text-align: center;

        .score {
          display: block;
          font-size: 48px;
          font-weight: bold;
          color: #333;
        }

        .label {
          font-size: 16px;
          color: #999;
        }
      }
    }
  }

  .breakdown-card {
    .breakdown-list {
      .breakdown-item {
        margin-bottom: 20px;

        &:last-child {
          margin-bottom: 0;
        }

        .item-header {
          display: flex;
          justify-content: space-between;
          margin-bottom: 8px;

          .name {
            color: #333;
          }

          .score {
            color: #999;
          }
        }

        .item-desc {
          font-size: 12px;
          color: #999;
          margin-top: 4px;
        }
      }
    }
  }

  .advice-card {
    margin-top: 20px;

    .advice-list {
      .advice-item {
        margin-bottom: 12px;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}
</style>
