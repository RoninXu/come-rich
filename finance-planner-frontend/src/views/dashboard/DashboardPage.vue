<template>
  <div class="dashboard-page">
    <PageHeader
      title="财务总览"
      :subtitle="`今天是 ${today}`"
      dense
    >
      <template #actions>
        <n-button type="primary" @click="router.push('/accounting/new')">
          记一笔
        </n-button>
        <n-button @click="router.push('/accounting/ocr')">拍照导入</n-button>
      </template>
    </PageHeader>

    <n-spin :show="loading">
      <n-grid :x-gap="16" :y-gap="16" :cols="4" class="metric-grid">
        <n-gi>
          <MetricCard
            label="本月收入"
            :value="formatCurrency(dashboard?.currentMonth?.totalIncome)"
            :delta="dashboard?.incomeChange"
          >
            <template #icon><TrendingUp /></template>
          </MetricCard>
        </n-gi>
        <n-gi>
          <MetricCard
            label="本月支出"
            :value="formatCurrency(dashboard?.currentMonth?.totalExpense)"
            :delta="dashboard?.expenseChange"
          >
            <template #icon><Wallet /></template>
          </MetricCard>
        </n-gi>
        <n-gi>
          <MetricCard
            label="本月结余"
            :value="formatCurrency(dashboard?.currentMonth?.balance)"
            tone="positive"
          >
            <template #icon><Cash /></template>
          </MetricCard>
        </n-gi>
        <n-gi>
          <MetricCard
            label="财务健康分"
            :value="`${dashboard?.healthScore ?? 0} ${dashboard?.healthGrade ?? ''}`"
            clickable
            @click="router.push('/analysis/health')"
          >
            <template #icon><Pulse /></template>
          </MetricCard>
        </n-gi>
      </n-grid>

      <n-grid :x-gap="16" :y-gap="16" :cols="24" class="content-grid">
        <n-gi :span="16">
          <GlassCard>
            <template #header>
              <SectionHeader title="本周 Review" description="最近交易与可执行动作" />
            </template>

            <div v-if="transactions.length" class="review-list">
              <div v-for="item in transactions" :key="item.id" class="review-row">
                <div class="review-row__left">
                  <div class="review-row__title">{{ item.categoryName || '未分类' }}</div>
                  <div class="review-row__meta">{{ item.description || '无备注' }} · {{ item.transactionDate }}</div>
                </div>
                <div :class="['review-row__amount tabular-nums', item.type === 1 ? 'income' : 'expense']">
                  {{ item.type === 1 ? '+' : '-' }}{{ formatCurrency(item.amount) }}
                </div>
              </div>
            </div>

            <EmptyState
              v-else
              title="还没有交易记录"
              description="先记一笔，系统会开始生成消费洞察"
            />
          </GlassCard>
        </n-gi>

        <n-gi :span="8">
          <GlassCard>
            <template #header>
              <SectionHeader title="预算风险" description="本月预算执行" />
            </template>

            <template v-if="budgetSummary">
              <BudgetProgressBar
                :budget="Number(budgetSummary.totalBudget)"
                :spent="Number(budgetSummary.totalSpent)"
              />
              <div class="budget-footnote">
                <span>剩余 {{ formatCurrency(budgetSummary.totalRemaining) }}</span>
                <n-button text type="primary" @click="router.push('/budget')">查看预算详情</n-button>
              </div>
            </template>
            <EmptyState v-else title="尚未设置预算" description="设置预算后可得到超支提醒" />

            <div class="risk-block">
              <h4>投资画像</h4>
              <p v-if="riskAssessment" class="tabular-nums">
                {{ riskAssessment.riskLevel }} · {{ riskAssessment.riskScore }}/32
              </p>
              <p v-else class="muted">尚未完成风险测评</p>
            </div>
          </GlassCard>
        </n-gi>
      </n-grid>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { NSpin, NGrid, NGi, NButton, useMessage } from "naive-ui";
import { TrendingUp, Wallet, Cash, Pulse } from "@vicons/ionicons5";
import dayjs from "dayjs";
import { getDashboard } from "@/api/analysis";
import { getBudgetSummary } from "@/api/budget";
import { getLatestAssessment } from "@/api/investment";
import type { Dashboard } from "@/types/analysis";
import type { BudgetSummary } from "@/types/budget";
import type { RiskAssessment } from "@/types/investment";
import type { Transaction } from "@/types/accounting";
import PageHeader from "@/components/common/PageHeader.vue";
import GlassCard from "@/components/common/GlassCard.vue";
import SectionHeader from "@/components/common/SectionHeader.vue";
import MetricCard from "@/components/common/MetricCard.vue";
import BudgetProgressBar from "@/components/common/BudgetProgressBar.vue";
import EmptyState from "@/components/common/EmptyState.vue";

const router = useRouter();
const message = useMessage();

const loading = ref(false);
const dashboard = ref<Dashboard | null>(null);
const budgetSummary = ref<BudgetSummary | null>(null);
const riskAssessment = ref<RiskAssessment | null>(null);

const today = computed(() => dayjs().format("YYYY年M月D日"));
const transactions = computed<Transaction[]>(
  () => dashboard.value?.recentTransactions?.slice(0, 8) ?? [],
);

onMounted(async () => {
  await fetchDashboard();
  await Promise.all([fetchBudget(), fetchRisk()]);
});

async function fetchDashboard() {
  loading.value = true;
  try {
    const res = await getDashboard();
    if (res.data.code === 200) dashboard.value = res.data.data;
  } catch {
    message.error("加载首页数据失败");
  } finally {
    loading.value = false;
  }
}

async function fetchBudget() {
  try {
    const month = dayjs().format("YYYY-MM");
    const res = await getBudgetSummary(month);
    if (res.data.code === 200) budgetSummary.value = res.data.data;
  } catch {
    budgetSummary.value = null;
  }
}

async function fetchRisk() {
  try {
    const res = await getLatestAssessment();
    if (res.data.code === 200) riskAssessment.value = res.data.data || null;
  } catch {
    riskAssessment.value = null;
  }
}

function formatCurrency(value: number | undefined | null): string {
  return new Intl.NumberFormat("zh-CN", {
    style: "currency",
    currency: "CNY",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(Number(value ?? 0));
}
</script>

<style scoped lang="scss">
.metric-grid {
  margin-bottom: var(--cr-space-lg);
}

.content-grid {
  margin-bottom: var(--cr-space-lg);
}

.review-list {
  display: grid;
  gap: 12px;
}

.review-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid var(--cr-divider);
  padding-bottom: 12px;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }

  &__title {
    font-size: 14px;
    color: var(--cr-text-primary);
    font-weight: 600;
  }

  &__meta {
    margin-top: 2px;
    font-size: 12px;
    color: var(--cr-text-tertiary);
  }

  &__amount {
    font-size: 14px;
    font-weight: 700;

    &.income {
      color: var(--cr-success);
    }

    &.expense {
      color: var(--cr-error);
    }
  }
}

.budget-footnote {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--cr-text-secondary);
  font-size: 12px;
}

.risk-block {
  margin-top: 18px;
  border-top: 1px solid var(--cr-divider);
  padding-top: 14px;

  h4 {
    font-size: 14px;
    margin-bottom: 4px;
  }
}

.muted {
  color: var(--cr-text-tertiary);
}

@media (max-width: 1200px) {
  .content-grid :deep(.n-grid-item) {
    grid-column: span 24 !important;
  }
}
</style>
