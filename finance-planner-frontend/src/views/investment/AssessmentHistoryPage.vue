<template>
  <div class="assessment-history-page" v-loading="loading">
    <div class="page-header">
      <h2>评估历史</h2>
      <el-button @click="router.back()">返回</el-button>
    </div>

    <el-card>
      <el-empty v-if="!loading && history.length === 0" description="暂无评估记录">
        <el-button type="primary" @click="router.push('/investment/quiz')">开始风险评估</el-button>
      </el-empty>

      <el-timeline v-else>
        <el-timeline-item
          v-for="item in history"
          :key="item.id"
          :timestamp="item.assessmentDate"
          placement="top"
          :color="getTimelineColor(item.riskLevel)"
        >
          <el-card shadow="hover">
            <div class="history-item">
              <div class="item-info">
                <span class="risk-level" :class="getRiskClass(item.riskLevel)">{{ item.riskLevel }}</span>
                <span class="risk-score">得分：{{ item.riskScore }} / 32</span>
              </div>
              <el-progress
                :percentage="(item.riskScore / 32) * 100"
                :color="getTimelineColor(item.riskLevel)"
                :stroke-width="10"
                :format="() => item.riskScore + '/32'"
                style="width: 200px"
              />
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAssessmentHistory } from '@/api/investment'
import type { RiskAssessment } from '@/types/investment'

const router = useRouter()
const loading = ref(false)
const history = ref<RiskAssessment[]>([])

onMounted(() => {
  fetchHistory()
})

async function fetchHistory() {
  loading.value = true
  try {
    const res = await getAssessmentHistory()
    if (res.data.code === 200) {
      history.value = res.data.data || []
    }
  } catch {
    ElMessage.error('加载评估历史失败')
  } finally {
    loading.value = false
  }
}

function getRiskClass(level: string): string {
  if (level === '保守型') return 'conservative'
  if (level === '稳健型') return 'moderate'
  return 'aggressive'
}

function getTimelineColor(level: string): string {
  if (level === '保守型') return '#67C23A'
  if (level === '稳健型') return '#E6A23C'
  return '#F56C6C'
}
</script>

<style scoped lang="scss">
.assessment-history-page {
  max-width: 800px;
  margin: 0 auto;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    h2 { margin: 0; }
  }

  .history-item {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .item-info {
      .risk-level {
        font-size: 18px;
        font-weight: bold;
        margin-right: 16px;

        &.conservative { color: #67C23A; }
        &.moderate { color: #E6A23C; }
        &.aggressive { color: #F56C6C; }
      }

      .risk-score {
        color: #999;
        font-size: 14px;
      }
    }
  }
}
</style>
