<template>
  <div class="ocr-import-page">
    <PageHeader
      title="拍照记账"
      show-back
    />

    <n-grid
      :x-gap="16"
      :y-gap="16"
      :cols="2"
    >
      <n-gi>
        <GlassCard>
          <template #header>
            上传小票/账单
          </template>
          <n-upload
            :max="1"
            accept="image/*"
            :default-upload="false"
            :show-file-list="false"
            drag
            @change="handleFileChange"
          >
            <n-upload-dragger>
              <div style="padding: 32px 0; text-align: center">
                <n-icon
                  :size="48"
                  :depth="3"
                >
                  <CloudUpload />
                </n-icon>
                <p style="margin-top: 12px; color: var(--cr-text-secondary)">
                  将小票图片拖到此处，或点击上传
                </p>
                <p
                  style="
                    font-size: 12px;
                    color: var(--cr-text-tertiary);
                    margin-top: 4px;
                  "
                >
                  支持 jpg/png 格式，不超过 10MB
                </p>
              </div>
            </n-upload-dragger>
          </n-upload>

          <n-button
            type="primary"
            :loading="uploading"
            :disabled="!selectedFile"
            block
            style="margin-top: 16px"
            @click="handleUpload"
          >
            开始识别
          </n-button>
        </GlassCard>

        <GlassCard
          v-if="pendingRecords.length > 0"
          style="margin-top: 16px"
        >
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
              <span class="filename">{{
                record.originalFilename || "小票"
              }}</span>
              <span
                v-if="record.extractedAmount"
                class="amount"
              >¥{{ Number(record.extractedAmount).toFixed(2) }}</span>
            </div>
            <span class="date">{{ record.extractedDate || "-" }}</span>
          </div>
        </GlassCard>
      </n-gi>

      <n-gi>
        <GlassCard v-if="currentPreview">
          <n-spin :show="confirming">
            <template #header>
              <div class="card-header">
                <span>识别结果</span>
                <n-space>
                  <n-button
                    type="primary"
                    size="small"
                    @click="handleConfirm"
                  >
                    确认记账
                  </n-button>
                  <n-button
                    size="small"
                    @click="handleReject"
                  >
                    忽略
                  </n-button>
                </n-space>
              </div>
            </template>

            <n-form
              :model="confirmForm"
              label-placement="left"
              label-width="80"
            >
              <n-form-item
                label="金额"
                required
              >
                <n-input-number
                  v-model:value="confirmForm.amount"
                  :min="0.01"
                  :precision="2"
                  :show-button="false"
                  style="width: 100%"
                />
              </n-form-item>

              <n-form-item
                label="分类"
                required
              >
                <n-select
                  v-model:value="confirmForm.categoryId"
                  :options="categorySelectOptions"
                  placeholder="选择分类"
                  style="width: 100%"
                />
              </n-form-item>

              <n-form-item
                label="日期"
                required
              >
                <n-date-picker
                  v-model:value="confirmForm.transactionDate"
                  type="date"
                  style="width: 100%"
                />
              </n-form-item>

              <n-form-item label="商家">
                <n-input
                  v-model:value="confirmForm.merchant"
                  placeholder="商家名称"
                />
              </n-form-item>

              <n-form-item label="描述">
                <n-input
                  v-model:value="confirmForm.description"
                  placeholder="备注说明"
                />
              </n-form-item>
            </n-form>

            <n-collapse>
              <n-collapse-item title="原始识别文本">
                <pre class="ocr-raw-text">{{ currentPreview.ocrRawText }}</pre>
              </n-collapse-item>
            </n-collapse>
          </n-spin>
        </GlassCard>

        <GlassCard v-else>
          <n-empty description="上传小票图片开始识别" />
        </GlassCard>
      </n-gi>
    </n-grid>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from "vue";
import {
  NGrid,
  NGi,
  NUpload,
  NUploadDragger,
  NButton,
  NIcon,
  NSpace,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NSelect,
  NDatePicker,
  NCollapse,
  NCollapseItem,
  NSpin,
  NEmpty,
  useMessage,
  type UploadFileInfo,
} from "naive-ui";
import { CloudUpload } from "@vicons/ionicons5";
import dayjs from "dayjs";
import {
  uploadReceipt,
  confirmOcrRecord,
  rejectOcrRecord,
  getPendingOcrRecords,
} from "@/api/ocr";
import { getExpenseCategories } from "@/api/category";
import type { OcrPreview } from "@/types/ocr";
import type { Category } from "@/types/accounting";
import GlassCard from "@/components/common/GlassCard.vue";
import PageHeader from "@/components/common/PageHeader.vue";

const message = useMessage();

const uploading = ref(false);
const confirming = ref(false);
const selectedFile = ref<File | null>(null);
const currentPreview = ref<OcrPreview | null>(null);
const pendingRecords = ref<OcrPreview[]>([]);
const expenseCategories = ref<Category[]>([]);

const categorySelectOptions = computed(() =>
  expenseCategories.value.map((cat) => ({ label: cat.name, value: cat.id })),
);

const confirmForm = reactive({
  amount: undefined as number | undefined,
  categoryId: undefined as number | undefined,
  transactionDate: Date.now() as number,
  merchant: "",
  description: "",
});

onMounted(async () => {
  await Promise.all([fetchPendingRecords(), fetchCategories()]);
});

async function fetchPendingRecords() {
  try {
    const res = await getPendingOcrRecords();
    if (res.data.code === 200) pendingRecords.value = res.data.data;
  } catch {
    /* silently fail */
  }
}

async function fetchCategories() {
  try {
    const res = await getExpenseCategories();
    if (res.data.code === 200) expenseCategories.value = res.data.data;
  } catch {
    /* silently fail */
  }
}

function handleFileChange(data: { fileList: UploadFileInfo[] }) {
  const file = data.fileList[0];
  selectedFile.value = file?.file || null;
}

async function handleUpload() {
  if (!selectedFile.value) return;

  uploading.value = true;
  try {
    const res = await uploadReceipt(selectedFile.value);
    if (res.data.code === 200) {
      const preview = res.data.data;
      currentPreview.value = preview;
      fillConfirmForm(preview);
      message.success("识别完成");
      selectedFile.value = null;
      await fetchPendingRecords();
    }
  } catch {
    message.error("识别失败，请重试");
  } finally {
    uploading.value = false;
  }
}

function selectRecord(record: OcrPreview) {
  currentPreview.value = record;
  fillConfirmForm(record);
}

function fillConfirmForm(preview: OcrPreview) {
  confirmForm.amount = preview.extractedAmount || undefined;
  confirmForm.categoryId = preview.suggestedCategoryId || undefined;
  confirmForm.transactionDate = preview.extractedDate
    ? new Date(preview.extractedDate).getTime()
    : Date.now();
  confirmForm.merchant = preview.extractedMerchant || "";
  confirmForm.description = "";
}

async function handleConfirm() {
  if (!currentPreview.value) return;
  if (
    !confirmForm.amount ||
    !confirmForm.categoryId ||
    !confirmForm.transactionDate
  ) {
    message.warning("请填写金额、分类和日期");
    return;
  }

  confirming.value = true;
  try {
    await confirmOcrRecord(currentPreview.value.id, {
      amount: confirmForm.amount,
      categoryId: confirmForm.categoryId,
      transactionDate: dayjs(confirmForm.transactionDate).format("YYYY-MM-DD"),
      merchant: confirmForm.merchant || undefined,
      description: confirmForm.description || undefined,
    });
    message.success("已成功记账");
    currentPreview.value = null;
    await fetchPendingRecords();
  } catch {
    // Handled by interceptor
  } finally {
    confirming.value = false;
  }
}

async function handleReject() {
  if (!currentPreview.value) return;
  try {
    await rejectOcrRecord(currentPreview.value.id);
    message.info("已忽略该记录");
    currentPreview.value = null;
    await fetchPendingRecords();
  } catch {
    // Handled by interceptor
  }
}
</script>

<style scoped lang="scss">
.ocr-import-page {
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
    border-radius: var(--cr-radius-md);
    cursor: pointer;
    transition: background-color 0.2s;
    margin-bottom: 4px;

    &:hover,
    &.active {
      background: var(--cr-bg-hover);
    }

    .pending-info {
      display: flex;
      flex-direction: column;
      gap: 2px;

      .filename {
        font-size: 14px;
        color: var(--cr-text-primary);
      }

      .amount {
        font-size: 13px;
        font-weight: 600;
        color: var(--cr-error);
      }
    }

    .date {
      font-size: 12px;
      color: var(--cr-text-secondary);
    }
  }

  .ocr-raw-text {
    white-space: pre-wrap;
    word-break: break-all;
    font-size: 12px;
    color: var(--cr-text-secondary);
    background: var(--cr-bg-input);
    padding: 12px;
    border-radius: var(--cr-radius-md);
    max-height: 200px;
    overflow-y: auto;
  }
}
</style>
