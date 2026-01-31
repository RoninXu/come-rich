<template>
  <div class="profile-page">
    <div class="page-header">
      <h2>个人资料</h2>
      <p class="subtitle">完善个人资料，获取更精准的副业推荐</p>
    </div>

    <el-card v-loading="loading">
      <el-form
        ref="formRef"
        :model="form"
        label-width="120px"
        style="max-width: 600px"
      >
        <el-form-item label="当前职业">
          <el-input v-model="form.occupation" placeholder="例如：软件工程师、产品经理" />
        </el-form-item>

        <el-form-item label="技能特长">
          <el-input
            v-model="form.skills"
            type="textarea"
            :rows="3"
            placeholder="例如：Python编程、写作、设计、英语翻译（多个用逗号分隔）"
          />
        </el-form-item>

        <el-form-item label="经验水平">
          <el-select v-model="form.experienceLevel" placeholder="选择经验水平" style="width: 100%">
            <el-option label="初级（0-2年）" value="junior" />
            <el-option label="中级（3-5年）" value="mid" />
            <el-option label="高级（5年以上）" value="senior" />
          </el-select>
        </el-form-item>

        <el-form-item label="每周可用时间">
          <el-input-number
            v-model="form.availableHoursPerWeek"
            :min="1"
            :max="40"
            style="width: 200px"
          />
          <span class="unit-text">小时/周</span>
        </el-form-item>

        <el-form-item label="收入期望">
          <el-input-number
            v-model="form.incomeExpectation"
            :min="0"
            :precision="0"
            :controls="false"
            style="width: 200px"
            placeholder="月收入目标"
          />
          <span class="unit-text">元/月</span>
        </el-form-item>

        <el-form-item label="兴趣爱好">
          <el-input
            v-model="form.interests"
            type="textarea"
            :rows="3"
            placeholder="例如：摄影、烘焙、读书、运动（多个用逗号分隔）"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="submitting">
            保存资料
          </el-button>
          <el-button @click="goToRecommendations">
            查看 AI 推荐
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProfile, saveProfile } from '@/api/profile'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
  occupation: '',
  skills: '',
  experienceLevel: '',
  availableHoursPerWeek: 10 as number | undefined,
  incomeExpectation: undefined as number | undefined,
  interests: ''
})

onMounted(async () => {
  await fetchProfile()
})

async function fetchProfile() {
  loading.value = true
  try {
    const res = await getProfile()
    if (res.data.code === 200 && res.data.data) {
      const profile = res.data.data
      form.occupation = profile.occupation || ''
      form.skills = profile.skills || ''
      form.experienceLevel = profile.experienceLevel || ''
      form.availableHoursPerWeek = profile.availableHoursPerWeek || 10
      form.incomeExpectation = profile.incomeExpectation || undefined
      form.interests = profile.interests || ''
    }
  } catch (error) {
    // First time user - form stays empty
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  submitting.value = true
  try {
    const data: Record<string, any> = {}
    if (form.occupation) data.occupation = form.occupation
    if (form.skills) data.skills = form.skills
    if (form.experienceLevel) data.experienceLevel = form.experienceLevel
    if (form.availableHoursPerWeek) data.availableHoursPerWeek = form.availableHoursPerWeek
    if (form.incomeExpectation) data.incomeExpectation = form.incomeExpectation
    if (form.interests) data.interests = form.interests

    await saveProfile(data)
    ElMessage.success('个人资料已保存')
  } catch (error) {
    // Handled by interceptor
  } finally {
    submitting.value = false
  }
}

function goToRecommendations() {
  router.push('/career')
}
</script>

<style scoped lang="scss">
.profile-page {
  .page-header {
    margin-bottom: 20px;

    h2 { margin: 0 0 8px; }

    .subtitle {
      color: #999;
      font-size: 14px;
      margin: 0;
    }
  }

  .unit-text {
    margin-left: 8px;
    color: #999;
    font-size: 14px;
  }
}
</style>
