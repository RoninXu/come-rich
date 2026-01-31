<template>
  <div class="ocr-import-page">
    <div class="page-header">
      <h2>拍照记账</h2>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>上传小票/账单</template>
          <el-upload
            class="upload-area"
            drag
            accept="image/*"
            :auto-upload="false"
            :show-file-list="false"
            @change="handleFileChange"
          >
            <el-icon class="el-icon--upload" :size="60"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将小票图片拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">支持 jpg/png 格式，不超过 10MB</div>
            </template>
          </el-upload>

          <el-button
            type="primary"
            :loading="uploading"
            @click="handleUpload"
            :disabled="!selectedFile"
            style="width: 100%; margin-top: 16px"
          >
            开始识别
          </el-button>
        </el-card>

        <!-- Pending Records -->
        <el-card style="margin-top: 20px" v-if="pendingRecords.length > 0">
          <template #header>
            <span>待确认记录 ({{ pendingRecords.length }})</span>
          </template>
          <div
            v-for="record in pendingRecords"
            :key="record.id"
            class="pending-item"
            :class="{ active: currentPreview?.id === record.id }"
            @click="selectRecord(record)"
          >
            <div class="pending-info">
              <span class="filename">{{ record.originalFilename || '小票' }}</span>
              <span class="amount" v-if="record.extractedAmount">¥{{ Number(record.extractedAmount).toFixed(2) }}</span>
            </div>
            <span class="date">{{ record.extractedDate || '-' }}</span>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card v-if="currentPreview" v-loading="confirming">
          <template #header>
            <div class="card-header">
              <span>识别结果</span>
              <div>
                <el-button type="primary" size="small" @click="handleConfirm">确认记账</el-button>
                <el-button type="info" size="small" @click="handleReject">忽略</el-button>
              </div>
            </div>
          </template>

          <el-form :model="confirmForm" label-width="80px">
            <el-form-item label="金额" required>
              <el-input-number
                v-model="confirmForm.amount"
                :min="0.01"
                :precision="2"
                :controls="false"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="分类" required>
              <el-select v-model="confirmForm.categoryId" placeholder="选择分类" style="width: 100%">
                <el-option
                  v-for="cat in expenseCategories"
                  :key="cat.id"
                  :label="cat.name"
                  :value="cat.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="日期" required>
              <el-date-picker
                v-model="confirmForm.transactionDate"
                type="date"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="商家">
              <el-input v-model="confirmForm.merchant" placeholder="商家名称" />
            </el-form-item>

            <el-form-item label="描述">
              <el-input v-model="confirmForm.description" placeholder="备注说明" />
            </el-form-item>
          </el-form>

          <!-- Raw OCR Text -->
          <el-collapse>
            <el-collapse-item title="原始识别文本">
              <pre class="ocr-raw-text">{{ currentPreview.ocrRawText }}</pre>
            </el-collapse-item>
          </el-collapse>
        </el-card>

        <el-card v-else>
          <el-empty description="上传小票图片开始识别" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { uploadReceipt, confirmOcrRecord, rejectOcrRecord, getPendingOcrRecords } from '@/api/ocr'
import { getExpenseCategories } from '@/api/category'
import type { OcrPreview } from '@/types/ocr'
import type { Category } from '@/types/accounting'
import type { UploadFile } from 'element-plus'

const uploading = ref(false)
const confirming = ref(false)
const selectedFile = ref<File | null>(null)
const currentPreview = ref<OcrPreview | null>(null)
const pendingRecords = ref<OcrPreview[]>([])
const expenseCategories = ref<Category[]>([])

const confirmForm = reactive({
  amount: undefined as number | undefined,
  categoryId: undefined as number | undefined,
  transactionDate: '' as string | Date,
  merchant: '',
  description: ''
})

onMounted(async () => {
  await Promise.all([fetchPendingRecords(), fetchCategories()])
})

async function fetchPendingRecords() {
  try {
    const res = await getPendingOcrRecords()
    if (res.data.code === 200) {
      pendingRecords.value = res.data.data
    }
  } catch (error) {
    // silently fail
  }
}

async function fetchCategories() {
  try {
    const res = await getExpenseCategories()
    if (res.data.code === 200) {
      expenseCategories.value = res.data.data
    }
  } catch (error) {
    // silently fail
  }
}

function handleFileChange(uploadFile: UploadFile) {
  selectedFile.value = uploadFile.raw || null
}

async function handleUpload() {
  if (!selectedFile.value) return

  uploading.value = true
  try {
    const res = await uploadReceipt(selectedFile.value)
    if (res.data.code === 200) {
      const preview = res.data.data
      currentPreview.value = preview
      fillConfirmForm(preview)
      ElMessage.success('识别完成')
      selectedFile.value = null
      await fetchPendingRecords()
    }
  } catch (error) {
    ElMessage.error('识别失败，请重试')
  } finally {
    uploading.value = false
  }
}

function selectRecord(record: OcrPreview) {
  currentPreview.value = record
  fillConfirmForm(record)
}

function fillConfirmForm(preview: OcrPreview) {
  confirmForm.amount = preview.extractedAmount || undefined
  confirmForm.categoryId = preview.suggestedCategoryId || undefined
  confirmForm.transactionDate = preview.extractedDate || new Date()
  confirmForm.merchant = preview.extractedMerchant || ''
  confirmForm.description = ''
}

async function handleConfirm() {
  if (!currentPreview.value) return
  if (!confirmForm.amount || !confirmForm.categoryId || !confirmForm.transactionDate) {
    ElMessage.warning('请填写金额、分类和日期')
    return
  }

  confirming.value = true
  try {
    await confirmOcrRecord(currentPreview.value.id, {
      amount: confirmForm.amount,
      categoryId: confirmForm.categoryId,
      transactionDate: dayjs(confirmForm.transactionDate).format('YYYY-MM-DD'),
      merchant: confirmForm.merchant || undefined,
      description: confirmForm.description || undefined
    })
    ElMessage.success('已成功记账')
    currentPreview.value = null
    await fetchPendingRecords()
  } catch (error) {
    // Handled by interceptor
  } finally {
    confirming.value = false
  }
}

async function handleReject() {
  if (!currentPreview.value) return
  try {
    await rejectOcrRecord(currentPreview.value.id)
    ElMessage.info('已忽略该记录')
    currentPreview.value = null
    await fetchPendingRecords()
  } catch (error) {
    // Handled by interceptor
  }
}
</script>

<style scoped lang="scss">
.ocr-import-page {
  .page-header {
    margin-bottom: 20px;
    h2 { margin: 0; }
  }

  .upload-area {
    width: 100%;
    :deep(.el-upload-dragger) {
      width: 100%;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .pending-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 12px;
    border-radius: 6px;
    cursor: pointer;
    transition: background-color 0.2s;
    margin-bottom: 4px;

    &:hover, &.active {
      background-color: #f0f9ff;
    }

    .pending-info {
      display: flex;
      flex-direction: column;
      gap: 2px;

      .filename {
        font-size: 14px;
        color: #333;
      }

      .amount {
        font-size: 13px;
        font-weight: bold;
        color: #FF7043;
      }
    }

    .date {
      font-size: 12px;
      color: #999;
    }
  }

  .ocr-raw-text {
    white-space: pre-wrap;
    word-break: break-all;
    font-size: 12px;
    color: #666;
    background: #f5f5f5;
    padding: 12px;
    border-radius: 4px;
    max-height: 200px;
    overflow-y: auto;
  }
}
</style>
