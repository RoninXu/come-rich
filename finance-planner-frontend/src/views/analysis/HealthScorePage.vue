<template>
  <div class="health-score-page">
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
              />
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
              />
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
              />
            </div>
            <div class="breakdown-item">
              <div class="item-header">
                <span class="name">消费结构</span>
                <span class="score">{{ healthScore.consumptionStructure }}/15</span>
              </div>
              <el-progress
                :percentage="(healthScore.consumptionStructure / 15) * 100"
                :stroke-width="8"
                :show-text="false"
              />
            </div>
            <div class="breakdown-item">
              <div class="item-header">
                <span class="name">记账习惯</span>
                <span class="score">{{ healthScore.recordingHabit }}/15</span>
              </div>
              <el-progress
                :percentage="(healthScore.recordingHabit / 15) * 100"
                :stroke-width="8"
                :show-text="false"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="advice-card">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'

const gaugeRef = ref<HTMLElement>()
let gaugeChart: echarts.ECharts | null = null

const healthScore = reactive({
  totalScore: 0,
  savingAbility: 0,
  balanceRatio: 0,
  assetGrowth: 0,
  consumptionStructure: 0,
  recordingHabit: 0
})

const scoreLevel = computed(() => {
  const score = healthScore.totalScore
  if (score >= 90) return '优秀'
  if (score >= 70) return '良好'
  if (score >= 50) return '一般'
  return '较差'
})

const adviceList = ref([
  {
    type: 'warning' as const,
    title: '提高储蓄率',
    description: '建议将月储蓄率提高到30%以上，可以通过减少非必要支出来实现。'
  },
  {
    type: 'info' as const,
    title: '坚持记账',
    description: '养成每日记账的习惯，有助于更好地了解自己的消费模式。'
  },
  {
    type: 'success' as const,
    title: '收支平衡良好',
    description: '您的收支比例控制得很好，继续保持！'
  }
])

onMounted(() => {
  fetchHealthScore()
})

function fetchHealthScore() {
  // TODO: Fetch from API

  // Mock data
  healthScore.totalScore = 72
  healthScore.savingAbility = 20
  healthScore.balanceRatio = 18
  healthScore.assetGrowth = 10
  healthScore.consumptionStructure = 12
  healthScore.recordingHabit = 12

  nextTick(() => {
    initGaugeChart()
  })
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
