<template>
  <div class="career-plan-list-page">
    <div class="page-header">
      <h2>我的副业计划</h2>
      <el-button type="primary" @click="goToRecommendations">
        <el-icon><Plus /></el-icon>
        获取 AI 推荐
      </el-button>
    </div>

    <div v-loading="loading">
      <el-empty v-if="!loading && plans.length === 0" description="暂无副业计划">
        <el-button type="primary" @click="goToRecommendations">去获取推荐</el-button>
      </el-empty>

      <el-row :gutter="20">
        <el-col :span="8" v-for="plan in plans" :key="plan.id">
          <el-card class="plan-card" shadow="hover" @click="goToDetail(plan.id)">
            <div class="plan-header">
              <div class="plan-title">
                <el-tag v-if="plan.careerType" size="small" type="info">{{ plan.careerType }}</el-tag>
                <span class="title-text">{{ plan.title }}</span>
              </div>
              <el-tag :type="statusTag(plan.status)" size="small">
                {{ statusLabel(plan.status) }}
              </el-tag>
            </div>

            <p class="plan-description" v-if="plan.description">{{ plan.description }}</p>

            <div class="plan-stats">
              <div class="stat-item" v-if="plan.matchScore">
                <span class="stat-label">匹配度</span>
                <el-progress
                  :percentage="plan.matchScore"
                  :stroke-width="6"
                  :show-text="true"
                  :color="scoreColor(plan.matchScore)"
                  style="width: 120px"
                />
              </div>
              <div class="stat-item">
                <span class="stat-label">目标月收入</span>
                <span class="stat-value">¥{{ formatNumber(plan.targetMonthlyIncome) }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">实际月收入</span>
                <span class="stat-value income">¥{{ formatNumber(plan.actualMonthlyIncome) }}</span>
              </div>
            </div>

            <div class="plan-footer">
              <span v-if="plan.startDate" class="start-date">
                <el-icon><Calendar /></el-icon>
                {{ plan.startDate }}
              </span>
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
import { getCareerPlans } from '@/api/career'
import type { CareerPlan } from '@/types/career'

const router = useRouter()
const loading = ref(false)
const plans = ref<CareerPlan[]>([])

onMounted(async () => {
  await fetchPlans()
})

async function fetchPlans() {
  loading.value = true
  try {
    const res = await getCareerPlans()
    if (res.data.code === 200) {
      plans.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('加载计划列表失败')
  } finally {
    loading.value = false
  }
}

function goToDetail(id: number) {
  router.push(`/career/plans/${id}`)
}

function goToRecommendations() {
  router.push('/career')
}

function formatNumber(value: number | null | undefined): string {
  if (value === undefined || value === null) return '0.00'
  return Number(value).toFixed(2)
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
.career-plan-list-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 { margin: 0; }
  }

  .plan-card {
    margin-bottom: 20px;
    cursor: pointer;
    transition: transform 0.2s;

    &:hover {
      transform: translateY(-2px);
    }

    .plan-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .plan-title {
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

    .plan-description {
      color: #666;
      font-size: 13px;
      line-height: 1.5;
      margin: 0 0 12px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .plan-stats {
      .stat-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 6px 0;
        font-size: 14px;

        .stat-label {
          color: #999;
        }

        .stat-value {
          color: #333;
          font-weight: 500;

          &.income {
            color: #67C23A;
            font-weight: bold;
          }
        }
      }
    }

    .plan-footer {
      margin-top: 12px;
      font-size: 13px;
      color: #999;

      .start-date {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }
}
</style>
