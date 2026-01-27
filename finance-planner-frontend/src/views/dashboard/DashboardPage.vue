<template>
  <div class="dashboard-page">
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
              <div class="stat-value">¥ {{ monthlyIncome.toFixed(2) }}</div>
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
              <div class="stat-value">¥ {{ monthlyExpense.toFixed(2) }}</div>
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
              <div class="stat-value">¥ {{ (monthlyIncome - monthlyExpense).toFixed(2) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card health">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">财务健康分</div>
              <div class="stat-value">{{ healthScore }}</div>
            </div>
          </div>
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
          <div v-if="recentTransactions.length === 0" class="empty-state">
            <el-empty description="暂无记录，快去记一笔吧" />
          </div>
          <div v-else class="transaction-list">
            <div v-for="item in recentTransactions" :key="item.id" class="transaction-item">
              <div class="transaction-info">
                <span class="category">{{ item.categoryName }}</span>
                <span class="description">{{ item.description }}</span>
              </div>
              <span :class="['amount', item.type === 1 ? 'income' : 'expense']">
                {{ item.type === 1 ? '+' : '-' }}¥{{ item.amount.toFixed(2) }}
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
import dayjs from 'dayjs'
import { TrendCharts, Wallet, Money, DataLine, Plus } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const today = computed(() => dayjs().format('YYYY年MM月DD日'))

// Mock data - will be replaced with API calls
const monthlyIncome = ref(0)
const monthlyExpense = ref(0)
const healthScore = ref(0)
const recentTransactions = ref<any[]>([])

onMounted(() => {
  // TODO: Fetch data from API
})

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
