<template>
  <div class="goal-detail-page" v-loading="loading">
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>{{ goal?.title }}</h2>
      </div>
      <div class="header-actions">
        <el-button @click="goToEdit" v-if="goal?.status === 1">编辑</el-button>
        <el-button type="danger" @click="handleDelete">删除</el-button>
      </div>
    </div>

    <template v-if="goal">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card class="progress-card">
            <template #header>
              <div class="card-header">
                <span>目标进度</span>
                <el-tag :type="statusTag(goal.status)">{{ statusLabel(goal.status) }}</el-tag>
              </div>
            </template>
            <div class="progress-summary">
              <div class="amount-display">
                <span class="current-amount">¥{{ formatNumber(goal.currentAmount) }}</span>
                <span class="divider">/</span>
                <span class="target-amount">¥{{ formatNumber(goal.targetAmount) }}</span>
              </div>
              <el-progress
                :percentage="Math.min(100, Number(goal.progressPercentage))"
                :stroke-width="16"
                :color="goal.status === 2 ? '#67C23A' : '#409EFF'"
              />
              <el-row :gutter="20" class="stats-row">
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-label">剩余天数</div>
                    <div class="stat-value">{{ goal.remainingDays }} 天</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-label">完成进度</div>
                    <div class="stat-value">{{ goal.progressPercentage }}%</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-label">每月需存</div>
                    <div class="stat-value">¥{{ formatNumber(goal.monthlySavingsNeeded) }}</div>
                  </div>
                </el-col>
              </el-row>
            </div>

            <!-- Progress Chart -->
            <div ref="chartRef" style="width: 100%; height: 300px; margin-top: 20px"></div>
          </el-card>

          <!-- Progress History -->
          <el-card class="history-card" style="margin-top: 20px">
            <template #header>
              <div class="card-header">
                <span>存款记录</span>
                <el-button type="primary" size="small" @click="showAddProgress = true" v-if="goal.status === 1">
                  <el-icon><Plus /></el-icon>
                  添加存款
                </el-button>
              </div>
            </template>
            <el-empty v-if="progressHistory.length === 0" description="暂无存款记录" />
            <el-timeline v-else>
              <el-timeline-item
                v-for="item in progressHistory"
                :key="item.id"
                :timestamp="item.recordDate"
                placement="top"
              >
                <el-card shadow="never">
                  <div class="progress-item">
                    <span class="progress-amount">+¥{{ formatNumber(item.amount) }}</span>
                    <span class="progress-note" v-if="item.note">{{ item.note }}</span>
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card class="info-card">
            <template #header>目标详情</template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="优先级">
                <el-tag :type="priorityTag(goal.priority)" size="small">
                  {{ priorityLabel(goal.priority) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="截止日期">{{ goal.deadline }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ formatDate(goal.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="描述" v-if="goal.description">
                {{ goal.description }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- AI Plan -->
          <el-card class="ai-card" style="margin-top: 20px">
            <template #header>
              <div class="card-header">
                <span>AI 理财建议</span>
              </div>
            </template>
            <el-button
              type="primary"
              :loading="aiLoading"
              @click="handleGenerateAiPlan"
              style="width: 100%"
              v-if="!aiPlan"
            >
              生成 AI 理财计划
            </el-button>
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
              <el-alert :title="aiPlan.riskWarning" type="warning" :closable="false" show-icon />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- Add Progress Dialog -->
    <el-dialog v-model="showAddProgress" title="添加存款记录" width="400px">
      <el-form :model="progressForm" label-width="80px">
        <el-form-item label="存入金额" required>
          <el-input-number
            v-model="progressForm.amount"
            :min="0.01"
            :precision="2"
            :controls="false"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="日期" required>
          <el-date-picker v-model="progressForm.recordDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="progressForm.note" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddProgress = false">取消</el-button>
        <el-button type="primary" @click="handleAddProgress" :loading="progressSubmitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import { getGoal, deleteGoal, addProgress, getProgressHistory, generateAiPlan } from '@/api/goal'
import type { Goal, GoalProgress, GoalAiPlan } from '@/types/goal'

const route = useRoute()
const router = useRouter()

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
  recordDate: new Date() as string | Date,
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
    ElMessage.error('加载目标详情失败')
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
  if (!progressForm.amount || !progressForm.recordDate) {
    ElMessage.warning('请填写金额和日期')
    return
  }
  progressSubmitting.value = true
  try {
    await addProgress(Number(route.params.id), {
      amount: progressForm.amount,
      note: progressForm.note || undefined,
      recordDate: dayjs(progressForm.recordDate).format('YYYY-MM-DD')
    })
    ElMessage.success('存款记录已添加')
    showAddProgress.value = false
    progressForm.amount = undefined
    progressForm.note = ''
    progressForm.recordDate = new Date()
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
    ElMessage.error('AI 计划生成失败')
  } finally {
    aiLoading.value = false
  }
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除这个目标吗？相关存款记录也会被删除。', '提示', {
      type: 'warning'
    })
    await deleteGoal(Number(route.params.id))
    ElMessage.success('目标已删除')
    router.push('/goals')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Delete error:', error)
    }
  }
}

function goToEdit() {
  router.push(`/goals/edit/${route.params.id}`)
}

function goBack() {
  router.push('/goals')
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

function priorityTag(priority: number): '' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<number, '' | 'danger' | 'warning' | 'info'> = { 1: 'danger', 2: 'warning', 3: 'info' }
  return map[priority] || 'warning'
}

function statusLabel(status: number): string {
  const map: Record<number, string> = { 1: '进行中', 2: '已完成', 3: '已放弃' }
  return map[status] || '未知'
}

function statusTag(status: number): '' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<number, '' | 'success' | 'info'> = { 1: '', 2: 'success', 3: 'info' }
  return map[status] || ''
}
</script>

<style scoped lang="scss">
.goal-detail-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;
      h2 { margin: 0; }
    }

    .header-actions {
      display: flex;
      gap: 8px;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .progress-card {
    .progress-summary {
      .amount-display {
        margin-bottom: 12px;
        .current-amount {
          font-size: 28px;
          font-weight: bold;
          color: #409EFF;
        }
        .divider {
          margin: 0 8px;
          color: #ccc;
          font-size: 20px;
        }
        .target-amount {
          font-size: 18px;
          color: #999;
        }
      }

      .stats-row {
        margin-top: 20px;
        .stat-item {
          text-align: center;
          .stat-label {
            font-size: 13px;
            color: #999;
            margin-bottom: 4px;
          }
          .stat-value {
            font-size: 18px;
            font-weight: bold;
            color: #333;
          }
        }
      }
    }
  }

  .history-card {
    .progress-item {
      display: flex;
      align-items: center;
      gap: 12px;
      .progress-amount {
        font-weight: bold;
        color: #66BB6A;
      }
      .progress-note {
        color: #999;
        font-size: 13px;
      }
    }
  }

  .ai-plan-content {
    h4 {
      margin: 16px 0 8px;
      color: #333;
      &:first-child { margin-top: 0; }
    }
    p {
      color: #666;
      line-height: 1.6;
    }
    ol, ul {
      padding-left: 20px;
      li {
        margin-bottom: 6px;
        color: #666;
        line-height: 1.5;
      }
    }
    .el-alert {
      margin-top: 16px;
    }
  }
}
</style>
