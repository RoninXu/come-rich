<template>
  <div class="transaction-form-page">
    <GlassCard>
      <template #header>
        <span>{{ isEdit ? '编辑记录' : '记一笔' }}</span>
      </template>

      <n-spin :show="pageLoading">
        <n-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-placement="left"
          label-width="80"
          style="max-width: 500px"
        >
          <n-form-item label="类型" path="type">
            <n-radio-group v-model:value="form.type" @update:value="handleTypeChange">
              <n-radio-button :value="2">支出</n-radio-button>
              <n-radio-button :value="1">收入</n-radio-button>
            </n-radio-group>
          </n-form-item>

          <n-form-item label="金额" path="amount">
            <n-input-number
              v-model:value="form.amount"
              :min="0.01"
              :precision="2"
              :show-button="false"
              placeholder="请输入金额"
              style="width: 100%"
            />
          </n-form-item>

          <n-form-item label="分类" path="categoryId">
            <n-select
              v-model:value="form.categoryId"
              :options="categoryOptions"
              placeholder="请选择分类"
              style="width: 100%"
            />
          </n-form-item>

          <n-form-item label="日期" path="transactionDate">
            <n-date-picker
              v-model:value="form.transactionDate"
              type="date"
              style="width: 100%"
            />
          </n-form-item>

          <n-form-item label="备注">
            <n-input
              v-model:value="form.description"
              type="textarea"
              :rows="3"
              placeholder="添加备注..."
            />
          </n-form-item>

          <n-form-item>
            <n-space>
              <n-button type="primary" :loading="loading" @click="handleSubmit">
                {{ isEdit ? '保存' : '记录' }}
              </n-button>
              <n-button @click="handleCancel">取消</n-button>
            </n-space>
          </n-form-item>
        </n-form>
      </n-spin>
    </GlassCard>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  NForm, NFormItem, NInput, NInputNumber, NSelect,
  NDatePicker, NRadioGroup, NRadioButton,
  NButton, NSpace, NSpin,
  useMessage,
  type FormInst, type FormRules, type SelectGroupOption,
} from 'naive-ui'
import dayjs from 'dayjs'
import { getCategoryTree } from '@/api/category'
import { createTransaction, updateTransaction, getTransaction } from '@/api/transaction'
import type { Category, CreateTransactionRequest } from '@/types/accounting'
import GlassCard from '@/components/common/GlassCard.vue'

const router = useRouter()
const route = useRoute()
const message = useMessage()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const pageLoading = ref(false)

const isEdit = computed(() => !!route.params.id)
const transactionId = computed(() => route.params.id ? Number(route.params.id) : null)

const form = reactive({
  type: 2,
  amount: null as number | null,
  categoryId: null as number | null,
  transactionDate: Date.now(),
  description: ''
})

const expenseCategories = ref<Category[]>([])
const incomeCategories = ref<Category[]>([])

const categoryOptions = computed(() => {
  const cats = form.type === 1 ? incomeCategories.value : expenseCategories.value
  return cats.map((parent): SelectGroupOption => ({
    type: 'group' as const,
    label: parent.name,
    key: parent.id,
    children: (parent.children && parent.children.length > 0)
      ? parent.children.map(child => ({ label: child.name, value: child.id }))
      : [{ label: parent.name, value: parent.id }]
  }))
})

const rules: FormRules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  amount: [{ type: 'number', required: true, message: '请输入金额', trigger: 'blur' }],
  categoryId: [{ type: 'number', required: true, message: '请选择分类', trigger: 'change' }],
  transactionDate: [{ type: 'number', required: true, message: '请选择日期', trigger: 'change' }]
}

onMounted(async () => {
  pageLoading.value = true
  try {
    await loadCategories()
    if (isEdit.value && transactionId.value) {
      await loadTransaction(transactionId.value)
    }
  } finally {
    pageLoading.value = false
  }
})

async function loadCategories() {
  try {
    const [expenseRes, incomeRes] = await Promise.all([
      getCategoryTree(2),
      getCategoryTree(1)
    ])
    if (expenseRes.data.code === 200) expenseCategories.value = expenseRes.data.data
    if (incomeRes.data.code === 200) incomeCategories.value = incomeRes.data.data
  } catch {
    message.error('加载分类失败')
  }
}

async function loadTransaction(id: number) {
  try {
    const res = await getTransaction(id)
    if (res.data.code === 200) {
      const transaction = res.data.data
      form.type = transaction.type
      form.amount = transaction.amount
      form.categoryId = transaction.categoryId
      form.transactionDate = new Date(transaction.transactionDate).getTime()
      form.description = transaction.description || ''
    }
  } catch {
    message.error('加载交易记录失败')
    router.push('/accounting')
  }
}

function handleTypeChange() {
  form.categoryId = null
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const data: CreateTransactionRequest = {
      type: form.type,
      amount: form.amount!,
      categoryId: form.categoryId!,
      transactionDate: dayjs(form.transactionDate).format('YYYY-MM-DD'),
      description: form.description || undefined
    }

    if (isEdit.value && transactionId.value) {
      await updateTransaction(transactionId.value, data)
      message.success('保存成功')
    } else {
      await createTransaction(data)
      message.success('记录成功')
    }

    router.push('/accounting')
  } catch {
    // Error handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  router.back()
}
</script>

<style scoped lang="scss">
.transaction-form-page {
  max-width: 600px;
}
</style>
