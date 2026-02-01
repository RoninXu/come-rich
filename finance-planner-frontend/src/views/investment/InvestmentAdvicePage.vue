<template>
  <div class="investment-advice-page" v-loading="loading">
    <div class="page-header">
      <h2>投资建议</h2>
      <div style="display: flex; gap: 8px">
        <el-button @click="router.push('/investment/quiz')">
          {{ hasAssessment ? '重新评估' : '开始评估' }}
        </el-button>
        <el-button v-if="hasAssessment" type="primary" @click="handleGenerate" :loading="generating">
          生成AI建议
        </el-button>
      </div>
    </div>

    <!-- No assessment -->
    <el-card v-if="!hasAssessment && !loading">
      <el-empty description="您还没有完成风险评估">
        <el-button type="primary" @click="router.push('/investment/quiz')">开始风险评估</el-button>
      </el-empty>
    </el-card>

    <!-- Risk profile card -->
    <el-card v-if="hasAssessment" class="profile-card">
      <template #header>我的风险画像</template>
      <div class="profile-content">
        <div class="score-badge" :class="riskClass">
          <span class="score">{{ assessment!.riskScore }}</span>
          <span class="max">/32</span>
        </div>
        <div class="profile-info">
          <div class="risk-level" :class="riskClass">{{ assessment!.riskLevel }}</div>
          <div class="assessment-date">评估日期：{{ assessment!.assessmentDate }}</div>
        </div>
      </div>
    </el-card>

    <el-row v-if="recommendations.length > 0" :gutter="20" style="margin-top: 16px">
      <el-col :span="10">
        <el-card>
          <template #header>资产配置</template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card>
          <template #header>投资方向推荐</template>
          <div class="recommendation-list">
            <el-card
              v-for="rec in recommendations"
              :key="rec.id"
              shadow="hover"
              class="rec-card"
            >
              <div class="rec-header">
                <span class="track-name">{{ rec.trackName }}</span>
                <el-tag size="small">{{ rec.allocationPercentage }}%</el-tag>
              </div>
              <p v-if="rec.description" class="rec-desc">{{ rec.description }}</p>
              <p v-if="rec.rationale" class="rec-rationale">{{ rec.rationale }}</p>
              <div class="rec-meta">
                <span v-if="rec.riskLevel">风险：{{ rec.riskLevel }}</span>
                <span v-if="rec.expectedAnnualReturn">预期年化：{{ rec.expectedAnnualReturn }}</span>
              </div>
            </el-card>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Disclaimers -->
    <el-alert
      v-if="recommendations.length > 0"
      title="投资有风险，入市需谨慎。以上建议仅供参考，不构成具体投资建议。本平台不推荐具体金融产品，仅提供投资类别方向参考。"
      type="warning"
      :closable="false"
      show-icon
      style="margin-top: 16px"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  getLatestAssessment,
  getActiveRecommendations,
  getAssetAllocation,
  generateRecommendations
} from '@/api/investment'
import type { RiskAssessment, InvestmentRecommendation } from '@/types/investment'

const router = useRouter()
const loading = ref(false)
const generating = ref(false)
const assessment = ref<RiskAssessment | null>(null)
const recommendations = ref<InvestmentRecommendation[]>([])

const pieChartRef = ref<HTMLElement>()
let pieChart: echarts.ECharts | null = null

const hasAssessment = computed(() => assessment.value !== null)
const riskClass = computed(() => {
  if (!assessment.value) return ''
  const level = assessment.value.riskLevel
  if (level === '保守型') return 'conservative'
  if (level === '稳健型') return 'moderate'
  return 'aggressive'
})

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    // Fetch assessment
    try {
      const assessRes = await getLatestAssessment()
      if (assessRes.data.code === 200) {
        assessment.value = assessRes.data.data
      }
    } catch {
      assessment.value = null
    }

    // Fetch recommendations
    if (assessment.value) {
      try {
        const recRes = await getActiveRecommendations()
        if (recRes.data.code === 200) {
          recommendations.value = recRes.data.data || []
        }
      } catch {
        recommendations.value = []
      }
    }

    if (recommendations.value.length > 0) {
      nextTick(() => updatePieChart())
    }
  } finally {
    loading.value = false
  }
}

async function handleGenerate() {
  generating.value = true
  try {
    const res = await generateRecommendations()
    if (res.data.code === 200 && res.data.data) {
      recommendations.value = res.data.data.recommendations || []
      ElMessage.success('投资建议已生成')
      nextTick(() => updatePieChart())
    }
  } catch {
    ElMessage.error('生成建议失败')
  } finally {
    generating.value = false
  }
}

async function updatePieChart() {
  if (!pieChartRef.value) return
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }

  // Try to get allocation data from API
  let chartData: { name: string; value: number; itemStyle?: { color: string } }[] = []
  try {
    const alloRes = await getAssetAllocation()
    if (alloRes.data.code === 200 && alloRes.data.data?.tracks) {
      chartData = alloRes.data.data.tracks.map(t => ({
        name: t.name,
        value: Number(t.percentage),
        itemStyle: t.color ? { color: t.color } : undefined
      }))
    }
  } catch {
    // Fallback to recommendations data
    chartData = recommendations.value.map(r => ({
      name: r.trackName,
      value: Number(r.allocationPercentage)
    }))
  }

  const option: echarts.EChartsOption = {
    tooltip: { trigger: 'item', formatter: '{b}: {c}% ({d}%)' },
    legend: { orient: 'vertical', right: 10, top: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      data: chartData.length > 0 ? chartData : [{ value: 0, name: '暂无数据' }]
    }]
  }
  pieChart.setOption(option, true)
}
</script>

<style scoped lang="scss">
.investment-advice-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    h2 { margin: 0; }
  }

  .profile-card {
    .profile-content {
      display: flex;
      align-items: center;
      gap: 24px;

      .score-badge {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: #fff;

        &.conservative { background: linear-gradient(135deg, #67C23A, #43A047); }
        &.moderate { background: linear-gradient(135deg, #E6A23C, #F57C00); }
        &.aggressive { background: linear-gradient(135deg, #F56C6C, #D32F2F); }

        .score { font-size: 24px; font-weight: bold; }
        .max { font-size: 12px; opacity: 0.8; }
      }

      .profile-info {
        .risk-level {
          font-size: 20px;
          font-weight: bold;
          margin-bottom: 4px;

          &.conservative { color: #67C23A; }
          &.moderate { color: #E6A23C; }
          &.aggressive { color: #F56C6C; }
        }

        .assessment-date { color: #999; font-size: 14px; }
      }
    }
  }

  .chart-container { height: 300px; }

  .recommendation-list {
    .rec-card {
      margin-bottom: 12px;

      .rec-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .track-name { font-size: 16px; font-weight: bold; }
      }

      .rec-desc { color: #666; font-size: 14px; margin: 4px 0; }
      .rec-rationale { color: #999; font-size: 13px; margin: 4px 0; }

      .rec-meta {
        display: flex;
        gap: 16px;
        color: #999;
        font-size: 12px;
        margin-top: 8px;
      }
    }
  }
}
</style>
