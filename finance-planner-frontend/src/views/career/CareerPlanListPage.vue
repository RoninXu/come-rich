<template>
  <div class="career-plan-list-page">
    <n-spin :show="loading">
      <PageHeader title="我的副业计划">
        <template #actions>
          <n-button
            type="primary"
            @click="goToRecommendations"
          >
            <template #icon>
              <n-icon><Add /></n-icon>
            </template>
            获取 AI 推荐
          </n-button>
        </template>
      </PageHeader>

      <n-empty
        v-if="!loading && plans.length === 0"
        description="暂无副业计划"
      >
        <template #extra>
          <n-button
            type="primary"
            @click="goToRecommendations"
          >
            去获取推荐
          </n-button>
        </template>
      </n-empty>

      <n-grid
        :x-gap="20"
        :y-gap="20"
        :cols="3"
      >
        <n-gi
          v-for="plan in plans"
          :key="plan.id"
        >
          <GlassCard
            hoverable
            class="plan-card"
            @click="goToDetail(plan.id)"
          >
            <div class="plan-header">
              <div class="plan-title">
                <n-tag
                  v-if="plan.careerType"
                  size="small"
                  type="info"
                >
                  {{
                    plan.careerType
                  }}
                </n-tag>
                <span class="title-text">{{ plan.title }}</span>
              </div>
              <n-tag
                :type="statusTag(plan.status)"
                size="small"
              >
                {{ statusLabel(plan.status) }}
              </n-tag>
            </div>

            <p
              v-if="plan.description"
              class="plan-description"
            >
              {{ plan.description }}
            </p>

            <div class="plan-stats">
              <div
                v-if="plan.matchScore"
                class="stat-item"
              >
                <span class="stat-label">匹配度</span>
                <n-progress
                  :percentage="plan.matchScore"
                  :height="6"
                  :border-radius="3"
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

            <div
              v-if="plan.startDate"
              class="plan-footer"
            >
              <n-icon><Calendar /></n-icon>
              <span>{{ plan.startDate }}</span>
            </div>
          </GlassCard>
        </n-gi>
      </n-grid>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  NSpin,
  NGrid,
  NGi,
  NButton,
  NIcon,
  NTag,
  NProgress,
  NEmpty,
  useMessage,
} from "naive-ui";
import { Add, Calendar } from "@vicons/ionicons5";
import { getCareerPlans } from "@/api/career";
import type { CareerPlan } from "@/types/career";
import GlassCard from "@/components/common/GlassCard.vue";
import PageHeader from "@/components/common/PageHeader.vue";

const router = useRouter();
const message = useMessage();
const loading = ref(false);
const plans = ref<CareerPlan[]>([]);

onMounted(async () => {
  await fetchPlans();
});

async function fetchPlans() {
  loading.value = true;
  try {
    const res = await getCareerPlans();
    if (res.data.code === 200) {
      plans.value = res.data.data;
    }
  } catch {
    message.error("加载计划列表失败");
  } finally {
    loading.value = false;
  }
}

function goToDetail(id: number) {
  router.push(`/career/plans/${id}`);
}

function goToRecommendations() {
  router.push("/career");
}

function formatNumber(value: number | null | undefined): string {
  if (value === undefined || value === null) return "0.00";
  return Number(value).toFixed(2);
}

function statusLabel(status: number): string {
  const map: Record<number, string> = {
    1: "探索中",
    2: "进行中",
    3: "已暂停",
    4: "已完成",
  };
  return map[status] || "未知";
}

function statusTag(
  status: number,
): "default" | "success" | "warning" | "error" | "info" {
  const map: Record<number, "info" | "default" | "warning" | "success"> = {
    1: "info",
    2: "default",
    3: "warning",
    4: "success",
  };
  return map[status] || "default";
}

function scoreColor(score: number | null): string {
  if (!score) return "#86868B";
  if (score >= 80) return "#34C759";
  if (score >= 60) return "#FF9500";
  return "#FF3B30";
}
</script>

<style scoped lang="scss">
.career-plan-list-page {
  max-width: 1200px;
  margin: 0 auto;

  .plan-card {
    cursor: pointer;

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
          color: var(--cr-text-primary);
        }
      }
    }

    .plan-description {
      color: var(--cr-text-secondary);
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
          color: var(--cr-text-tertiary);
        }
        .stat-value {
          color: var(--cr-text-primary);
          font-weight: 500;

          &.income {
            color: var(--cr-success);
            font-weight: bold;
          }
        }
      }
    }

    .plan-footer {
      display: flex;
      align-items: center;
      gap: 4px;
      margin-top: 12px;
      font-size: 13px;
      color: var(--cr-text-tertiary);
    }
  }
}
</style>
