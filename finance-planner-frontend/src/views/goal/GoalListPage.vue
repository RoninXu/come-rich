<template>
  <div class="goal-list-page">
    <div class="page-header">
      <h2>理财目标</h2>
      <el-button type="primary" @click="goToNew">
        <el-icon><Plus /></el-icon>
        新建目标
      </el-button>
    </div>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="进行中" name="active" />
      <el-tab-pane label="已完成" name="completed" />
      <el-tab-pane label="已放弃" name="abandoned" />
      <el-tab-pane label="全部" name="all" />
    </el-tabs>

    <div v-loading="loading">
      <el-empty v-if="!loading && goals.length === 0" description="暂无目标，快去创建一个吧" />

      <el-row :gutter="20">
        <el-col :span="8" v-for="goal in goals" :key="goal.id">
          <el-card class="goal-card" shadow="hover" @click="goToDetail(goal.id)">
            <div class="goal-header">
              <div class="goal-title">
                <el-tag :type="priorityTag(goal.priority)" size="small">
                  {{ priorityLabel(goal.priority) }}
                </el-tag>
                <span class="title-text">{{ goal.title }}</span>
              </div>
              <el-tag :type="statusTag(goal.status)" size="small">
                {{ statusLabel(goal.status) }}
              </el-tag>
            </div>
            <div class="goal-amounts">
              <span class="current">¥{{ formatNumber(goal.currentAmount) }}</span>
              <span class="separator">/</span>
              <span class="target">¥{{ formatNumber(goal.targetAmount) }}</span>
            </div>
            <el-progress
              :percentage="Math.min(100, Number(goal.progressPercentage))"
              :color="progressColor(goal.status)"
              :stroke-width="10"
            />
            <div class="goal-footer">
              <span class="deadline">
                <el-icon><Calendar /></el-icon>
                {{ goal.deadline }}
              </span>
              <span v-if="goal.status === 1" class="remaining">
                还剩 {{ goal.remainingDays }} 天
              </span>
            </div>
            <div v-if="goal.status === 1 && goal.monthlySavingsNeeded > 0" class="monthly-hint">
              每月需存 ¥{{ formatNumber(goal.monthlySavingsNeeded) }}
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Calendar } from '@element-plus/icons-vue'
import { getGoals } from '@/api/goal'
import type { Goal } from '@/types/goal'

const router = useRouter()
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
    ElMessage.error('加载目标失败')
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

function progressColor(status: number): string {
  if (status === 2) return '#67C23A'
  if (status === 3) return '#909399'
  return '#409EFF'
}
</script>

<style scoped lang="scss">
.goal-list-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 { margin: 0; }
  }

  .el-tabs {
    margin-bottom: 20px;
  }

  .goal-card {
    margin-bottom: 20px;
    cursor: pointer;
    transition: transform 0.2s;

    &:hover {
      transform: translateY(-2px);
    }

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
          color: #333;
        }
      }
    }

    .goal-amounts {
      margin-bottom: 8px;
      font-size: 14px;

      .current {
        font-weight: bold;
        color: #409EFF;
        font-size: 18px;
      }

      .separator {
        margin: 0 4px;
        color: #ccc;
      }

      .target {
        color: #999;
      }
    }

    .goal-footer {
      display: flex;
      justify-content: space-between;
      margin-top: 12px;
      font-size: 13px;
      color: #999;

      .deadline {
        display: flex;
        align-items: center;
        gap: 4px;
      }

      .remaining {
        color: #E6A23C;
      }
    }

    .monthly-hint {
      margin-top: 8px;
      font-size: 12px;
      color: #909399;
      background: #f4f4f5;
      padding: 4px 8px;
      border-radius: 4px;
    }
  }
}
</style>
