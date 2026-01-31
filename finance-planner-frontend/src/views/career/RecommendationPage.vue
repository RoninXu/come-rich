<template>
  <div class="recommendation-page">
    <div class="page-header">
      <h2>AI 副业推荐</h2>
      <div class="header-actions">
        <el-button @click="goToProfile">
          <el-icon><User /></el-icon>
          编辑资料
        </el-button>
        <el-button type="primary" @click="fetchRecommendations" :loading="loading">
          <el-icon><Refresh /></el-icon>
          重新推荐
        </el-button>
      </div>
    </div>

    <el-alert
      title="AI 推荐基于您的个人资料生成，请先完善个人资料以获得更精准的推荐。"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 20px"
    />

    <div v-loading="loading">
      <el-empty v-if="!loading && recommendations.length === 0" description="暂无推荐，请先完善个人资料">
        <el-button type="primary" @click="goToProfile">去完善资料</el-button>
      </el-empty>

      <el-row :gutter="20">
        <el-col :span="8" v-for="(rec, index) in recommendations" :key="index">
          <el-card class="recommendation-card" shadow="hover">
            <div class="card-top">
              <div class="match-score">
                <el-progress
                  type="circle"
                  :percentage="rec.matchScore || 0"
                  :width="60"
                  :stroke-width="5"
                  :color="scoreColor(rec.matchScore)"
                />
              </div>
              <div class="card-info">
                <el-tag size="small" type="info">{{ rec.careerType }}</el-tag>
                <h3 class="title">{{ rec.title }}</h3>
              </div>
            </div>

            <p class="description">{{ rec.description }}</p>

            <div class="detail-items">
              <div class="detail-item" v-if="rec.estimatedMonthlyIncome">
                <span class="detail-label">预期月收入</span>
                <span class="detail-value income">¥{{ rec.estimatedMonthlyIncome }}</span>
              </div>
              <div class="detail-item" v-if="rec.requiredSkills">
                <span class="detail-label">所需技能</span>
                <span class="detail-value">{{ rec.requiredSkills }}</span>
              </div>
              <div class="detail-item" v-if="rec.timeCommitment">
                <span class="detail-label">时间投入</span>
                <span class="detail-value">{{ rec.timeCommitment }}</span>
              </div>
            </div>

            <el-button
              type="primary"
              style="width: 100%; margin-top: 16px"
              @click="handleAdopt(rec)"
            >
              采纳为计划
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- Adopt Dialog -->
    <el-dialog v-model="showAdoptDialog" title="创建副业计划" width="500px">
      <el-form :model="adoptForm" label-width="100px">
        <el-form-item label="计划名称">
          <el-input v-model="adoptForm.title" />
        </el-form-item>
        <el-form-item label="副业类型">
          <el-input v-model="adoptForm.careerType" disabled />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="adoptForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="目标月收入">
          <el-input-number
            v-model="adoptForm.targetMonthlyIncome"
            :min="0"
            :precision="0"
            :controls="false"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="adoptForm.startDate" type="date" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdoptDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAdopt" :loading="adopting">创建计划</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getRecommendations, createCareerPlan } from '@/api/career'
import type { CareerRecommendation } from '@/types/career'

const router = useRouter()
const loading = ref(false)
const recommendations = ref<CareerRecommendation[]>([])
const showAdoptDialog = ref(false)
const adopting = ref(false)

const adoptForm = reactive({
  title: '',
  careerType: '',
  description: '',
  matchScore: undefined as number | undefined,
  targetMonthlyIncome: undefined as number | undefined,
  startDate: new Date() as string | Date
})

onMounted(async () => {
  await fetchRecommendations()
})

async function fetchRecommendations() {
  loading.value = true
  try {
    const res = await getRecommendations()
    if (res.data.code === 200) {
      recommendations.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取推荐失败，请先完善个人资料')
  } finally {
    loading.value = false
  }
}

function handleAdopt(rec: CareerRecommendation) {
  adoptForm.title = rec.title || ''
  adoptForm.careerType = rec.careerType || ''
  adoptForm.description = rec.description || ''
  adoptForm.matchScore = rec.matchScore || undefined
  adoptForm.targetMonthlyIncome = rec.estimatedMonthlyIncome || undefined
  adoptForm.startDate = new Date()
  showAdoptDialog.value = true
}

async function confirmAdopt() {
  if (!adoptForm.title) {
    ElMessage.warning('请填写计划名称')
    return
  }
  adopting.value = true
  try {
    await createCareerPlan({
      title: adoptForm.title,
      careerType: adoptForm.careerType || undefined,
      description: adoptForm.description || undefined,
      matchScore: adoptForm.matchScore,
      targetMonthlyIncome: adoptForm.targetMonthlyIncome,
      startDate: dayjs(adoptForm.startDate).format('YYYY-MM-DD')
    })
    ElMessage.success('计划已创建')
    showAdoptDialog.value = false
    router.push('/career/plans')
  } catch (error) {
    // Handled by interceptor
  } finally {
    adopting.value = false
  }
}

function scoreColor(score: number | null): string {
  if (!score) return '#909399'
  if (score >= 80) return '#67C23A'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}

function goToProfile() {
  router.push('/career/profile')
}
</script>

<style scoped lang="scss">
.recommendation-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 { margin: 0; }

    .header-actions {
      display: flex;
      gap: 8px;
    }
  }

  .recommendation-card {
    margin-bottom: 20px;

    .card-top {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 12px;

      .card-info {
        flex: 1;

        .title {
          margin: 8px 0 0;
          font-size: 16px;
          color: #333;
        }
      }
    }

    .description {
      color: #666;
      font-size: 14px;
      line-height: 1.6;
      margin: 0 0 16px;
    }

    .detail-items {
      .detail-item {
        display: flex;
        justify-content: space-between;
        padding: 8px 0;
        border-bottom: 1px solid #f0f0f0;
        font-size: 14px;

        &:last-child {
          border-bottom: none;
        }

        .detail-label {
          color: #999;
        }

        .detail-value {
          color: #333;
          font-weight: 500;

          &.income {
            color: #67C23A;
            font-weight: bold;
          }
        }
      }
    }
  }
}
</style>
