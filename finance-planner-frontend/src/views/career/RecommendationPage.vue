<template>
  <div class="recommendation-page">
    <n-spin :show="loading">
      <PageHeader title="AI 副业推荐">
        <template #actions>
          <n-button @click="goToProfile">
            <template #icon><n-icon><Person /></n-icon></template>
            编辑资料
          </n-button>
          <n-button type="primary" @click="fetchRecommendations" :loading="loading">
            <template #icon><n-icon><Refresh /></n-icon></template>
            重新推荐
          </n-button>
        </template>
      </PageHeader>

      <n-alert
        title="AI 推荐基于您的个人资料生成，请先完善个人资料以获得更精准的推荐。"
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      />

      <n-empty v-if="!loading && recommendations.length === 0" description="暂无推荐，请先完善个人资料">
        <template #extra>
          <n-button type="primary" @click="goToProfile">去完善资料</n-button>
        </template>
      </n-empty>

      <n-grid :x-gap="20" :y-gap="20" :cols="3">
        <n-gi v-for="(rec, index) in recommendations" :key="index">
          <GlassCard hoverable>
            <div class="card-top">
              <div class="match-score">
                <n-progress
                  type="circle"
                  :percentage="rec.matchScore || 0"
                  :circle-gap="0"
                  :stroke-width="5"
                  :color="scoreColor(rec.matchScore)"
                  style="width: 60px"
                />
              </div>
              <div class="card-info">
                <n-tag size="small" type="info">{{ rec.careerType }}</n-tag>
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

            <n-button
              type="primary"
              block
              style="margin-top: 16px"
              @click="handleAdopt(rec)"
            >
              采纳为计划
            </n-button>
          </GlassCard>
        </n-gi>
      </n-grid>

      <!-- Adopt Dialog -->
      <n-modal v-model:show="showAdoptDialog" preset="card" title="创建副业计划" style="width: 500px">
        <n-form :model="adoptForm" label-placement="left" label-width="100">
          <n-form-item label="计划名称">
            <n-input v-model:value="adoptForm.title" />
          </n-form-item>
          <n-form-item label="副业类型">
            <n-input v-model:value="adoptForm.careerType" disabled />
          </n-form-item>
          <n-form-item label="描述">
            <n-input v-model:value="adoptForm.description" type="textarea" :autosize="{ minRows: 3, maxRows: 5 }" />
          </n-form-item>
          <n-form-item label="目标月收入">
            <n-input-number
              v-model:value="adoptForm.targetMonthlyIncome"
              :min="0"
              :precision="0"
              :show-button="false"
              style="width: 100%"
            />
          </n-form-item>
          <n-form-item label="开始日期">
            <n-date-picker v-model:value="adoptForm.startDate" type="date" style="width: 100%" />
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="showAdoptDialog = false">取消</n-button>
            <n-button type="primary" @click="confirmAdopt" :loading="adopting">创建计划</n-button>
          </n-space>
        </template>
      </n-modal>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NGrid, NGi, NButton, NIcon, NTag, NProgress, NAlert, NEmpty,
  NModal, NForm, NFormItem, NInput, NInputNumber, NDatePicker, NSpace,
  useMessage,
} from 'naive-ui'
import { Person, Refresh } from '@vicons/ionicons5'
import dayjs from 'dayjs'
import { getRecommendations, createCareerPlan } from '@/api/career'
import type { CareerRecommendation } from '@/types/career'
import GlassCard from '@/components/common/GlassCard.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const router = useRouter()
const message = useMessage()
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
  startDate: Date.now() as number | null
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
  } catch {
    message.error('获取推荐失败，请先完善个人资料')
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
  adoptForm.startDate = Date.now()
  showAdoptDialog.value = true
}

async function confirmAdopt() {
  if (!adoptForm.title) {
    message.warning('请填写计划名称')
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
    message.success('计划已创建')
    showAdoptDialog.value = false
    router.push('/career/plans')
  } catch {
    // Handled by interceptor
  } finally {
    adopting.value = false
  }
}

function scoreColor(score: number | null): string {
  if (!score) return '#86868B'
  if (score >= 80) return '#34C759'
  if (score >= 60) return '#FF9500'
  return '#FF3B30'
}

function goToProfile() {
  router.push('/career/profile')
}
</script>

<style scoped lang="scss">
.recommendation-page {
  max-width: 1200px;
  margin: 0 auto;

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
        color: var(--cr-text-primary);
      }
    }
  }

  .description {
    color: var(--cr-text-secondary);
    font-size: 14px;
    line-height: 1.6;
    margin: 0 0 16px;
  }

  .detail-items {
    .detail-item {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      border-bottom: 1px solid var(--cr-divider);
      font-size: 14px;

      &:last-child { border-bottom: none; }

      .detail-label { color: var(--cr-text-tertiary); }
      .detail-value {
        color: var(--cr-text-primary);
        font-weight: 500;

        &.income { color: var(--cr-success); font-weight: bold; }
      }
    }
  }
}
</style>
