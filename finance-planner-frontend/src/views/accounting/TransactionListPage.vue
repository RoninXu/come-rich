<template>
  <div class="transaction-list-page">
    <div class="page-header">
      <h2>记账记录</h2>
      <el-button type="primary" @click="goToNew">
        <el-icon><Plus /></el-icon>
        记一笔
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filterForm.type" placeholder="全部" clearable>
            <el-option label="收入" :value="1" />
            <el-option label="支出" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="list-card" v-loading="loading">
      <el-empty v-if="!loading && transactions.length === 0" description="暂无记录" />

      <template v-else>
        <el-table :data="transactions" stripe style="width: 100%">
          <el-table-column prop="transactionDate" label="日期" width="120" />
          <el-table-column label="分类" width="120">
            <template #default="{ row }">
              <span>{{ row.categoryName || '未分类' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="备注">
            <template #default="{ row }">
              <span>{{ row.description || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="150">
            <template #default="{ row }">
              <span :class="row.type === 1 ? 'income' : 'expense'">
                {{ row.type === 1 ? '+' : '-' }}¥{{ Number(row.amount).toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getTransactions, deleteTransaction } from '@/api/transaction'
import type { Transaction, TransactionQueryParams } from '@/types/accounting'

const router = useRouter()

const loading = ref(false)

const filterForm = reactive({
  dateRange: null as [Date, Date] | null,
  type: null as number | null
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const transactions = ref<Transaction[]>([])

onMounted(() => {
  fetchTransactions()
})

async function fetchTransactions() {
  loading.value = true
  try {
    const params: TransactionQueryParams = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }

    if (filterForm.type !== null) {
      params.type = filterForm.type
    }

    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = dayjs(filterForm.dateRange[0]).format('YYYY-MM-DD')
      params.endDate = dayjs(filterForm.dateRange[1]).format('YYYY-MM-DD')
    }

    const res = await getTransactions(params)
    if (res.data.code === 200) {
      const pageData = res.data.data
      transactions.value = pageData.list
      pagination.total = pageData.total
    }
  } catch (error) {
    ElMessage.error('加载记录失败')
  } finally {
    loading.value = false
  }
}

function goToNew() {
  router.push('/accounting/new')
}

function handleFilter() {
  pagination.page = 1
  fetchTransactions()
}

function resetFilter() {
  filterForm.dateRange = null
  filterForm.type = null
  handleFilter()
}

function handleEdit(row: Transaction) {
  router.push(`/accounting/edit/${row.id}`)
}

async function handleDelete(row: Transaction) {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      type: 'warning'
    })

    await deleteTransaction(row.id)
    ElMessage.success('删除成功')
    fetchTransactions()
  } catch (error: any) {
    // User cancelled or API error (API error handled by interceptor)
    if (error !== 'cancel') {
      console.error('Delete error:', error)
    }
  }
}

function handleSizeChange() {
  pagination.page = 1
  fetchTransactions()
}

function handlePageChange() {
  fetchTransactions()
}
</script>

<style scoped lang="scss">
.transaction-list-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
    }
  }

  .filter-card {
    margin-bottom: 20px;
  }

  .list-card {
    .income {
      color: #66BB6A;
      font-weight: bold;
    }

    .expense {
      color: #FF7043;
      font-weight: bold;
    }

    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
