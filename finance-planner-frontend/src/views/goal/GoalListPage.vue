<template>
  <div class="goal-list-page">
    <n-spin :show="loading">
      <PageHeader title="理财目标">
        <template #actions>
          <n-button type="primary" @click="goToNew">
            <template #icon><n-icon><AddOutline /></n-icon></template>
            新建目标
          </n-button>
        </template>
      </PageHeader>

      <n-tabs v-model:value="activeTab" type="line" @update:value="handleTabChange">
        <n-tab-pane name="active" tab="进行中" />
        <n-tab-pane name="completed" tab="已完成" />
        <n-tab-pane name="abandoned" tab="已放弃" />
        <n-tab-pane name="all" tab="全部" />
      </n-tabs>

      <n-empty v-if="!loading && goals.length === 0" description="暂无目标，快去创建一个吧" style="margin-top: 40px" />

      <n-grid :x-gap="16" :y-gap="16" :cols="3" class="goal-grid">
        <n-gi v-for="goal in goals" :key="goal.id">
          <GlassCard hoverable class="goal-card" @click="goToDetail(goal.id)">
            <div class="goal-header">
              <div class="goal-title">
                <n-tag :type="priorityTag(goal.priority)" size="small">
                  {{ priorityLabel(goal.priority) }}
                </n-tag>
                <span class="title-text">{{ goal.title }}</span>
              </div>
              <n-tag :type="statusTag(goal.status)" size="small">
                {{ statusLabel(goal.status) }}
              </n-tag>
            </div>
            <div class="goal-amounts">
              <span class="current">¥{{ formatNumber(goal.currentAmount) }}</span>
              <span class="separator">/</span>
              <span class="target">¥{{ formatNumber(goal.targetAmount) }}</span>
            </div>
            <n-progress
              type="line"
              :percentage="Math.min(100, Number(goal.progressPercentage))"
              :color="progressColor(goal.status)"
              :height="10"
              :border-radius="5"
            />
            <div class="goal-footer">
              <span class="deadline">
                <n-icon :size="14"><CalendarOutline /></n-icon>
                {{ goal.deadline }}
              </span>
              <span v-if="goal.status === 1" class="remaining">
                还剩 {{ goal.remainingDays }} 天
              </span>
            </div>
            <div v-if="goal.status === 1 && goal.monthlySavingsNeeded > 0" class="monthly-hint">
              每月需存 ¥{{ formatNumber(goal.monthlySavingsNeeded) }}
            </div>
          </GlassCard>
        </n-gi>
      </n-grid>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NTabs, NTabPane, NButton, NIcon, NTag,
  NProgress, NEmpty, NGrid, NGi, useMessage,
} from 'naive-ui'
import { AddOutline, CalendarOutline } from '@vicons/ionicons5'
import { getGoals } from '@/api/goal'
import type { Goal } from '@/types/goal'
import GlassCard from '@/components/common/GlassCard.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const goals = ref<Goal[]>([])
const activeTab = ref('active')

onMounted(() => {
  fetchGoals()
})

async function fetchGoals() {
  loading.value = true
  try {
    const statusMap: Record<string, number | undefined> = {
      active: 1,
      completed: 2,
      abandoned: 3,
      all: undefined
    }
    const res = await getGoals(statusMap[activeTab.value])
    if (res.data.code === 200) {
      goals.value = res.data.data
    }
  } catch (error) {
    message.error('加载目标失败')
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  fetchGoals()
}

function goToNew() {
  router.push('/goals/new')
}

function goToDetail(id: number) {
  router.push(`/goals/${id}`)
}

function formatNumber(value: number | undefined | null): string {
  if (value === undefined || value === null) return '0.00'
  return Number(value).toFixed(2)
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

function progressColor(status: number): string {
  if (status === 2) return 'var(--cr-success)'
  if (status === 3) return 'var(--cr-text-tertiary)'
  return 'var(--cr-primary)'
}
</script>

<style scoped lang="scss">
.goal-list-page {
  .n-tabs {
    margin-bottom: 20px;
  }

  .goal-grid {
    margin-top: 16px;
  }

  .goal-card {
    cursor: pointer;

    .goal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .goal-title {
        display: flex;
        align-items: center;
        gap: 8px;

        .title-text {
          font-weight: 600;
          font-size: 16px;
          color: var(--cr-text-primary);
        }
      }
    }

    .goal-amounts {
      margin-bottom: 8px;
      font-size: 14px;

      .current {
        font-weight: bold;
        color: var(--cr-primary);
        font-size: 18px;
      }

      .separator {
        margin: 0 4px;
        color: var(--cr-text-tertiary);
      }

      .target {
        color: var(--cr-text-secondary);
      }
    }

    .goal-footer {
      display: flex;
      justify-content: space-between;
      margin-top: 12px;
      font-size: 13px;
      color: var(--cr-text-secondary);

      .deadline {
        display: flex;
        align-items: center;
        gap: 4px;
      }

      .remaining {
        color: var(--cr-warning);
      }
    }

    .monthly-hint {
      margin-top: 8px;
      font-size: 12px;
      color: var(--cr-text-secondary);
      background: var(--cr-bg-card);
      padding: 4px 8px;
      border-radius: 4px;
    }
  }
}
</style>
