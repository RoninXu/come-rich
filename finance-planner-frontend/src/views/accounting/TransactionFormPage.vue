<template>
  <div class="transaction-form-page">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑记录' : '记一笔' }}</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        style="max-width: 500px"
        v-loading="pageLoading"
      >
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type" @change="handleTypeChange">
            <el-radio-button :value="2">支出</el-radio-button>
            <el-radio-button :value="1">收入</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="金额" prop="amount">
          <el-input-number
            v-model="form.amount"
            :min="0.01"
            :precision="2"
            :controls="false"
            placeholder="请输入金额"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-select
            v-model="form.categoryId"
            placeholder="请选择分类"
            style="width: 100%"
          >
            <el-option-group
              v-for="parent in categoryTree"
              :key="parent.id"
              :label="parent.name"
            >
              <el-option
                v-if="!parent.children || parent.children.length === 0"
                :label="parent.name"
                :value="parent.id"
              />
              <el-option
                v-for="child in parent.children"
                :key="child.id"
                :label="child.name"
                :value="child.id"
              />
            </el-option-group>
          </el-select>
        </el-form-item>

        <el-form-item label="日期" prop="transactionDate">
          <el-date-picker
            v-model="form.transactionDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="备注">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="添加备注..."
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            {{ isEdit ? '保存' : '记录' }}
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import dayjs from 'dayjs'
import { getCategoryTree } from '@/api/category'
import { createTransaction, updateTransaction, getTransaction } from '@/api/transaction'
import type { Category, CreateTransactionRequest } from '@/types/accounting'

const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const loading = ref(false)
const pageLoading = ref(false)

const isEdit = computed(() => !!route.params.id)
const transactionId = computed(() => route.params.id ? Number(route.params.id) : null)

const form = reactive({
  type: 2,
  amount: null as number | null,
  categoryId: null as number | null,
  transactionDate: new Date(),
  description: ''
})

// Category data from API
const expenseCategories = ref<Category[]>([])
const incomeCategories = ref<Category[]>([])

// Get category tree based on selected type
const categoryTree = computed(() => {
  return form.type === 1 ? incomeCategories.value : expenseCategories.value
})

const rules: FormRules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  transactionDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

onMounted(async () => {
  pageLoading.value = true
  try {
    // Load categories
    await loadCategories()

    // If editing, load existing transaction
    if (isEdit.value && transactionId.value) {
      await loadTransaction(transactionId.value)
    }
  } finally {
    pageLoading.value = false
  }
})

async function loadCategories() {
  try {
    // Load both expense and income categories
    const [expenseRes, incomeRes] = await Promise.all([
      getCategoryTree(2),
      getCategoryTree(1)
    ])

    if (expenseRes.data.code === 200) {
      expenseCategories.value = expenseRes.data.data
    }
    if (incomeRes.data.code === 200) {
      incomeCategories.value = incomeRes.data.data
    }
  } catch (error) {
    ElMessage.error('加载分类失败')
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
      form.transactionDate = new Date(transaction.transactionDate)
      form.description = transaction.description || ''
    }
  } catch (error) {
    ElMessage.error('加载交易记录失败')
    router.push('/accounting')
  }
}

function handleTypeChange() {
  // Reset category when type changes
  form.categoryId = null
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

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
      // Update existing transaction
      await updateTransaction(transactionId.value, data)
      ElMessage.success('保存成功')
    } else {
      // Create new transaction
      await createTransaction(data)
      ElMessage.success('记录成功')
    }

    router.push('/accounting')
  } catch (error) {
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
