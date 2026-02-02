<template>
  <div class="assessment-history-page">
    <n-spin :show="loading">
      <PageHeader title="评估历史" show-back />

      <GlassCard>
        <n-empty v-if="!loading && history.length === 0" description="暂无评估记录">
          <template #extra>
            <n-button type="primary" @click="router.push('/investment/quiz')">开始风险评估</n-button>
          </template>
        </n-empty>

        <n-timeline v-else>
          <n-timeline-item
            v-for="item in history"
            :key="item.id"
            :time="item.assessmentDate"
            :color="getTimelineColor(item.riskLevel)"
          >
            <GlassCard hoverable>
              <div class="history-item">
                <div class="item-info">
                  <span class="risk-level" :class="getRiskClass(item.riskLevel)">{{ item.riskLevel }}</span>
                  <span class="risk-score">得分：{{ item.riskScore }} / 32</span>
                </div>
                <n-progress
                  type="line"
                  :percentage="(item.riskScore / 32) * 100"
                  :color="getTimelineColor(item.riskLevel)"
                  :height="10"
                  :border-radius="5"
                  style="width: 200px"
                />
              </div>
            </GlassCard>
          </n-timeline-item>
        </n-timeline>
      </GlassCard>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NSpin, NTimeline, NTimelineItem, NProgress, NEmpty, NButton, useMessage } from 'naive-ui'
import { getAssessmentHistory } from '@/api/investment'
import type { RiskAssessment } from '@/types/investment'
import GlassCard from '@/components/common/GlassCard.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const history = ref<RiskAssessment[]>([])

onMounted(() => { fetchHistory() })

async function fetchHistory() {
  loading.value = true
  try {
    const res = await getAssessmentHistory()
    if (res.data.code === 200) history.value = res.data.data || []
  } catch { message.error('加载评估历史失败') }
  finally { loading.value = false }
}

function getRiskClass(level: string): string {
  if (level === '保守型') return 'conservative'
  if (level === '稳健型') return 'moderate'
  return 'aggressive'
}

function getTimelineColor(level: string): string {
  if (level === '保守型') return '#34C759'
  if (level === '稳健型') return '#FF9500'
  return '#FF3B30'
}
</script>

<style scoped lang="scss">
.assessment-history-page {
  max-width: 800px;
  margin: 0 auto;

  .history-item {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .item-info {
      .risk-level {
        font-size: 18px;
        font-weight: 700;
        margin-right: 16px;

        &.conservative { color: var(--cr-success); }
        &.moderate { color: var(--cr-warning); }
        &.aggressive { color: var(--cr-error); }
      }

      .risk-score {
        color: var(--cr-text-secondary);
        font-size: 14px;
      }
    }
  }
}
</style>
