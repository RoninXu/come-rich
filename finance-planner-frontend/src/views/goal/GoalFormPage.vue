<template>
  <div class="goal-form-page">
    <n-spin :show="loading">
      <PageHeader :title="isEdit ? '编辑目标' : '新建目标'" show-back />

      <GlassCard>
        <n-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          label-placement="left"
          style="max-width: 600px"
        >
          <n-form-item label="目标名称" path="title">
            <n-input
              v-model:value="form.title"
              placeholder="例如：买车基金、旅行基金"
              maxlength="100"
              show-count
            />
          </n-form-item>

          <n-form-item label="描述" path="description">
            <n-input
              v-model:value="form.description"
              type="textarea"
              :rows="3"
              placeholder="描述你的目标"
              maxlength="500"
              show-count
            />
          </n-form-item>

          <n-form-item label="目标金额" path="targetAmount">
            <n-input-number
              v-model:value="form.targetAmount"
              :min="0.01"
              :precision="2"
              :show-button="false"
              style="width: 100%"
              placeholder="请输入目标金额"
            />
          </n-form-item>

          <n-form-item label="截止日期" path="deadline">
            <n-date-picker
              v-model:value="form.deadlineTs"
              type="date"
              placeholder="选择截止日期"
              :is-date-disabled="disablePastDates"
              style="width: 100%"
            />
          </n-form-item>

          <n-form-item label="优先级" path="priority">
            <n-radio-group v-model:value="form.priority">
              <n-radio :value="1">高</n-radio>
              <n-radio :value="2">中</n-radio>
              <n-radio :value="3">低</n-radio>
            </n-radio-group>
          </n-form-item>

          <n-form-item>
            <n-space>
              <n-button type="primary" @click="handleSubmit" :loading="submitting">
                {{ isEdit ? '保存修改' : '创建目标' }}
              </n-button>
              <n-button @click="goBack">取消</n-button>
            </n-space>
          </n-form-item>
        </n-form>
      </GlassCard>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NSpin, NForm, NFormItem, NInput, NInputNumber,
  NDatePicker, NRadioGroup, NRadio, NButton, NSpace,
  useMessage, type FormInst, type FormRules,
} from 'naive-ui'
import dayjs from 'dayjs'
import { createGoal, getGoal, updateGoal } from '@/api/goal'
import GlassCard from '@/components/common/GlassCard.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const route = useRoute()
const router = useRouter()
const message = useMessage()

const formRef = ref<FormInst>()
const loading = ref(false)
const submitting = ref(false)
const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  description: '',
  targetAmount: undefined as number | undefined,
  deadlineTs: null as number | null,
  priority: 2
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入目标名称', trigger: 'blur' }],
  targetAmount: [{ required: true, type: 'number', message: '请输入目标金额', trigger: 'blur' }],
  deadlineTs: [{ required: true, type: 'number', message: '请选择截止日期', trigger: 'change' }]
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
      form.deadlineTs = goal.deadline ? dayjs(goal.deadline).valueOf() : null
      form.priority = goal.priority
    }
  } catch (error) {
    message.error('加载目标失败')
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
      deadline: form.deadlineTs ? dayjs(form.deadlineTs).format('YYYY-MM-DD') : '',
      priority: form.priority
    }

    if (isEdit.value) {
      await updateGoal(Number(route.params.id), data)
      message.success('目标已更新')
    } else {
      await createGoal(data)
      message.success('目标已创建')
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

function disablePastDates(ts: number): boolean {
  return ts < Date.now() - 86400000
}
</script>

<style scoped lang="scss">
.goal-form-page {
  /* styles handled by GlassCard and PageHeader */
}
</style>
