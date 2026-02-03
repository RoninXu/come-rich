<template>
  <div class="health-score-page">
    <n-spin :show="loading">
      <PageHeader title="财务健康评分">
        <template #actions>
          <n-button
            :loading="exporting"
            @click="handleExportHealth"
          >
            <template #icon>
              <n-icon><Download /></n-icon>
            </template>
            导出报告
          </n-button>
        </template>
      </PageHeader>

      <n-grid
        :x-gap="20"
        :y-gap="20"
        :cols="2"
      >
        <n-gi>
          <GlassCard>
            <div class="score-display">
              <div
                ref="gaugeRef"
                class="gauge-chart"
              />
              <div class="score-text">
                <span class="score">{{ healthScore.totalScore }}</span>
                <span class="label">{{ scoreLevel }}</span>
              </div>
            </div>
          </GlassCard>
        </n-gi>
        <n-gi>
          <GlassCard>
            <template #header>
              评分明细
            </template>
            <div class="breakdown-list">
              <div class="breakdown-item">
                <div class="item-header">
                  <span class="name">储蓄能力</span>
                  <span class="score-label">{{ healthScore.savingAbility }}/30</span>
                </div>
                <n-progress
                  type="line"
                  :percentage="(healthScore.savingAbility / 30) * 100"
                  :height="8"
                  :border-radius="4"
                  :show-indicator="false"
                  :color="getProgressColor(healthScore.savingDetail?.status)"
                />
                <div
                  v-if="healthScore.savingDetail"
                  class="item-desc"
                >
                  {{ healthScore.savingDetail.description }}
                </div>
              </div>
              <div class="breakdown-item">
                <div class="item-header">
                  <span class="name">收支平衡</span>
                  <span class="score-label">{{ healthScore.balanceRatio }}/25</span>
                </div>
                <n-progress
                  type="line"
                  :percentage="(healthScore.balanceRatio / 25) * 100"
                  :height="8"
                  :border-radius="4"
                  :show-indicator="false"
                  :color="getProgressColor(healthScore.balanceDetail?.status)"
                />
                <div
                  v-if="healthScore.balanceDetail"
                  class="item-desc"
                >
                  {{ healthScore.balanceDetail.description }}
                </div>
              </div>
              <div class="breakdown-item">
                <div class="item-header">
                  <span class="name">消费结构</span>
                  <span class="score-label">{{ healthScore.consumptionStructure }}/20</span>
                </div>
                <n-progress
                  type="line"
                  :percentage="(healthScore.consumptionStructure / 20) * 100"
                  :height="8"
                  :border-radius="4"
                  :show-indicator="false"
                  :color="
                    getProgressColor(healthScore.consumptionDetail?.status)
                  "
                />
                <div
                  v-if="healthScore.consumptionDetail"
                  class="item-desc"
                >
                  {{ healthScore.consumptionDetail.description }}
                </div>
              </div>
              <div class="breakdown-item">
                <div class="item-header">
                  <span class="name">资产增长</span>
                  <span class="score-label">{{ healthScore.assetGrowth }}/15</span>
                </div>
                <n-progress
                  type="line"
                  :percentage="(healthScore.assetGrowth / 15) * 100"
                  :height="8"
                  :border-radius="4"
                  :show-indicator="false"
                  :color="getProgressColor(healthScore.growthDetail?.status)"
                />
                <div
                  v-if="healthScore.growthDetail"
                  class="item-desc"
                >
                  {{ healthScore.growthDetail.description }}
                </div>
              </div>
              <div class="breakdown-item">
                <div class="item-header">
                  <span class="name">记账习惯</span>
                  <span class="score-label">{{ healthScore.recordingHabit }}/10</span>
                </div>
                <n-progress
                  type="line"
                  :percentage="(healthScore.recordingHabit / 10) * 100"
                  :height="8"
                  :border-radius="4"
                  :show-indicator="false"
                  :color="getProgressColor(healthScore.habitDetail?.status)"
                />
                <div
                  v-if="healthScore.habitDetail"
                  class="item-desc"
                >
                  {{ healthScore.habitDetail.description }}
                </div>
              </div>
            </div>
          </GlassCard>
        </n-gi>
      </n-grid>

      <GlassCard
        v-if="adviceList.length > 0"
        style="margin-top: 20px"
      >
        <template #header>
          改进建议
        </template>
        <div class="advice-list">
          <n-alert
            v-for="(advice, index) in adviceList"
            :key="index"
            :title="advice.title"
            :type="advice.type"
            style="margin-bottom: 12px"
          >
            {{ advice.description }}
          </n-alert>
        </div>
      </GlassCard>

      <GlassCard
        v-else
        style="margin-top: 20px"
      >
        <template #header>
          改进建议
        </template>
        <n-empty description="暂无建议，请先记录一些交易数据" />
      </GlassCard>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from "vue";
import * as echarts from "echarts";
import {
  NSpin,
  NGrid,
  NGi,
  NButton,
  NIcon,
  NProgress,
  NAlert,
  NEmpty,
  useMessage,
} from "naive-ui";
import { Download } from "@vicons/ionicons5";
import { getHealthScore } from "@/api/analysis";
import { exportAnnualReport } from "@/api/export";
import { downloadBlob } from "@/utils/export";
import type { HealthScore, ScoreDetail } from "@/types/analysis";
import GlassCard from "@/components/common/GlassCard.vue";
import PageHeader from "@/components/common/PageHeader.vue";

const message = useMessage();
const gaugeRef = ref<HTMLElement>();
let gaugeChart: echarts.ECharts | null = null;
const loading = ref(false);
const exporting = ref(false);

const healthScore = reactive({
  totalScore: 0,
  grade: "",
  savingAbility: 0,
  balanceRatio: 0,
  assetGrowth: 0,
  consumptionStructure: 0,
  recordingHabit: 0,
  savingDetail: null as ScoreDetail | null,
  balanceDetail: null as ScoreDetail | null,
  consumptionDetail: null as ScoreDetail | null,
  growthDetail: null as ScoreDetail | null,
  habitDetail: null as ScoreDetail | null,
  suggestions: [] as string[],
});

const scoreLevel = computed(() => {
  if (healthScore.grade) return healthScore.grade;
  const score = healthScore.totalScore;
  if (score >= 90) return "优秀";
  if (score >= 70) return "良好";
  if (score >= 50) return "一般";
  return "较差";
});

interface Advice {
  type: "warning" | "info" | "success";
  title: string;
  description: string;
}

const adviceList = computed<Advice[]>(() => {
  if (healthScore.suggestions.length > 0) {
    return healthScore.suggestions.map((suggestion, index) => ({
      type: index === 0 ? ("warning" as const) : ("info" as const),
      title: `建议 ${index + 1}`,
      description: suggestion,
    }));
  }
  return [];
});

onMounted(() => {
  fetchHealthScoreData();
});

async function fetchHealthScoreData() {
  loading.value = true;
  try {
    const res = await getHealthScore();
    if (res.data.code === 200 && res.data.data) {
      const data = res.data.data;
      healthScore.totalScore = data.totalScore;
      healthScore.grade = data.grade;
      healthScore.savingAbility = data.savingAbility;
      healthScore.balanceRatio = data.balanceRatio;
      healthScore.consumptionStructure = data.consumptionStructure;
      healthScore.assetGrowth = data.assetGrowth;
      healthScore.recordingHabit = data.recordingHabit;
      healthScore.savingDetail = data.savingDetail;
      healthScore.balanceDetail = data.balanceDetail;
      healthScore.consumptionDetail = data.consumptionDetail;
      healthScore.growthDetail = data.growthDetail;
      healthScore.habitDetail = data.habitDetail;
      healthScore.suggestions = data.suggestions || [];
    }

    nextTick(() => {
      initGaugeChart();
    });
  } catch {
    message.error("加载健康评分失败");
  } finally {
    loading.value = false;
  }
}

function initGaugeChart() {
  if (!gaugeRef.value) return;

  gaugeChart = echarts.init(gaugeRef.value);

  gaugeChart.setOption({
    series: [
      {
        type: "gauge",
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max: 100,
        splitNumber: 10,
        axisLine: {
          lineStyle: {
            width: 20,
            color: [
              [0.5, "#FF3B30"],
              [0.7, "#FF9500"],
              [0.9, "#34C759"],
              [1, "#30D158"],
            ],
          },
        },
        pointer: { itemStyle: { color: "var(--cr-text-primary, #333)" } },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { show: false },
        detail: { show: false },
        data: [{ value: healthScore.totalScore }],
      },
    ],
  });
}

async function handleExportHealth() {
  exporting.value = true;
  try {
    const year = new Date().getFullYear();
    const res = await exportAnnualReport(year);
    downloadBlob(new Blob([res.data]), `年度报表_${year}.xlsx`);
    message.success("导出成功");
  } catch {
    message.error("导出失败");
  } finally {
    exporting.value = false;
  }
}

function getProgressColor(status?: string): string {
  switch (status) {
    case "good":
      return "#34C759";
    case "average":
      return "#FF9500";
    case "poor":
      return "#FF3B30";
    default:
      return "#007AFF";
  }
}
</script>

<style scoped lang="scss">
.health-score-page {
  max-width: 1200px;
  margin: 0 auto;

  .score-display {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;

    .gauge-chart {
      width: 100%;
      height: 200px;
    }

    .score-text {
      position: absolute;
      bottom: 30px;
      text-align: center;

      .score {
        display: block;
        font-size: 48px;
        font-weight: bold;
        color: var(--cr-text-primary);
      }

      .label {
        font-size: 16px;
        color: var(--cr-text-tertiary);
      }
    }
  }

  .breakdown-list {
    .breakdown-item {
      margin-bottom: 20px;

      &:last-child {
        margin-bottom: 0;
      }

      .item-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;

        .name {
          color: var(--cr-text-primary);
        }
        .score-label {
          color: var(--cr-text-tertiary);
        }
      }

      .item-desc {
        font-size: 12px;
        color: var(--cr-text-tertiary);
        margin-top: 4px;
      }
    }
  }

  .advice-list {
    .n-alert:last-child {
      margin-bottom: 0;
    }
  }
}
</style>
