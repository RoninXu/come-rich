<template>
  <div class="goal-form-page">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑目标' : '新建目标' }}</h2>
    </div>

    <el-card v-loading="loading">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        style="max-width: 600px"
      >
        <el-form-item label="目标名称" prop="title">
          <el-input v-model="form.title" placeholder="例如：买车基金、旅行基金" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="描述你的目标"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="目标金额" prop="targetAmount">
          <el-input-number
            v-model="form.targetAmount"
            :min="0.01"
            :precision="2"
            :controls="false"
            style="width: 100%"
            placeholder="请输入目标金额"
          />
        </el-form-item>

        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker
            v-model="form.deadline"
            type="date"
            placeholder="选择截止日期"
            :disabled-date="disablePastDates"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="优先级" prop="priority">
          <el-radio-group v-model="form.priority">
            <el-radio :value="1">高</el-radio>
            <el-radio :value="2">中</el-radio>
            <el-radio :value="3">低</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '保存修改' : '创建目标' }}
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import dayjs from 'dayjs'
import { createGoal, getGoal, updateGoal } from '@/api/goal'

const route = useRoute()
const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)
const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  description: '',
  targetAmount: undefined as number | undefined,
  deadline: '' as string | Date,
  priority: 2
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入目标名称', trigger: 'blur' }],
  targetAmount: [{ required: true, message: '请输入目标金额', trigger: 'blur' }],
  deadline: [{ required: true, message: '请选择截止日期', trigger: 'change' }]
}

onMounted(async () => {
  if (isEdit.value) {
    await fetchGoal()
  }
})

async function fetchGoal() {
  loading.value = true
  try {
    const res = await getGoal(Number(route.params.id))
    if (res.data.code === 200) {
      const goal = res.data.data
      form.title = goal.title
      form.description = goal.description || ''
      form.targetAmount = goal.targetAmount
      form.deadline = goal.deadline
      form.priority = goal.priority
    }
  } catch (error) {
    ElMessage.error('加载目标失败')
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()

  submitting.value = true
  try {
    const data = {
      title: form.title,
      description: form.description || undefined,
      targetAmount: form.targetAmount!,
      deadline: dayjs(form.deadline).format('YYYY-MM-DD'),
      priority: form.priority
    }

    if (isEdit.value) {
      await updateGoal(Number(route.params.id), data)
      ElMessage.success('目标已更新')
    } else {
      await createGoal(data)
      ElMessage.success('目标已创建')
    }
    router.push('/goals')
  } catch (error) {
    // Error handled by interceptor
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.back()
}

function disablePastDates(date: Date): boolean {
  return date.getTime() < Date.now() - 86400000
}
</script>

<style scoped lang="scss">
.goal-form-page {
  .page-header {
    margin-bottom: 20px;

    h2 { margin: 0; }
  }
}
</style>
