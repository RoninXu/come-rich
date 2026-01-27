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
      >
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
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
            <el-option
              v-for="category in filteredCategories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()

const formRef = ref<FormInstance>()
const loading = ref(false)

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  type: 2,
  amount: null as number | null,
  categoryId: null as number | null,
  transactionDate: new Date(),
  description: ''
})

// Mock categories - will be replaced with API data
const categories = ref([
  { id: 1, name: '餐饮', type: 2 },
  { id: 2, name: '交通', type: 2 },
  { id: 3, name: '购物', type: 2 },
  { id: 4, name: '娱乐', type: 2 },
  { id: 10, name: '工资', type: 1 },
  { id: 11, name: '副业', type: 1 },
  { id: 12, name: '投资收益', type: 1 }
])

const filteredCategories = computed(() =>
  categories.value.filter(c => c.type === form.type)
)

const rules: FormRules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  transactionDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

onMounted(() => {
  if (isEdit.value) {
    // TODO: Fetch transaction by ID
  }
  // TODO: Fetch categories
})

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = {
      ...form,
      transactionDate: dayjs(form.transactionDate).format('YYYY-MM-DD')
    }

    // TODO: Call create/update API
    console.log('Submit:', data)

    ElMessage.success(isEdit.value ? '保存成功' : '记录成功')
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
