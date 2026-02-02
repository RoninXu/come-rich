<template>
  <div class="transaction-list-page">
    <PageHeader title="记账记录">
      <template #actions>
        <n-button type="primary" @click="goToNew">
          <template #icon><n-icon><Add /></n-icon></template>
          记一笔
        </n-button>
        <n-button @click="goToOcr">
          <template #icon><n-icon><Camera /></n-icon></template>
          拍照记账
        </n-button>
        <n-dropdown :options="exportOptions" @select="handleExport">
          <n-button>
            <template #icon><n-icon><Download /></n-icon></template>
            导出
          </n-button>
        </n-dropdown>
      </template>
    </PageHeader>

    <GlassCard class="filter-card">
      <n-space align="center" :wrap="true">
        <n-space :size="0">
          <n-button
            v-for="preset in datePresets"
            :key="preset.key"
            :type="filterForm.activePreset === preset.key ? 'primary' : 'default'"
            size="small"
            @click="setDatePreset(preset.key)"
          >
            {{ preset.label }}
          </n-button>
        </n-space>
        <n-date-picker
          v-model:value="filterForm.dateRange"
          type="daterange"
          clearable
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @update:value="handleDateRangeChange"
        />
        <n-select
          v-model:value="filterForm.type"
          :options="typeOptions"
          placeholder="全部类型"
          clearable
          style="width: 120px"
        />
        <n-button type="primary" @click="handleFilter">查询</n-button>
        <n-button @click="resetFilter">重置</n-button>
      </n-space>
    </GlassCard>

    <GlassCard>
      <n-spin :show="loading">
        <n-empty v-if="!loading && transactions.length === 0" description="暂无记录" />
        <template v-else>
          <n-data-table
            :columns="columns"
            :data="transactions"
            :bordered="false"
            :single-line="false"
            striped
          />
          <div class="pagination">
            <n-pagination
              v-model:page="pagination.page"
              v-model:page-size="pagination.pageSize"
              :item-count="pagination.total"
              :page-sizes="[10, 20, 50]"
              show-size-picker
              @update:page="handlePageChange"
              @update:page-size="handleSizeChange"
            />
          </div>
        </template>
      </n-spin>
    </GlassCard>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import {
  NSpin, NEmpty, NDataTable, NPagination, NDatePicker,
  NSelect, NButton, NIcon, NDropdown, NSpace,
  useMessage, useDialog,
  type DataTableColumns,
} from 'naive-ui'
import { Add, Camera, Download } from '@vicons/ionicons5'
import dayjs from 'dayjs'
import { getTransactions, deleteTransaction } from '@/api/transaction'
import { exportTransactionsExcel, exportTransactionsCsv } from '@/api/export'
import { downloadBlob } from '@/utils/export'
import type { Transaction, TransactionQueryParams } from '@/types/accounting'
import GlassCard from '@/components/common/GlassCard.vue'
import PageHeader from '@/components/common/PageHeader.vue'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const loading = ref(false)

const filterForm = reactive({
  dateRange: null as [number, number] | null,
  type: null as number | null,
  activePreset: null as string | null
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const transactions = ref<Transaction[]>([])

const datePresets = [
  { key: 'today', label: '今天' },
  { key: 'week', label: '本周' },
  { key: 'month', label: '本月' },
]

const typeOptions = [
  { label: '收入', value: 1 },
  { label: '支出', value: 2 },
]

const exportOptions = [
  { label: '导出Excel', key: 'excel' },
  { label: '导出CSV', key: 'csv' },
]

const columns: DataTableColumns<Transaction> = [
  { title: '日期', key: 'transactionDate', width: 120 },
  {
    title: '分类', key: 'categoryName', width: 120,
    render: (row) => row.categoryName || '未分类'
  },
  {
    title: '备注', key: 'description',
    render: (row) => row.description || '-'
  },
  {
    title: '金额', key: 'amount', width: 150,
    render: (row) => {
      const cls = row.type === 1 ? 'income' : 'expense'
      const prefix = row.type === 1 ? '+' : '-'
      return h('span', { class: cls }, `${prefix}¥${Number(row.amount).toFixed(2)}`)
    }
  },
  {
    title: '操作', key: 'actions', width: 150,
    render: (row) => {
      return h(NSpace, { size: 'small' }, () => [
        h(NButton, { text: true, type: 'primary', onClick: () => handleEdit(row) }, () => '编辑'),
        h(NButton, { text: true, type: 'error', onClick: () => handleDelete(row) }, () => '删除'),
      ])
    }
  },
]

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
  } catch {
    message.error('加载记录失败')
  } finally {
    loading.value = false
  }
}

function goToNew() { router.push('/accounting/new') }
function goToOcr() { router.push('/accounting/ocr') }

function handleFilter() {
  pagination.page = 1
  fetchTransactions()
}

function resetFilter() {
  filterForm.dateRange = null
  filterForm.type = null
  filterForm.activePreset = null
  handleFilter()
}

function setDatePreset(preset: string) {
  const today = dayjs()
  let start: number
  let end: number

  switch (preset) {
    case 'today':
      start = today.startOf('day').valueOf()
      end = today.endOf('day').valueOf()
      break
    case 'week':
      start = today.startOf('week').valueOf()
      end = today.endOf('week').valueOf()
      break
    case 'month':
      start = today.startOf('month').valueOf()
      end = today.endOf('month').valueOf()
      break
    default:
      return
  }

  filterForm.dateRange = [start, end]
  filterForm.activePreset = preset
  handleFilter()
}

function handleDateRangeChange() {
  filterForm.activePreset = null
}

function handleEdit(row: Transaction) {
  router.push(`/accounting/edit/${row.id}`)
}

function handleDelete(row: Transaction) {
  dialog.warning({
    title: '提示',
    content: '确定要删除这条记录吗？',
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteTransaction(row.id)
        message.success('删除成功')
        fetchTransactions()
      } catch {
        // API error handled by interceptor
      }
    }
  })
}

function handleSizeChange(pageSize: number) {
  pagination.pageSize = pageSize
  pagination.page = 1
  fetchTransactions()
}

function handlePageChange(page: number) {
  pagination.page = page
  fetchTransactions()
}

async function handleExport(command: string | number) {
  const startDate = filterForm.dateRange?.[0]
    ? dayjs(filterForm.dateRange[0]).format('YYYY-MM-DD')
    : dayjs().startOf('month').format('YYYY-MM-DD')
  const endDate = filterForm.dateRange?.[1]
    ? dayjs(filterForm.dateRange[1]).format('YYYY-MM-DD')
    : dayjs().format('YYYY-MM-DD')
  const type = filterForm.type ?? undefined

  try {
    if (command === 'excel') {
      const res = await exportTransactionsExcel(startDate, endDate, type)
      downloadBlob(new Blob([res.data]), `交易记录_${startDate}_${endDate}.xlsx`)
    } else {
      const res = await exportTransactionsCsv(startDate, endDate, type)
      downloadBlob(new Blob([res.data]), `交易记录_${startDate}_${endDate}.csv`)
    }
    message.success('导出成功')
  } catch {
    message.error('导出失败')
  }
}
</script>

<style scoped lang="scss">
.transaction-list-page {
  .filter-card {
    margin-bottom: 16px;
  }

  .income {
    color: var(--cr-success);
    font-weight: 600;
  }

  .expense {
    color: var(--cr-error);
    font-weight: 600;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
