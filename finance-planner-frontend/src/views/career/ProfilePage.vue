<template>
  <div class="profile-page">
    <n-spin :show="loading">
      <PageHeader title="个人资料">
        <template #subtitle>
          完善个人资料，获取更精准的副业推荐
        </template>
      </PageHeader>

      <GlassCard>
        <n-form
          :model="form"
          label-placement="left"
          label-width="120"
          style="max-width: 600px"
        >
          <n-form-item label="当前职业">
            <n-input
              v-model:value="form.occupation"
              placeholder="例如：软件工程师、产品经理"
            />
          </n-form-item>

          <n-form-item label="技能特长">
            <n-input
              v-model:value="form.skills"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 5 }"
              placeholder="例如：Python编程、写作、设计、英语翻译（多个用逗号分隔）"
            />
          </n-form-item>

          <n-form-item label="经验水平">
            <n-select
              v-model:value="form.experienceLevel"
              :options="experienceOptions"
              placeholder="选择经验水平"
              style="width: 100%"
            />
          </n-form-item>

          <n-form-item label="每周可用时间">
            <n-space align="center">
              <n-input-number
                v-model:value="form.availableHoursPerWeek"
                :min="1"
                :max="40"
                style="width: 200px"
              />
              <span class="unit-text">小时/周</span>
            </n-space>
          </n-form-item>

          <n-form-item label="收入期望">
            <n-space align="center">
              <n-input-number
                v-model:value="form.incomeExpectation"
                :min="0"
                :precision="0"
                :show-button="false"
                style="width: 200px"
                placeholder="月收入目标"
              />
              <span class="unit-text">元/月</span>
            </n-space>
          </n-form-item>

          <n-form-item label="兴趣爱好">
            <n-input
              v-model:value="form.interests"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 5 }"
              placeholder="例如：摄影、烘焙、读书、运动（多个用逗号分隔）"
            />
          </n-form-item>

          <n-form-item>
            <n-space>
              <n-button
                type="primary"
                :loading="submitting"
                @click="handleSave"
              >
                保存资料
              </n-button>
              <n-button @click="goToRecommendations">
                查看 AI 推荐
              </n-button>
            </n-space>
          </n-form-item>
        </n-form>
      </GlassCard>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  NSpin,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NSelect,
  NButton,
  NSpace,
  useMessage,
} from "naive-ui";
import { getProfile, saveProfile } from "@/api/profile";
import GlassCard from "@/components/common/GlassCard.vue";
import PageHeader from "@/components/common/PageHeader.vue";

const router = useRouter();
const message = useMessage();
const loading = ref(false);
const submitting = ref(false);

const experienceOptions = [
  { label: "初级（0-2年）", value: "junior" },
  { label: "中级（3-5年）", value: "mid" },
  { label: "高级（5年以上）", value: "senior" },
];

const form = reactive({
  occupation: "",
  skills: "",
  experienceLevel: null as string | null,
  availableHoursPerWeek: 10 as number | undefined,
  incomeExpectation: undefined as number | undefined,
  interests: "",
});

onMounted(async () => {
  await fetchProfile();
});

async function fetchProfile() {
  loading.value = true;
  try {
    const res = await getProfile();
    if (res.data.code === 200 && res.data.data) {
      const profile = res.data.data;
      form.occupation = profile.occupation || "";
      form.skills = profile.skills || "";
      form.experienceLevel = profile.experienceLevel || null;
      form.availableHoursPerWeek = profile.availableHoursPerWeek || 10;
      form.incomeExpectation = profile.incomeExpectation || undefined;
      form.interests = profile.interests || "";
    }
  } catch {
    // First time user - form stays empty
  } finally {
    loading.value = false;
  }
}

async function handleSave() {
  submitting.value = true;
  try {
    const data: Record<string, any> = {};
    if (form.occupation) data.occupation = form.occupation;
    if (form.skills) data.skills = form.skills;
    if (form.experienceLevel) data.experienceLevel = form.experienceLevel;
    if (form.availableHoursPerWeek)
      data.availableHoursPerWeek = form.availableHoursPerWeek;
    if (form.incomeExpectation) data.incomeExpectation = form.incomeExpectation;
    if (form.interests) data.interests = form.interests;

    await saveProfile(data);
    message.success("个人资料已保存");
  } catch {
    // Handled by interceptor
  } finally {
    submitting.value = false;
  }
}

function goToRecommendations() {
  router.push("/career");
}
</script>

<style scoped lang="scss">
.profile-page {
  max-width: 800px;
  margin: 0 auto;

  .unit-text {
    color: var(--cr-text-tertiary);
    font-size: 14px;
  }
}
</style>
