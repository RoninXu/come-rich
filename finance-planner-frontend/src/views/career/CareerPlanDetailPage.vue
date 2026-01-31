<template>
  <div class="career-plan-detail-page" v-loading="loading">
    <div class="page-header">
      <div class="header-left">
        <el-button link @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>{{ plan?.title }}</h2>
        <el-tag v-if="plan" :type="statusTag(plan.status)" size="small">
          {{ statusLabel(plan.status) }}
        </el-tag>
      </div>
      <div class="header-actions" v-if="plan">
        <el-select v-model="plan.status" size="small" @change="handleStatusChange" style="width: 120px">
          <el-option :value="1" label="探索中" />
          <el-option :value="2" label="进行中" />
          <el-option :value="3" label="已暂停" />
          <el-option :value="4" label="已完成" />
        </el-select>
        <el-button type="danger" @click="handleDelete">删除</el-button>
      </div>
    </div>

    <template v-if="plan">
      <el-row :gutter="20">
        <el-col :span="16">
          <!-- Income Overview -->
          <el-card class="income-card">
            <template #header>
              <div class="card-header">
                <span>收入概览</span>
                <el-button type="primary" size="small" @click="showAddIncome = true">
                  <el-icon><Plus /></el-icon>
                  记录收入
                </el-button>
              </div>
            </template>
            <el-row :gutter="20" class="stats-row">
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-label">目标月收入</div>
                  <div class="stat-value">¥{{ formatNumber(plan.targetMonthlyIncome) }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-label">本月实际收入</div>
                  <div class="stat-value income">¥{{ formatNumber(plan.actualMonthlyIncome) }}</div>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="stat-item">
                  <div class="stat-label">完成度</div>
                  <div class="stat-value">{{ incomeProgress }}%</div>
                </div>
              </el-col>
            </el-row>
            <el-progress
              :percentage="Math.min(100, incomeProgress)"
              :stroke-width="12"
              :color="incomeProgress >= 100 ? '#67C23A' : '#409EFF'"
              style="margin-top: 16px"
            />

            <!-- Income Chart -->
            <div ref="chartRef" style="width: 100%; height: 300px; margin-top: 20px"></div>
          </el-card>

          <!-- Income History -->
          <el-card class="history-card" style="margin-top: 20px">
            <template #header>
              <span>收入记录</span>
            </template>
            <el-empty v-if="incomeHistory.length === 0" description="暂无收入记录" />
            <el-table v-else :data="incomeHistory" stripe>
              <el-table-column prop="incomeDate" label="日期" width="120" />
              <el-table-column prop="amount" label="金额" width="120">
                <template #default="{ row }">
                  <span class="amount-text">+¥{{ Number(row.amount).toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述">
                <template #default="{ row }">
                  {{ row.description || '-' }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <el-col :span="8">
          <!-- Plan Info -->
          <el-card class="info-card">
            <template #header>计划详情</template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="副业类型" v-if="plan.careerType">
                {{ plan.careerType }}
              </el-descriptions-item>
              <el-descriptions-item label="匹配度" v-if="plan.matchScore">
                <el-progress
                  :percentage="plan.matchScore"
                  :stroke-width="6"
                  :color="scoreColor(plan.matchScore)"
                  style="width: 100px"
                />
              </el-descriptions-item>
              <el-descriptions-item label="开始日期" v-if="plan.startDate">
                {{ plan.startDate }}
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDate(plan.createdAt) }}
              </el-descriptions-item>
              <el-descriptions-item label="描述" v-if="plan.description">
                {{ plan.description }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- AI Startup Plan -->
          <el-card class="ai-card" style="margin-top: 20px">
            <template #header>
              <div class="card-header">
                <span>AI 90天启动计划</span>
              </div>
            </template>
            <el-button
              type="primary"
              :loading="aiLoading"
              @click="handleGenerateStartupPlan"
              style="width: 100%"
              v-if="!plan.startupPlan"
            >
              生成启动计划
            </el-button>
            <div v-if="plan.startupPlan" class="startup-plan-content" v-html="renderMarkdown(plan.startupPlan)"></div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- Add Income Dialog -->
    <el-dialog v-model="showAddIncome" title="记录收入" width="400px">
      <el-form :model="incomeForm" label-width="80px">
        <el-form-item label="金额" required>
          <el-input-number
            v-model="incomeForm.amount"
            :min="0.01"
            :precision="2"
            :controls="false"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="日期" required>
          <el-date-picker v-model="incomeForm.incomeDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="incomeForm.description" placeholder="收入来源描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddIncome = false">取消</el-button>
        <el-button type="primary" @click="handleAddIncome" :loading="incomeSubmitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import {
  getCareerPlan,
  updateCareerPlan,
  deleteCareerPlan,
  addCareerIncome,
  getCareerIncomeHistory,
  generateStartupPlan
} from '@/api/career'
import type { CareerPlan, CareerIncome } from '@/types/career'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const plan = ref<CareerPlan | null>(null)
const incomeHistory = ref<CareerIncome[]>([])
const showAddIncome = ref(false)
const incomeSubmitting = ref(false)
const aiLoading = ref(false)
const chartRef = ref<HTMLElement>()

const incomeForm = reactive({
  amount: undefined as number | undefined,
  incomeDate: new Date() as string | Date,
  description: ''
})

const incomeProgress = computed(() => {
  if (!plan.value?.targetMonthlyIncome || plan.value.targetMonthlyIncome === 0) return 0
  const actual = plan.value.actualMonthlyIncome || 0
  return Math.round((actual / plan.value.targetMonthlyIncome) * 100)
})

onMounted(async () => {
  await fetchPlanData()
})

async function fetchPlanData() {
  loading.value = true
  try {
    const planId = Number(route.params.id)
    const [planRes, incomeRes] = await Promise.all([
      getCareerPlan(planId),
      getCareerIncomeHistory(planId)
    ])
    if (planRes.data.code === 200) {
      plan.value = planRes.data.data
    }
    if (incomeRes.data.code === 200) {
      incomeHistory.value = incomeRes.data.data
      await nextTick()
      renderChart()
    }
  } catch (error) {
    ElMessage.error('加载计划详情失败')
  } finally {
    loading.value = false
  }
}

function renderChart() {
  if (!chartRef.value || incomeHistory.value.length === 0) return

  const chart = echarts.init(chartRef.value)
  // Group income by month
  const monthlyMap = new Map<string, number>()
  for (const income of incomeHistory.value) {
    const month = dayjs(income.incomeDate).format('YYYY-MM')
    monthlyMap.set(month, (monthlyMap.get(month) || 0) + Number(income.amount))
  }

  const months = Array.from(monthlyMap.keys()).sort()
  const values = months.map(m => monthlyMap.get(m) || 0)

  chart.setOption({
    title: { text: '月度收入趋势', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis', formatter: '{b}<br/>收入: ¥{c}' },
    xAxis: { type: 'category', data: months },
    yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
    series: [{
      type: 'bar',
      data: values,
      itemStyle: { color: '#67C23A', borderRadius: [4, 4, 0, 0] },
      markLine: plan.value?.targetMonthlyIncome ? {
        data: [{
          yAxis: Number(plan.value.targetMonthlyIncome),
          name: '目标',
          lineStyle: { color: '#E6A23C', type: 'dashed' }
        }]
      } : undefined
    }]
  })
}

async function handleAddIncome() {
  if (!incomeForm.amount || !incomeForm.incomeDate) {
    ElMessage.warning('请填写金额和日期')
    return
  }
  incomeSubmitting.value = true
  try {
    await addCareerIncome(Number(route.params.id), {
      amount: incomeForm.amount,
      description: incomeForm.description || undefined,
      incomeDate: dayjs(incomeForm.incomeDate).format('YYYY-MM-DD')
    })
    ElMessage.success('收入已记录')
    showAddIncome.value = false
    incomeForm.amount = undefined
    incomeForm.description = ''
    incomeForm.incomeDate = new Date()
    await fetchPlanData()
  } catch (error) {
    // Handled by interceptor
  } finally {
    incomeSubmitting.value = false
  }
}

async function handleStatusChange(newStatus: number) {
  if (!plan.value) return
  try {
    await updateCareerPlan(plan.value.id, {
      title: plan.value.title,
      careerType: plan.value.careerType || undefined,
      description: plan.value.description || undefined,
      targetMonthlyIncome: plan.value.targetMonthlyIncome || undefined
    })
    ElMessage.success('状态已更新')
  } catch (error) {
    // Handled by interceptor
  }
}

async function handleGenerateStartupPlan() {
  aiLoading.value = true
  try {
    const res = await generateStartupPlan(Number(route.params.id))
    if (res.data.code === 200 && plan.value) {
      plan.value.startupPlan = res.data.data
    }
  } catch (error) {
    ElMessage.error('AI 计划生成失败')
  } finally {
    aiLoading.value = false
  }
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除这个副业计划吗？相关收入记录也会被删除。', '提示', {
      type: 'warning'
    })
    await deleteCareerPlan(Number(route.params.id))
    ElMessage.success('计划已删除')
    router.push('/career/plans')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('Delete error:', error)
    }
  }
}

function renderMarkdown(text: string): string {
  // Simple markdown-like rendering for the startup plan
  return text
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/#{3}\s(.*?)(?:<br>|$)/g, '<h4>$1</h4>')
    .replace(/#{2}\s(.*?)(?:<br>|$)/g, '<h3>$1</h3>')
    .replace(/#{1}\s(.*?)(?:<br>|$)/g, '<h3>$1</h3>')
}

function goBack() {
  router.push('/career/plans')
}

function formatNumber(value: number | null | undefined): string {
  if (value === undefined || value === null) return '0.00'
  return Number(value).toFixed(2)
}

function formatDate(dateStr: string): string {
  return dayjs(dateStr).format('YYYY-MM-DD HH:mm')
}

function statusLabel(status: number): string {
  const map: Record<number, string> = { 1: '探索中', 2: '进行中', 3: '已暂停', 4: '已完成' }
  return map[status] || '未知'
}

function statusTag(status: number): '' | 'success' | 'warning' | 'danger' | 'info' {
  const map: Record<number, '' | 'success' | 'warning' | 'info'> = { 1: 'info', 2: '', 3: 'warning', 4: 'success' }
  return map[status] || ''
}

function scoreColor(score: number | null): string {
  if (!score) return '#909399'
  if (score >= 80) return '#67C23A'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}
</script>

<style scoped lang="scss">
.career-plan-detail-page {
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
      align-items: center;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .stats-row {
    .stat-item {
      text-align: center;

      .stat-label {
        font-size: 13px;
        color: #999;
        margin-bottom: 4px;
      }

      .stat-value {
        font-size: 20px;
        font-weight: bold;
        color: #333;

        &.income {
          color: #67C23A;
        }
      }
    }
  }

  .amount-text {
    color: #67C23A;
    font-weight: bold;
  }

  .startup-plan-content {
    font-size: 14px;
    line-height: 1.8;
    color: #333;

    :deep(h3) {
      margin: 16px 0 8px;
      color: #409EFF;
    }

    :deep(h4) {
      margin: 12px 0 6px;
      color: #333;
    }

    :deep(strong) {
      color: #333;
    }
  }
}
</style>
