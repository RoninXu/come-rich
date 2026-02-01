<template>
  <div class="budget-form-page" v-loading="loading">
    <div class="page-header">
      <h2>设置预算</h2>
      <el-date-picker
        v-model="selectedMonth"
        type="month"
        placeholder="选择月份"
        format="YYYY年MM月"
        value-format="YYYY-MM"
        @change="fetchCategories"
      />
    </div>

    <el-card>
      <el-form label-width="120px">
        <el-form-item label="月度总预算">
          <el-input-number
            v-model="totalBudget"
            :min="0"
            :precision="2"
            :step="100"
            placeholder="设置月度总预算上限"
            style="width: 300px"
          />
          <el-button type="primary" style="margin-left: 12px" @click="saveTotalBudget">
            保存总预算
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 16px">
      <template #header>分类预算</template>
      <el-empty v-if="categories.length === 0" description="暂无支出分类" />
      <el-form v-else label-width="140px">
        <el-form-item
          v-for="cat in categories"
          :key="cat.id"
          :label="cat.name"
        >
          <el-input-number
            v-model="budgetAmounts[cat.id]"
            :min="0"
            :precision="2"
            :step="100"
            placeholder="输入预算金额"
            style="width: 250px"
          />
          <span style="margin-left: 8px; color: #999">元/月</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveAllBudgets" :loading="saving">保存所有预算</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { setBudget, getBudgets, setBudgetTotal, getBudgetTotal } from '@/api/budget'
import { getCategories } from '@/api/category'

const router = useRouter()
const route = useRoute()

const selectedMonth = ref((route.query.month as string) || dayjs().format('YYYY-MM'))
const loading = ref(false)
const saving = ref(false)
const totalBudget = ref(0)

interface CategoryItem {
  id: number
  name: string
}

const categories = ref<CategoryItem[]>([])
const budgetAmounts = reactive<Record<number, number>>({})

onMounted(() => {
  fetchCategories()
})

async function fetchCategories() {
  loading.value = true
  try {
    // Get expense categories (type=2)
    const catRes = await getCategories(2)
    if (catRes.data.code === 200) {
      // Filter to parent categories only
      categories.value = catRes.data.data
        .filter((c: any) => c.parentId === null || c.parentId === undefined)
        .map((c: any) => ({ id: c.id, name: c.name }))
    }

    // Get existing budgets
    const budgetRes = await getBudgets(selectedMonth.value)
    if (budgetRes.data.code === 200 && budgetRes.data.data) {
      for (const b of budgetRes.data.data) {
        budgetAmounts[b.categoryId] = b.amount
      }
    }

    // Get total budget
    try {
      const totalRes = await getBudgetTotal(selectedMonth.value)
      if (totalRes.data.code === 200 && totalRes.data.data) {
        totalBudget.value = totalRes.data.data.totalAmount
      }
    } catch {
      // No total set yet
    }
  } catch {
    ElMessage.error('加载分类失败')
  } finally {
    loading.value = false
  }
}

async function saveTotalBudget() {
  if (totalBudget.value <= 0) {
    ElMessage.warning('请输入有效的总预算金额')
    return
  }
  try {
    await setBudgetTotal({ yearMonth: selectedMonth.value, totalAmount: totalBudget.value })
    ElMessage.success('总预算保存成功')
  } catch {
    ElMessage.error('保存失败')
  }
}

async function saveAllBudgets() {
  saving.value = true
  try {
    const promises = []
    for (const cat of categories.value) {
      const amount = budgetAmounts[cat.id]
      if (amount && amount > 0) {
        promises.push(setBudget({
          categoryId: cat.id,
          yearMonth: selectedMonth.value,
          amount
        }))
      }
    }
    await Promise.all(promises)
    ElMessage.success('预算保存成功')
    router.push('/budget')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped lang="scss">
.budget-form-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 { margin: 0; }
  }
}
</style>
