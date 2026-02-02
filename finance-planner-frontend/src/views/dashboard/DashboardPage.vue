<template>
  <div class="dashboard-page">
    <n-spin :show="loading">
      <div class="welcome-section">
        <h2>欢迎回来，{{ userStore.user?.nickname || userStore.username }}</h2>
        <p>今天是 {{ today }}，开始记录您的财务吧！</p>
      </div>

      <n-grid :x-gap="16" :y-gap="16" :cols="4" class="stat-cards">
        <n-gi>
          <StatCard
            label="本月收入"
            :value="'¥ ' + formatNumber(dashboard?.currentMonth?.totalIncome)"
            :change="dashboard?.incomeChange"
            icon-bg="linear-gradient(135deg, #34C759, #30D158)"
            icon-color="#fff"
          >
            <template #icon><TrendingUp /></template>
          </StatCard>
        </n-gi>
        <n-gi>
          <StatCard
            label="本月支出"
            :value="'¥ ' + formatNumber(dashboard?.currentMonth?.totalExpense)"
            :change="dashboard?.expenseChange"
            icon-bg="linear-gradient(135deg, #FF9500, #FF9F0A)"
            icon-color="#fff"
          >
            <template #icon><Wallet /></template>
          </StatCard>
        </n-gi>
        <n-gi>
          <StatCard
            label="本月结余"
            :value="'¥ ' + formatNumber(dashboard?.currentMonth?.balance)"
            icon-bg="linear-gradient(135deg, #007AFF, #0A84FF)"
            icon-color="#fff"
          >
            <template #icon><Cash /></template>
          </StatCard>
        </n-gi>
        <n-gi>
          <StatCard
            label="财务健康分"
            :value="(dashboard?.healthScore || 0) + ' ' + (dashboard?.healthGrade || '-')"
            icon-bg="linear-gradient(135deg, #AF52DE, #BF5AF2)"
            icon-color="#fff"
            clickable
            @click="goToHealthScore"
          >
            <template #icon><StatsChart /></template>
          </StatCard>
        </n-gi>
      </n-grid>

      <n-grid :x-gap="16" :y-gap="16" :cols="2" style="margin-bottom: 16px">
        <n-gi>
          <GlassCard hoverable @click="router.push('/budget')" style="cursor: pointer">
            <template #header>
              <div class="card-header">
                <span>预算使用情况</span>
                <n-button text type="primary" @click.stop="router.push('/budget')">查看详情</n-button>
              </div>
            </template>
            <div v-if="budgetSummary" class="budget-mini">
              <n-progress
                type="line"
                :percentage="Math.min(Number(budgetSummary.overallUtilization), 100)"
                :color="budgetSummary.overallUtilization > 100 ? 'var(--cr-error)' : budgetSummary.overallUtilization > 80 ? 'var(--cr-warning)' : 'var(--cr-success)'"
                :height="12"
                :border-radius="6"
              />
              <div class="budget-info">
                <span>已用 ¥{{ Number(budgetSummary.totalSpent).toFixed(0) }}</span>
                <span>/ 预算 ¥{{ Number(budgetSummary.totalBudget).toFixed(0) }}</span>
              </div>
            </div>
            <n-empty v-else size="small" description="未设置预算" />
          </GlassCard>
        </n-gi>
        <n-gi>
          <GlassCard hoverable @click="router.push('/investment')" style="cursor: pointer">
            <template #header>
              <div class="card-header">
                <span>风险画像</span>
                <n-button text type="primary" @click.stop="router.push('/investment')">查看详情</n-button>
              </div>
            </template>
            <div v-if="riskAssessment" class="risk-mini">
              <span class="risk-level" :class="getRiskClass(riskAssessment.riskLevel)">{{ riskAssessment.riskLevel }}</span>
              <span class="risk-score">得分: {{ riskAssessment.riskScore }}/32</span>
            </div>
            <n-empty v-else size="small" description="未完成风险评估" />
          </GlassCard>
        </n-gi>
      </n-grid>

      <n-grid :x-gap="16" :y-gap="16" :cols="2">
        <n-gi>
          <GlassCard>
            <template #header>
              <span>快捷操作</span>
            </template>
            <div class="action-buttons">
              <n-button type="primary" @click="goToNewTransaction">
                <template #icon><n-icon><Add /></n-icon></template>
                记一笔
              </n-button>
              <n-button @click="goToMonthlyReport">
                <template #icon><n-icon><StatsChart /></n-icon></template>
                查看报表
              </n-button>
              <n-button @click="goToHealthScore">
                <template #icon><n-icon><TrendingUp /></n-icon></template>
                健康评分
              </n-button>
              <n-button @click="goToOcr">
                <template #icon><n-icon><Camera /></n-icon></template>
                拍照记账
              </n-button>
            </div>
          </GlassCard>
        </n-gi>
        <n-gi>
          <GlassCard>
            <template #header>
              <div class="card-header">
                <span>最近记录</span>
                <n-button text type="primary" @click="goToTransactions">查看全部</n-button>
              </div>
            </template>
            <div v-if="!dashboard?.recentTransactions || dashboard.recentTransactions.length === 0">
              <n-empty description="暂无记录，快去记一笔吧" />
            </div>
            <div v-else class="transaction-list">
              <div v-for="item in dashboard.recentTransactions" :key="item.id" class="transaction-item">
                <div class="transaction-info">
                  <span class="category">{{ item.categoryName || '未分类' }}</span>
                  <span class="description">{{ item.description || '-' }}</span>
                </div>
                <span :class="['amount', item.type === 1 ? 'income' : 'expense']">
                  {{ item.type === 1 ? '+' : '-' }}¥{{ Number(item.amount).toFixed(2) }}
                </span>
              </div>
            </div>
          </GlassCard>
        </n-gi>
      </n-grid>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NSpin, NGrid, NGi, NProgress, NEmpty, NButton, NIcon, useMessage } from 'naive-ui'
import { TrendingUp, Wallet, Cash, StatsChart, Add, Camera } from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import { getDashboard } from '@/api/analysis'
import { getBudgetSummary } from '@/api/budget'
import { getLatestAssessment } from '@/api/investment'
import type { Dashboard } from '@/types/analysis'
import type { BudgetSummary } from '@/types/budget'
import type { RiskAssessment } from '@/types/investment'
import GlassCard from '@/components/common/GlassCard.vue'
import StatCard from '@/components/common/StatCard.vue'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

const loading = ref(false)
const dashboard = ref<Dashboard | null>(null)
const budgetSummary = ref<BudgetSummary | null>(null)
const riskAssessment = ref<RiskAssessment | null>(null)

const today = computed(() => dayjs().format('YYYY年MM月DD日'))

onMounted(async () => {
  await fetchDashboard()
  fetchBudgetAndRisk()
})

async function fetchDashboard() {
  loading.value = true
  try {
    const res = await getDashboard()
    if (res.data.code === 200) {
      dashboard.value = res.data.data
    }
  } catch {
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

async function fetchBudgetAndRisk() {
  const currentMonth = dayjs().format('YYYY-MM')
  try {
    const budgetRes = await getBudgetSummary(currentMonth)
    if (budgetRes.data.code === 200) {
      budgetSummary.value = budgetRes.data.data
    }
  } catch {
    // No budget set
  }
  try {
    const riskRes = await getLatestAssessment()
    if (riskRes.data.code === 200) {
      riskAssessment.value = riskRes.data.data
    }
  } catch {
    // No assessment
  }
}

function getRiskClass(level: string): string {
  if (level === '保守型') return 'conservative'
  if (level === '稳健型') return 'moderate'
  return 'aggressive'
}

function formatNumber(value: number | undefined | null): string {
  if (value === undefined || value === null) return '0.00'
  return Number(value).toFixed(2)
}

function goToNewTransaction() { router.push('/accounting/new') }
function goToTransactions() { router.push('/accounting') }
function goToMonthlyReport() { router.push('/analysis/monthly') }
function goToHealthScore() { router.push('/analysis/health') }
function goToOcr() { router.push('/accounting/ocr') }
</script>

<style scoped lang="scss">
.dashboard-page {
  .welcome-section {
    margin-bottom: 24px;

    h2 {
      font-size: 24px;
      color: var(--cr-text-primary);
      font-weight: 700;
      letter-spacing: -0.02em;
      margin-bottom: 8px;
    }

    p {
      color: var(--cr-text-secondary);
      font-size: 14px;
    }
  }

  .stat-cards {
    margin-bottom: 16px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .budget-mini {
    .budget-info {
      display: flex;
      justify-content: space-between;
      margin-top: 8px;
      font-size: 13px;
      color: var(--cr-text-secondary);
    }
  }

  .risk-mini {
    display: flex;
    align-items: center;
    gap: 16px;

    .risk-level {
      font-size: 20px;
      font-weight: 700;

      &.conservative { color: var(--cr-success); }
      &.moderate { color: var(--cr-warning); }
      &.aggressive { color: var(--cr-error); }
    }

    .risk-score {
      color: var(--cr-text-secondary);
      font-size: 14px;
    }
  }

  .action-buttons {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }

  .transaction-list {
    .transaction-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid var(--cr-divider);

      &:last-child {
        border-bottom: none;
      }

      .transaction-info {
        .category {
          font-weight: 500;
          color: var(--cr-text-primary);
          margin-right: 8px;
        }

        .description {
          color: var(--cr-text-secondary);
          font-size: 12px;
        }
      }

      .amount {
        font-weight: 600;

        &.income { color: var(--cr-success); }
        &.expense { color: var(--cr-error); }
      }
    }
  }
}
</style>
