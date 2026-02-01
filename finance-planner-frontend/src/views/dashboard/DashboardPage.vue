<template>
  <div class="dashboard-page" v-loading="loading">
    <div class="welcome-section">
      <h2>欢迎回来，{{ userStore.user?.nickname || userStore.username }}</h2>
      <p>今天是 {{ today }}，开始记录您的财务吧！</p>
    </div>

    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card class="stat-card income">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">本月收入</div>
              <div class="stat-value">¥ {{ formatNumber(dashboard?.currentMonth?.totalIncome) }}</div>
              <div v-if="dashboard?.incomeChange" class="stat-change" :class="dashboard.incomeChange >= 0 ? 'up' : 'down'">
                {{ dashboard.incomeChange >= 0 ? '+' : '' }}{{ dashboard.incomeChange.toFixed(1) }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card expense">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">本月支出</div>
              <div class="stat-value">¥ {{ formatNumber(dashboard?.currentMonth?.totalExpense) }}</div>
              <div v-if="dashboard?.expenseChange" class="stat-change" :class="dashboard.expenseChange <= 0 ? 'up' : 'down'">
                {{ dashboard.expenseChange >= 0 ? '+' : '' }}{{ dashboard.expenseChange.toFixed(1) }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card balance">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">本月结余</div>
              <div class="stat-value">¥ {{ formatNumber(dashboard?.currentMonth?.balance) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card health" @click="goToHealthScore" style="cursor: pointer">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">财务健康分</div>
              <div class="stat-value">
                {{ dashboard?.healthScore || 0 }}
                <span class="grade">{{ dashboard?.healthGrade || '-' }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="12">
        <el-card class="mini-card" @click="router.push('/budget')" style="cursor: pointer">
          <template #header>
            <div class="card-header">
              <span>预算使用情况</span>
              <el-button link type="primary" @click.stop="router.push('/budget')">查看详情</el-button>
            </div>
          </template>
          <div v-if="budgetSummary" class="budget-mini">
            <el-progress
              :percentage="Math.min(Number(budgetSummary.overallUtilization), 100)"
              :color="budgetSummary.overallUtilization > 100 ? '#F56C6C' : budgetSummary.overallUtilization > 80 ? '#E6A23C' : '#67C23A'"
              :stroke-width="16"
            />
            <div class="budget-info">
              <span>已用 ¥{{ Number(budgetSummary.totalSpent).toFixed(0) }}</span>
              <span>/ 预算 ¥{{ Number(budgetSummary.totalBudget).toFixed(0) }}</span>
            </div>
          </div>
          <el-empty v-else :image-size="40" description="未设置预算" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="mini-card" @click="router.push('/investment')" style="cursor: pointer">
          <template #header>
            <div class="card-header">
              <span>风险画像</span>
              <el-button link type="primary" @click.stop="router.push('/investment')">查看详情</el-button>
            </div>
          </template>
          <div v-if="riskAssessment" class="risk-mini">
            <span class="risk-level" :class="getRiskClass(riskAssessment.riskLevel)">{{ riskAssessment.riskLevel }}</span>
            <span class="risk-score">得分: {{ riskAssessment.riskScore }}/32</span>
          </div>
          <el-empty v-else :image-size="40" description="未完成风险评估" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="quick-actions">
          <template #header>
            <span>快捷操作</span>
          </template>
          <div class="action-buttons">
            <el-button type="primary" size="large" @click="goToNewTransaction">
              <el-icon><Plus /></el-icon>
              记一笔
            </el-button>
            <el-button size="large" @click="goToMonthlyReport">
              <el-icon><DataLine /></el-icon>
              查看报表
            </el-button>
            <el-button size="large" @click="goToHealthScore">
              <el-icon><TrendCharts /></el-icon>
              健康评分
            </el-button>
            <el-button size="large" @click="goToOcr">
              <el-icon><Camera /></el-icon>
              拍照记账
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="recent-transactions">
          <template #header>
            <div class="card-header">
              <span>最近记录</span>
              <el-button link type="primary" @click="goToTransactions">查看全部</el-button>
            </div>
          </template>
          <div v-if="!dashboard?.recentTransactions || dashboard.recentTransactions.length === 0" class="empty-state">
            <el-empty description="暂无记录，快去记一笔吧" />
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
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { TrendCharts, Wallet, Money, DataLine, Plus, Camera } from '@element-plus/icons-vue'
import { getDashboard } from '@/api/analysis'
import { getBudgetSummary } from '@/api/budget'
import { getLatestAssessment } from '@/api/investment'
import type { Dashboard } from '@/types/analysis'
import type { BudgetSummary } from '@/types/budget'
import type { RiskAssessment } from '@/types/investment'

const router = useRouter()
const userStore = useUserStore()

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
  } catch (error) {
    ElMessage.error('加载数据失败')
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

function goToNewTransaction() {
  router.push('/accounting/new')
}

function goToTransactions() {
  router.push('/accounting')
}

function goToMonthlyReport() {
  router.push('/analysis/monthly')
}

function goToHealthScore() {
  router.push('/analysis/health')
}

function goToOcr() {
  router.push('/accounting/ocr')
}
</script>

<style scoped lang="scss">
.dashboard-page {
  .welcome-section {
    margin-bottom: 24px;

    h2 {
      font-size: 24px;
      color: #333;
      margin-bottom: 8px;
    }

    p {
      color: #999;
      font-size: 14px;
    }
  }

  .stat-cards {
    margin-bottom: 20px;
  }

  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
    }

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;

      .el-icon {
        font-size: 24px;
        color: #fff;
      }
    }

    .stat-info {
      .stat-label {
        font-size: 14px;
        color: #999;
        margin-bottom: 4px;
      }

      .stat-value {
        font-size: 20px;
        font-weight: bold;
        color: #333;

        .grade {
          font-size: 14px;
          margin-left: 4px;
          color: #AB47BC;
        }
      }

      .stat-change {
        font-size: 12px;
        margin-top: 4px;

        &.up {
          color: #66BB6A;
        }

        &.down {
          color: #FF7043;
        }
      }
    }

    &.income .stat-icon {
      background: linear-gradient(135deg, #66BB6A, #43A047);
    }

    &.expense .stat-icon {
      background: linear-gradient(135deg, #FF7043, #F4511E);
    }

    &.balance .stat-icon {
      background: linear-gradient(135deg, #42A5F5, #1E88E5);
    }

    &.health .stat-icon {
      background: linear-gradient(135deg, #AB47BC, #8E24AA);
    }
  }

  .mini-card {
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
        color: #999;
      }
    }

    .risk-mini {
      display: flex;
      align-items: center;
      gap: 16px;

      .risk-level {
        font-size: 20px;
        font-weight: bold;

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

  .quick-actions {
    .action-buttons {
      display: flex;
      gap: 12px;
    }
  }

  .recent-transactions {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .transaction-list {
      .transaction-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;

        &:last-child {
          border-bottom: none;
        }

        .transaction-info {
          .category {
            font-weight: 500;
            margin-right: 8px;
          }

          .description {
            color: #999;
            font-size: 12px;
          }
        }

        .amount {
          font-weight: bold;

          &.income {
            color: #66BB6A;
          }

          &.expense {
            color: #FF7043;
          }
        }
      }
    }
  }
}
</style>
