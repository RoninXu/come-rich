<template>
  <div class="investment-advice-page">
    <n-spin :show="loading">
      <PageHeader title="投资建议">
        <template #actions>
          <n-button @click="router.push('/investment/quiz')">
            {{ hasAssessment ? "重新评估" : "开始评估" }}
          </n-button>
          <n-button
            v-if="hasAssessment"
            type="primary"
            :loading="generating"
            @click="handleGenerate"
          >
            生成AI建议
          </n-button>
        </template>
      </PageHeader>

      <!-- No assessment -->
      <GlassCard v-if="!hasAssessment && !loading">
        <n-empty description="您还没有完成风险评估">
          <template #extra>
            <n-button
              type="primary"
              @click="router.push('/investment/quiz')"
            >
              开始风险评估
            </n-button>
          </template>
        </n-empty>
      </GlassCard>

      <!-- Risk profile card -->
      <GlassCard
        v-if="hasAssessment"
        class="profile-card"
      >
        <template #header>
          我的风险画像
        </template>
        <div class="profile-content">
          <div
            class="score-badge"
            :class="riskClass"
          >
            <span class="score">{{ assessment!.riskScore }}</span>
            <span class="max">/32</span>
          </div>
          <div class="profile-info">
            <div
              class="risk-level"
              :class="riskClass"
            >
              {{ assessment!.riskLevel }}
            </div>
            <div class="assessment-date">
              评估日期：{{ assessment!.assessmentDate }}
            </div>
          </div>
        </div>
      </GlassCard>

      <n-grid
        v-if="recommendations.length > 0"
        :x-gap="20"
        :y-gap="16"
        :cols="24"
        style="margin-top: 16px"
      >
        <n-gi :span="10">
          <GlassCard>
            <template #header>
              资产配置
            </template>
            <div
              ref="pieChartRef"
              class="chart-container"
            />
          </GlassCard>
        </n-gi>
        <n-gi :span="14">
          <GlassCard>
            <template #header>
              投资方向推荐
            </template>
            <div class="recommendation-list">
              <GlassCard
                v-for="rec in recommendations"
                :key="rec.id"
                hoverable
                class="rec-card"
              >
                <div class="rec-header">
                  <span class="track-name">{{ rec.trackName }}</span>
                  <n-tag size="small">
                    {{ rec.allocationPercentage }}%
                  </n-tag>
                </div>
                <p
                  v-if="rec.description"
                  class="rec-desc"
                >
                  {{ rec.description }}
                </p>
                <p
                  v-if="rec.rationale"
                  class="rec-rationale"
                >
                  {{ rec.rationale }}
                </p>
                <div class="rec-meta">
                  <span v-if="rec.riskLevel">风险：{{ rec.riskLevel }}</span>
                  <span v-if="rec.expectedAnnualReturn">预期年化：{{ rec.expectedAnnualReturn }}</span>
                </div>
              </GlassCard>
            </div>
          </GlassCard>
        </n-gi>
      </n-grid>

      <!-- Disclaimers -->
      <n-alert
        v-if="recommendations.length > 0"
        title="投资有风险，入市需谨慎。以上建议仅供参考，不构成具体投资建议。本平台不推荐具体金融产品，仅提供投资类别方向参考。"
        type="warning"
        :closable="false"
        style="margin-top: 16px"
      />
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from "vue";
import { useRouter } from "vue-router";
import { useMessage } from "naive-ui";
import * as echarts from "echarts";
import {
  getLatestAssessment,
  getActiveRecommendations,
  getAssetAllocation,
  generateRecommendations,
} from "@/api/investment";
import type {
  RiskAssessment,
  InvestmentRecommendation,
} from "@/types/investment";
import GlassCard from "@/components/common/GlassCard.vue";
import PageHeader from "@/components/common/PageHeader.vue";

const router = useRouter();
const message = useMessage();
const loading = ref(false);
const generating = ref(false);
const assessment = ref<RiskAssessment | null>(null);
const recommendations = ref<InvestmentRecommendation[]>([]);

const pieChartRef = ref<HTMLElement>();
let pieChart: echarts.ECharts | null = null;

const hasAssessment = computed(() => assessment.value !== null);
const riskClass = computed(() => {
  if (!assessment.value) return "";
  const level = assessment.value.riskLevel;
  if (level === "保守型") return "conservative";
  if (level === "稳健型") return "moderate";
  return "aggressive";
});

onMounted(() => {
  fetchData();
});

async function fetchData() {
  loading.value = true;
  try {
    // Fetch assessment
    try {
      const assessRes = await getLatestAssessment();
      if (assessRes.data.code === 200) {
        assessment.value = assessRes.data.data;
      }
    } catch {
      assessment.value = null;
    }

    // Fetch recommendations
    if (assessment.value) {
      try {
        const recRes = await getActiveRecommendations();
        if (recRes.data.code === 200) {
          recommendations.value = recRes.data.data || [];
        }
      } catch {
        recommendations.value = [];
      }
    }

    if (recommendations.value.length > 0) {
      nextTick(() => updatePieChart());
    }
  } finally {
    loading.value = false;
  }
}

async function handleGenerate() {
  generating.value = true;
  try {
    const res = await generateRecommendations();
    if (res.data.code === 200 && res.data.data) {
      recommendations.value = res.data.data.recommendations || [];
      message.success("投资建议已生成");
      nextTick(() => updatePieChart());
    }
  } catch {
    message.error("生成建议失败");
  } finally {
    generating.value = false;
  }
}

async function updatePieChart() {
  if (!pieChartRef.value) return;
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value);
  }

  // Try to get allocation data from API
  let chartData: {
    name: string;
    value: number;
    itemStyle?: { color: string };
  }[] = [];
  try {
    const alloRes = await getAssetAllocation();
    if (alloRes.data.code === 200 && alloRes.data.data?.tracks) {
      chartData = alloRes.data.data.tracks.map((t) => ({
        name: t.name,
        value: Number(t.percentage),
        itemStyle: t.color ? { color: t.color } : undefined,
      }));
    }
  } catch {
    // Fallback to recommendations data
    chartData = recommendations.value.map((r) => ({
      name: r.trackName,
      value: Number(r.allocationPercentage),
    }));
  }

  const option: echarts.EChartsOption = {
    tooltip: { trigger: "item", formatter: "{b}: {c}% ({d}%)" },
    legend: { orient: "vertical", right: 10, top: "center" },
    series: [
      {
        type: "pie",
        radius: ["40%", "70%"],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: "#fff", borderWidth: 2 },
        label: { show: false },
        data:
          chartData.length > 0 ? chartData : [{ value: 0, name: "暂无数据" }],
      },
    ],
  };
  pieChart.setOption(option, true);
}
</script>

<style scoped lang="scss">
.investment-advice-page {
  .profile-card {
    .profile-content {
      display: flex;
      align-items: center;
      gap: 24px;

      .score-badge {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: #fff;

        &.conservative {
          background: linear-gradient(135deg, var(--cr-success), #43a047);
        }
        &.moderate {
          background: linear-gradient(135deg, var(--cr-warning), #f57c00);
        }
        &.aggressive {
          background: linear-gradient(135deg, var(--cr-error), #d32f2f);
        }

        .score {
          font-size: 24px;
          font-weight: bold;
        }
        .max {
          font-size: 12px;
          opacity: 0.8;
        }
      }

      .profile-info {
        .risk-level {
          font-size: 20px;
          font-weight: bold;
          margin-bottom: 4px;

          &.conservative {
            color: var(--cr-success);
          }
          &.moderate {
            color: var(--cr-warning);
          }
          &.aggressive {
            color: var(--cr-error);
          }
        }

        .assessment-date {
          color: var(--cr-text-secondary);
          font-size: 14px;
        }
      }
    }
  }

  .chart-container {
    height: 300px;
  }

  .recommendation-list {
    .rec-card {
      margin-bottom: 12px;

      .rec-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .track-name {
          font-size: 16px;
          font-weight: bold;
          color: var(--cr-text-primary);
        }
      }

      .rec-desc {
        color: var(--cr-text-secondary);
        font-size: 14px;
        margin: 4px 0;
      }
      .rec-rationale {
        color: var(--cr-text-secondary);
        font-size: 13px;
        margin: 4px 0;
        opacity: 0.8;
      }

      .rec-meta {
        display: flex;
        gap: 16px;
        color: var(--cr-text-secondary);
        font-size: 12px;
        margin-top: 8px;
      }
    }
  }
}
</style>
