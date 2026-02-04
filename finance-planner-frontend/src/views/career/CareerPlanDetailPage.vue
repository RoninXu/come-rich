<template>
  <div class="career-plan-detail-page">
    <n-spin :show="loading">
      <PageHeader
        :title="plan?.title || '计划详情'"
        show-back
      >
        <template #actions>
          <template v-if="plan">
            <n-tag
              :type="statusTag(plan.status)"
              size="small"
            >
              {{
                statusLabel(plan.status)
              }}
            </n-tag>
            <n-select
              v-model:value="plan.status"
              :options="statusOptions"
              size="small"
              style="width: 120px"
              @update:value="handleStatusChange"
            />
            <n-button
              type="error"
              @click="handleDelete"
            >
              删除
            </n-button>
          </template>
        </template>
      </PageHeader>

      <template v-if="plan">
        <n-grid
          :x-gap="20"
          :y-gap="20"
          :cols="24"
        >
          <n-gi :span="16">
            <!-- Income Overview -->
            <GlassCard>
              <template #header>
                <div class="card-header">
                  <span>收入概览</span>
                  <n-button
                    type="primary"
                    size="small"
                    @click="showAddIncome = true"
                  >
                    <template #icon>
                      <n-icon><Add /></n-icon>
                    </template>
                    记录收入
                  </n-button>
                </div>
              </template>
              <n-grid
                :x-gap="20"
                :cols="3"
                class="stats-row"
              >
                <n-gi>
                  <div class="stat-item">
                    <div class="stat-label">
                      目标月收入
                    </div>
                    <div class="stat-value">
                      ¥{{ formatNumber(plan.targetMonthlyIncome) }}
                    </div>
                  </div>
                </n-gi>
                <n-gi>
                  <div class="stat-item">
                    <div class="stat-label">
                      本月实际收入
                    </div>
                    <div class="stat-value income">
                      ¥{{ formatNumber(plan.actualMonthlyIncome) }}
                    </div>
                  </div>
                </n-gi>
                <n-gi>
                  <div class="stat-item">
                    <div class="stat-label">
                      完成度
                    </div>
                    <div class="stat-value">
                      {{ incomeProgress }}%
                    </div>
                  </div>
                </n-gi>
              </n-grid>
              <n-progress
                type="line"
                :percentage="Math.min(100, incomeProgress)"
                :height="12"
                :border-radius="6"
                :color="incomeProgress >= 100 ? '#34C759' : '#007AFF'"
                style="margin-top: 16px"
              />

              <!-- Income Chart -->
              <div
                ref="chartRef"
                style="width: 100%; height: 300px; margin-top: 20px"
              />
            </GlassCard>

            <!-- Income History -->
            <GlassCard style="margin-top: 20px">
              <template #header>
                收入记录
              </template>
              <n-empty
                v-if="incomeHistory.length === 0"
                description="暂无收入记录"
              />
              <n-data-table
                v-else
                :columns="incomeColumns"
                :data="incomeHistory"
                :bordered="false"
                striped
              />
            </GlassCard>
          </n-gi>

          <n-gi :span="8">
            <!-- Plan Info -->
            <GlassCard>
              <template #header>
                计划详情
              </template>
              <n-descriptions
                :column="1"
                bordered
                label-placement="left"
              >
                <n-descriptions-item
                  v-if="plan.careerType"
                  label="副业类型"
                >
                  {{ plan.careerType }}
                </n-descriptions-item>
                <n-descriptions-item
                  v-if="plan.matchScore"
                  label="匹配度"
                >
                  <n-progress
                    :percentage="plan.matchScore"
                    :height="6"
                    :border-radius="3"
                    :color="scoreColor(plan.matchScore)"
                    style="width: 100px"
                  />
                </n-descriptions-item>
                <n-descriptions-item
                  v-if="plan.startDate"
                  label="开始日期"
                >
                  {{ plan.startDate }}
                </n-descriptions-item>
                <n-descriptions-item label="创建时间">
                  {{ formatDate(plan.createdAt) }}
                </n-descriptions-item>
                <n-descriptions-item
                  v-if="plan.description"
                  label="描述"
                >
                  {{ plan.description }}
                </n-descriptions-item>
              </n-descriptions>
            </GlassCard>

            <!-- AI Startup Plan -->
            <GlassCard style="margin-top: 20px">
              <template #header>
                AI 90天启动计划
              </template>
              <n-button
                v-if="!plan.startupPlan"
                type="primary"
                :loading="aiLoading"
                block
                @click="handleGenerateStartupPlan"
              >
                生成启动计划
              </n-button>
              <div
                v-if="plan.startupPlan"
                class="startup-plan-content"
                v-html="renderMarkdown(plan.startupPlan)"
              />
            </GlassCard>
          </n-gi>
        </n-grid>
      </template>

      <!-- Add Income Dialog -->
      <n-modal
        v-model:show="showAddIncome"
        preset="card"
        title="记录收入"
        style="width: 400px"
      >
        <n-form
          :model="incomeForm"
          label-placement="left"
          label-width="80"
        >
          <n-form-item
            label="金额"
            required
          >
            <n-input-number
              v-model:value="incomeForm.amount"
              :min="0.01"
              :precision="2"
              :show-button="false"
              style="width: 100%"
            />
          </n-form-item>
          <n-form-item
            label="日期"
            required
          >
            <n-date-picker
              v-model:value="incomeForm.incomeDate"
              type="date"
              style="width: 100%"
            />
          </n-form-item>
          <n-form-item label="描述">
            <n-input
              v-model:value="incomeForm.description"
              placeholder="收入来源描述"
            />
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="showAddIncome = false">
              取消
            </n-button>
            <n-button
              type="primary"
              :loading="incomeSubmitting"
              @click="handleAddIncome"
            >
              确定
            </n-button>
          </n-space>
        </template>
      </n-modal>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick, h } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  NSpin,
  NGrid,
  NGi,
  NButton,
  NIcon,
  NTag,
  NProgress,
  NEmpty,
  NDataTable,
  NDescriptions,
  NDescriptionsItem,
  NSelect,
  NModal,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NDatePicker,
  NSpace,
  useMessage,
  useDialog,
  type DataTableColumns,
} from "naive-ui";
import { Add } from "@vicons/ionicons5";
import dayjs from "dayjs";
import * as echarts from "echarts";
import {
  getCareerPlan,
  updateCareerPlan,
  deleteCareerPlan,
  addCareerIncome,
  getCareerIncomeHistory,
  generateStartupPlan,
} from "@/api/career";
import type { CareerPlan, CareerIncome } from "@/types/career";
import GlassCard from "@/components/common/GlassCard.vue";
import PageHeader from "@/components/common/PageHeader.vue";

const route = useRoute();
const router = useRouter();
const message = useMessage();
const dialog = useDialog();

const loading = ref(false);
const plan = ref<CareerPlan | null>(null);
const incomeHistory = ref<CareerIncome[]>([]);
const showAddIncome = ref(false);
const incomeSubmitting = ref(false);
const aiLoading = ref(false);
const chartRef = ref<HTMLElement>();

const incomeForm = reactive({
  amount: undefined as number | undefined,
  incomeDate: Date.now() as number | null,
  description: "",
});

const statusOptions = [
  { label: "探索中", value: 1 },
  { label: "进行中", value: 2 },
  { label: "已暂停", value: 3 },
  { label: "已完成", value: 4 },
];

const incomeColumns: DataTableColumns<CareerIncome> = [
  { title: "日期", key: "incomeDate", width: 120 },
  {
    title: "金额",
    key: "amount",
    width: 120,
    render: (row) =>
      h(
        "span",
        { style: { color: "var(--cr-success)", fontWeight: "bold" } },
        `+¥${Number(row.amount).toFixed(2)}`,
      ),
  },
  {
    title: "描述",
    key: "description",
    render: (row) => row.description || "-",
  },
];

const incomeProgress = computed(() => {
  if (!plan.value?.targetMonthlyIncome || plan.value.targetMonthlyIncome === 0)
    return 0;
  const actual = plan.value.actualMonthlyIncome || 0;
  return Math.round((actual / plan.value.targetMonthlyIncome) * 100);
});

onMounted(async () => {
  await fetchPlanData();
});

async function fetchPlanData() {
  loading.value = true;
  try {
    const planId = Number(route.params.id);
    const [planRes, incomeRes] = await Promise.all([
      getCareerPlan(planId),
      getCareerIncomeHistory(planId),
    ]);
    if (planRes.data.code === 200) {
      plan.value = planRes.data.data;
    }
    if (incomeRes.data.code === 200) {
      incomeHistory.value = incomeRes.data.data;
      await nextTick();
      renderChart();
    }
  } catch {
    message.error("加载计划详情失败");
  } finally {
    loading.value = false;
  }
}

function renderChart() {
  if (!chartRef.value || incomeHistory.value.length === 0) return;

  const chart = echarts.init(chartRef.value);
  const monthlyMap = new Map<string, number>();
  for (const income of incomeHistory.value) {
    const month = dayjs(income.incomeDate).format("YYYY-MM");
    monthlyMap.set(month, (monthlyMap.get(month) || 0) + Number(income.amount));
  }

  const months = Array.from(monthlyMap.keys()).sort();
  const values = months.map((m) => monthlyMap.get(m) || 0);

  chart.setOption({
    title: {
      text: "月度收入趋势",
      left: "center",
      textStyle: { fontSize: 14 },
    },
    tooltip: { trigger: "axis", formatter: "{b}<br/>收入: ¥{c}" },
    xAxis: { type: "category", data: months },
    yAxis: { type: "value", axisLabel: { formatter: "¥{value}" } },
    series: [
      {
        type: "bar",
        data: values,
        itemStyle: { color: "#34C759", borderRadius: [4, 4, 0, 0] },
        markLine: plan.value?.targetMonthlyIncome
          ? {
              data: [
                {
                  yAxis: Number(plan.value.targetMonthlyIncome),
                  name: "目标",
                  lineStyle: { color: "#FF9500", type: "dashed" },
                },
              ],
            }
          : undefined,
      },
    ],
  });
}

async function handleAddIncome() {
  if (!incomeForm.amount || !incomeForm.incomeDate) {
    message.warning("请填写金额和日期");
    return;
  }
  incomeSubmitting.value = true;
  try {
    await addCareerIncome(Number(route.params.id), {
      amount: incomeForm.amount,
      description: incomeForm.description || undefined,
      incomeDate: dayjs(incomeForm.incomeDate).format("YYYY-MM-DD"),
    });
    message.success("收入已记录");
    showAddIncome.value = false;
    incomeForm.amount = undefined;
    incomeForm.description = "";
    incomeForm.incomeDate = Date.now();
    await fetchPlanData();
  } catch {
    // Handled by interceptor
  } finally {
    incomeSubmitting.value = false;
  }
}

async function handleStatusChange(newStatus: number) {
  if (!plan.value) return;
  try {
    await updateCareerPlan(plan.value.id, {
      title: plan.value.title,
      careerType: plan.value.careerType || undefined,
      description: plan.value.description || undefined,
      targetMonthlyIncome: plan.value.targetMonthlyIncome || undefined,
    });
    message.success("状态已更新");
  } catch {
    // Handled by interceptor
  }
}

async function handleGenerateStartupPlan() {
  aiLoading.value = true;
  try {
    const res = await generateStartupPlan(Number(route.params.id));
    if (res.data.code === 200 && plan.value) {
      plan.value.startupPlan = res.data.data;
    }
  } catch {
    message.error("AI 计划生成失败");
  } finally {
    aiLoading.value = false;
  }
}

function handleDelete() {
  dialog.warning({
    title: "提示",
    content: "确定要删除这个副业计划吗？相关收入记录也会被删除。",
    positiveText: "确定",
    negativeText: "取消",
    onPositiveClick: async () => {
      try {
        await deleteCareerPlan(Number(route.params.id));
        message.success("计划已删除");
        router.push("/career/plans");
      } catch {
        // Handled by interceptor
      }
    },
  });
}

function renderMarkdown(text: string): string {
  return text
    .replace(/\n/g, "<br>")
    .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
    .replace(/#{3}\s(.*?)(?:<br>|$)/g, "<h4>$1</h4>")
    .replace(/#{2}\s(.*?)(?:<br>|$)/g, "<h3>$1</h3>")
    .replace(/#{1}\s(.*?)(?:<br>|$)/g, "<h3>$1</h3>");
}

function formatNumber(value: number | null | undefined): string {
  if (value === undefined || value === null) return "0.00";
  return Number(value).toFixed(2);
}

function formatDate(dateStr: string): string {
  return dayjs(dateStr).format("YYYY-MM-DD HH:mm");
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
.career-plan-detail-page {
  max-width: 1200px;
  margin: 0 auto;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .stats-row {
    .stat-item {
      text-align: center;

      .stat-label {
        font-size: 13px;
        color: var(--cr-text-tertiary);
        margin-bottom: 4px;
      }

      .stat-value {
        font-size: 20px;
        font-weight: bold;
        color: var(--cr-text-primary);

        &.income {
          color: var(--cr-success);
        }
      }
    }
  }

  .startup-plan-content {
    font-size: 14px;
    line-height: 1.8;
    color: var(--cr-text-primary);

    :deep(h3) {
      margin: 16px 0 8px;
      color: var(--cr-primary);
    }

    :deep(h4) {
      margin: 12px 0 6px;
      color: var(--cr-text-primary);
    }

    :deep(strong) {
      color: var(--cr-text-primary);
    }
  }
}
</style>
