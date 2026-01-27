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

    <el-card class="list-card">
      <el-table :data="transactions" stripe style="width: 100%">
        <el-table-column prop="transactionDate" label="日期" width="120" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="description" label="备注" />
        <el-table-column prop="amount" label="金额" width="150">
          <template #default="{ row }">
            <span :class="row.type === 1 ? 'income' : 'expense'">
              {{ row.type === 1 ? '+' : '-' }}¥{{ row.amount.toFixed(2) }}
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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()

const filterForm = reactive({
  dateRange: null as [Date, Date] | null,
  type: null as number | null
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const transactions = ref<any[]>([])

onMounted(() => {
  fetchTransactions()
})

function fetchTransactions() {
  // TODO: Implement API call
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

function handleEdit(row: any) {
  router.push(`/accounting/edit/${row.id}`)
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '提示', {
      type: 'warning'
    })
    // TODO: Call delete API
    ElMessage.success('删除成功')
    fetchTransactions()
  } catch {
    // User cancelled
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
