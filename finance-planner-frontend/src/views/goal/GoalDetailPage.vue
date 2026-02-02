<template>
  <div class="goal-detail-page">
    <n-spin :show="loading">
      <PageHeader :title="goal?.title ?? ''" show-back>
        <template #actions>
          <n-button v-if="goal?.status === 1" @click="goToEdit">编辑</n-button>
          <n-button type="error" @click="handleDelete">删除</n-button>
        </template>
      </PageHeader>

      <template v-if="goal">
        <n-grid :x-gap="16" :y-gap="16" :cols="24">
          <n-gi :span="16">
            <GlassCard>
              <template #header>
                <div class="card-header">
                  <span>目标进度</span>
                  <n-tag :type="statusTag(goal.status)" size="small">{{ statusLabel(goal.status) }}</n-tag>
                </div>
              </template>
              <div class="progress-summary">
                <div class="amount-display">
                  <span class="current-amount">¥{{ formatNumber(goal.currentAmount) }}</span>
                  <span class="divider">/</span>
                  <span class="target-amount">¥{{ formatNumber(goal.targetAmount) }}</span>
                </div>
                <n-progress
                  type="line"
                  :percentage="Math.min(100, Number(goal.progressPercentage))"
                  :height="16"
                  :border-radius="8"
                  :color="goal.status === 2 ? 'var(--cr-success)' : 'var(--cr-primary)'"
                />
                <n-grid :x-gap="16" :cols="3" class="stats-row">
                  <n-gi>
                    <div class="stat-item">
                      <div class="stat-label">剩余天数</div>
                      <div class="stat-value">{{ goal.remainingDays }} 天</div>
                    </div>
                  </n-gi>
                  <n-gi>
                    <div class="stat-item">
                      <div class="stat-label">完成进度</div>
                      <div class="stat-value">{{ goal.progressPercentage }}%</div>
                    </div>
                  </n-gi>
                  <n-gi>
                    <div class="stat-item">
                      <div class="stat-label">每月需存</div>
                      <div class="stat-value">¥{{ formatNumber(goal.monthlySavingsNeeded) }}</div>
                    </div>
                  </n-gi>
                </n-grid>
              </div>

              <!-- Progress Chart -->
              <div ref="chartRef" style="width: 100%; height: 300px; margin-top: 20px"></div>
            </GlassCard>

            <!-- Progress History -->
            <GlassCard style="margin-top: 16px">
              <template #header>
                <div class="card-header">
                  <span>存款记录</span>
                </div>
              </template>
              <template #header-extra>
                <n-button v-if="goal.status === 1" type="primary" size="small" @click="showAddProgress = true">
                  <template #icon><n-icon><AddOutline /></n-icon></template>
                  添加存款
                </n-button>
              </template>
              <n-empty v-if="progressHistory.length === 0" description="暂无存款记录" />
              <n-timeline v-else>
                <n-timeline-item
                  v-for="item in progressHistory"
                  :key="item.id"
                  :time="item.recordDate"
                >
                  <div class="progress-item">
                    <span class="progress-amount">+¥{{ formatNumber(item.amount) }}</span>
                    <span class="progress-note" v-if="item.note">{{ item.note }}</span>
                  </div>
                </n-timeline-item>
              </n-timeline>
            </GlassCard>
          </n-gi>

          <n-gi :span="8">
            <GlassCard>
              <template #header>目标详情</template>
              <n-descriptions :column="1" bordered label-placement="left">
                <n-descriptions-item label="优先级">
                  <n-tag :type="priorityTag(goal.priority)" size="small">
                    {{ priorityLabel(goal.priority) }}
                  </n-tag>
                </n-descriptions-item>
                <n-descriptions-item label="截止日期">{{ goal.deadline }}</n-descriptions-item>
                <n-descriptions-item label="创建时间">{{ formatDate(goal.createdAt) }}</n-descriptions-item>
                <n-descriptions-item v-if="goal.description" label="描述">
                  {{ goal.description }}
                </n-descriptions-item>
              </n-descriptions>
            </GlassCard>

            <!-- AI Plan -->
            <GlassCard style="margin-top: 16px">
              <template #header>AI 理财建议</template>
              <n-button
                type="primary"
                :loading="aiLoading"
                @click="handleGenerateAiPlan"
                block
                v-if="!aiPlan"
              >
                生成 AI 理财计划
              </n-button>
              <div v-if="aiPlan" class="ai-plan-content">
                <h4>总结</h4>
                <p>{{ aiPlan.summary }}</p>
                <h4 v-if="aiPlan.steps.length > 0">实施步骤</h4>
                <ol>
                  <li v-for="(step, idx) in aiPlan.steps" :key="idx">{{ step }}</li>
                </ol>
                <h4 v-if="aiPlan.tips.length > 0">小贴士</h4>
                <ul>
                  <li v-for="(tip, idx) in aiPlan.tips" :key="idx">{{ tip }}</li>
                </ul>
                <n-alert :title="aiPlan.riskWarning" type="warning" style="margin-top: 16px" />
              </div>
            </GlassCard>
          </n-gi>
        </n-grid>
      </template>

      <!-- Add Progress Modal -->
      <n-modal v-model:show="showAddProgress" preset="card" title="添加存款记录" style="width: 420px">
        <n-form label-placement="left" label-width="80">
          <n-form-item label="存入金额" required>
            <n-input-number
              v-model:value="progressForm.amount"
              :min="0.01"
              :precision="2"
              :show-button="false"
              style="width: 100%"
            />
          </n-form-item>
          <n-form-item label="日期" required>
            <n-date-picker v-model:value="progressForm.recordDateTs" type="date" style="width: 100%" />
          </n-form-item>
          <n-form-item label="备注">
            <n-input v-model:value="progressForm.note" placeholder="可选备注" />
          </n-form-item>
        </n-form>
        <template #action>
          <n-space justify="end">
            <n-button @click="showAddProgress = false">取消</n-button>
            <n-button type="primary" @click="handleAddProgress" :loading="progressSubmitting">确定</n-button>
          </n-space>
        </template>
      </n-modal>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NSpin, NGrid, NGi, NButton, NIcon, NTag, NProgress, NEmpty,
  NTimeline, NTimelineItem, NDescriptions, NDescriptionsItem,
  NAlert, NModal, NForm, NFormItem, NInput, NInputNumber,
  NDatePicker, NSpace, useMessage, useDialog,
} from 'naive-ui'
import { AddOutline } from '@vicons/ionicons5'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import { getGoal, deleteGoal, addProgress, getProgressHistory, generateAiPlan } from '@/api/goal'
import type { Goal, GoalProgress, GoalAiPlan } from '@/types/goal'
import GlassCard from '@/components/common/GlassCard.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const loading = ref(false)
const goal = ref<Goal | null>(null)
const progressHistory = ref<GoalProgress[]>([])
const showAddProgress = ref(false)
const progressSubmitting = ref(false)
const aiLoading = ref(false)
const aiPlan = ref<GoalAiPlan | null>(null)
const chartRef = ref<HTMLElement>()

const progressForm = reactive({
  amount: undefined as number | undefined,
  recordDateTs: Date.now() as number | null,
  note: ''
})

onMounted(async () => {
  await fetchGoalData()
})

async function fetchGoalData() {
  loading.value = true
  try {
    const goalId = Number(route.params.id)
    const [goalRes, progressRes] = await Promise.all([
      getGoal(goalId),
      getProgressHistory(goalId)
    ])
    if (goalRes.data.code === 200) {
      goal.value = goalRes.data.data
    }
    if (progressRes.data.code === 200) {
      progressHistory.value = progressRes.data.data
      await nextTick()
      renderChart()
    }
  } catch (error) {
    message.error('加载目标详情失败')
  } finally {
    loading.value = false
  }
}

function renderChart() {
  if (!chartRef.value || progressHistory.value.length === 0) return

  const chart = echarts.init(chartRef.value)
  // Build cumulative data from progress records (oldest first)
  const sorted = [...progressHistory.value].reverse()
  let cumulative = 0
  const dates: string[] = []
  const values: number[] = []
  for (const p of sorted) {
    cumulative += Number(p.amount)
    dates.push(p.recordDate)
    values.push(cumulative)
  }

  chart.setOption({
    title: { text: '存款趋势', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis', formatter: '{b}<br/>累计: ¥{c}' },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
    series: [{
      type: 'line',
      data: values,
      smooth: true,
      areaStyle: { opacity: 0.3 },
      markLine: goal.value ? {
        data: [{ yAxis: Number(goal.value.targetAmount), name: '目标', lineStyle: { color: '#E6A23C' } }]
      } : undefined
    }]
  })
}

async function handleAddProgress() {
  if (!progressForm.amount || !progressForm.recordDateTs) {
    message.warning('请填写金额和日期')
    return
  }
  progressSubmitting.value = true
  try {
    await addProgress(Number(route.params.id), {
      amount: progressForm.amount,
      note: progressForm.note || undefined,
      recordDate: dayjs(progressForm.recordDateTs).format('YYYY-MM-DD')
    })
    message.success('存款记录已添加')
    showAddProgress.value = false
    progressForm.amount = undefined
    progressForm.note = ''
    progressForm.recordDateTs = Date.now()
    await fetchGoalData()
  } catch (error) {
    // Handled by interceptor
  } finally {
    progressSubmitting.value = false
  }
}

async function handleGenerateAiPlan() {
  aiLoading.value = true
  try {
    const res = await generateAiPlan(Number(route.params.id))
    if (res.data.code === 200) {
      aiPlan.value = res.data.data
    }
  } catch (error) {
    message.error('AI 计划生成失败')
  } finally {
    aiLoading.value = false
  }
}

async function handleDelete() {
  dialog.warning({
    title: '提示',
    content: '确定要删除这个目标吗？相关存款记录也会被删除。',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteGoal(Number(route.params.id))
        message.success('目标已删除')
        router.push('/goals')
      } catch (error) {
        console.error('Delete error:', error)
      }
    }
  })
}

function goToEdit() {
  router.push(`/goals/edit/${route.params.id}`)
}

function formatNumber(value: number | undefined | null): string {
  if (value === undefined || value === null) return '0.00'
  return Number(value).toFixed(2)
}

function formatDate(dateStr: string): string {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

function priorityLabel(priority: number): string {
  const map: Record<number, string> = { 1: '高', 2: '中', 3: '低' }
  return map[priority] || '中'
}

function priorityTag(priority: number): 'error' | 'warning' | 'info' | 'default' {
  const map: Record<number, 'error' | 'warning' | 'info'> = { 1: 'error', 2: 'warning', 3: 'info' }
  return map[priority] || 'warning'
}

function statusLabel(status: number): string {
  const map: Record<number, string> = { 1: '进行中', 2: '已完成', 3: '已放弃' }
  return map[status] || '未知'
}

function statusTag(status: number): 'default' | 'success' | 'info' {
  const map: Record<number, 'default' | 'success' | 'info'> = { 1: 'default', 2: 'success', 3: 'info' }
  return map[status] || 'default'
}
</script>

<style scoped lang="scss">
.goal-detail-page {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .progress-summary {
    .amount-display {
      margin-bottom: 12px;
      .current-amount {
        font-size: 28px;
        font-weight: bold;
        color: var(--cr-primary);
      }
      .divider {
        margin: 0 8px;
        color: var(--cr-text-tertiary);
        font-size: 20px;
      }
      .target-amount {
        font-size: 18px;
        color: var(--cr-text-secondary);
      }
    }

    .stats-row {
      margin-top: 20px;
      .stat-item {
        text-align: center;
        .stat-label {
          font-size: 13px;
          color: var(--cr-text-secondary);
          margin-bottom: 4px;
        }
        .stat-value {
          font-size: 18px;
          font-weight: bold;
          color: var(--cr-text-primary);
        }
      }
    }
  }

  .progress-item {
    display: flex;
    align-items: center;
    gap: 12px;
    .progress-amount {
      font-weight: bold;
      color: var(--cr-success);
    }
    .progress-note {
      color: var(--cr-text-secondary);
      font-size: 13px;
    }
  }

  .ai-plan-content {
    h4 {
      margin: 16px 0 8px;
      color: var(--cr-text-primary);
      &:first-child { margin-top: 0; }
    }
    p {
      color: var(--cr-text-secondary);
      line-height: 1.6;
    }
    ol, ul {
      padding-left: 20px;
      li {
        margin-bottom: 6px;
        color: var(--cr-text-secondary);
        line-height: 1.5;
      }
    }
  }
}
</style>
