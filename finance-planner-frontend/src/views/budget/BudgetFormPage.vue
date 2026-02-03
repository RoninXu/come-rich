<template>
  <div class="budget-form-page">
    <n-spin :show="loading">
      <PageHeader
        title="设置预算"
        show-back
      >
        <template #actions>
          <n-date-picker
            v-model:formatted-value="selectedMonth"
            type="month"
            value-format="yyyy-MM"
            style="width: 160px"
            @update:formatted-value="fetchCategories"
          />
        </template>
      </PageHeader>

      <GlassCard>
        <n-form
          label-placement="left"
          label-width="120"
        >
          <n-form-item label="月度总预算">
            <n-space align="center">
              <n-input-number
                v-model:value="totalBudget"
                :min="0"
                :precision="2"
                :step="100"
                placeholder="设置月度总预算上限"
                style="width: 300px"
              />
              <n-button
                type="primary"
                @click="saveTotalBudget"
              >
                保存总预算
              </n-button>
            </n-space>
          </n-form-item>
        </n-form>
      </GlassCard>

      <GlassCard style="margin-top: 16px">
        <template #header>
          分类预算
        </template>
        <n-empty
          v-if="categories.length === 0"
          description="暂无支出分类"
        />
        <n-form
          v-else
          label-placement="left"
          label-width="140"
        >
          <n-form-item
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
          >
            <n-space align="center">
              <n-input-number
                v-model:value="budgetAmounts[cat.id]"
                :min="0"
                :precision="2"
                :step="100"
                placeholder="输入预算金额"
                style="width: 250px"
              />
              <span style="color: var(--cr-text-tertiary)">元/月</span>
            </n-space>
          </n-form-item>
          <n-form-item>
            <n-space>
              <n-button
                type="primary"
                :loading="saving"
                @click="saveAllBudgets"
              >
                保存所有预算
              </n-button>
              <n-button @click="router.back()">
                取消
              </n-button>
            </n-space>
          </n-form-item>
        </n-form>
      </GlassCard>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import {
  NSpin,
  NForm,
  NFormItem,
  NInputNumber,
  NButton,
  NSpace,
  NDatePicker,
  NEmpty,
  useMessage,
} from "naive-ui";
import dayjs from "dayjs";
import {
  setBudget,
  getBudgets,
  setBudgetTotal,
  getBudgetTotal,
} from "@/api/budget";
import { getCategories } from "@/api/category";
import GlassCard from "@/components/common/GlassCard.vue";
import PageHeader from "@/components/common/PageHeader.vue";

const router = useRouter();
const route = useRoute();
const message = useMessage();

const selectedMonth = ref(
  (route.query.month as string) || dayjs().format("YYYY-MM"),
);
const loading = ref(false);
const saving = ref(false);
const totalBudget = ref(0);

interface CategoryItem {
  id: number;
  name: string;
}
const categories = ref<CategoryItem[]>([]);
const budgetAmounts = reactive<Record<number, number>>({});

onMounted(() => {
  fetchCategories();
});

async function fetchCategories() {
  loading.value = true;
  try {
    const catRes = await getCategories(2);
    if (catRes.data.code === 200) {
      categories.value = catRes.data.data
        .filter((c: any) => c.parentId === null || c.parentId === undefined)
        .map((c: any) => ({ id: c.id, name: c.name }));
    }
    const budgetRes = await getBudgets(selectedMonth.value);
    if (budgetRes.data.code === 200 && budgetRes.data.data) {
      for (const b of budgetRes.data.data)
        budgetAmounts[b.categoryId] = b.amount;
    }
    try {
      const totalRes = await getBudgetTotal(selectedMonth.value);
      if (totalRes.data.code === 200 && totalRes.data.data)
        totalBudget.value = totalRes.data.data.totalAmount;
    } catch {
      /* No total set yet */
    }
  } catch {
    message.error("加载分类失败");
  } finally {
    loading.value = false;
  }
}

async function saveTotalBudget() {
  if (totalBudget.value <= 0) {
    message.warning("请输入有效的总预算金额");
    return;
  }
  try {
    await setBudgetTotal({
      yearMonth: selectedMonth.value,
      totalAmount: totalBudget.value,
    });
    message.success("总预算保存成功");
  } catch {
    message.error("保存失败");
  }
}

async function saveAllBudgets() {
  saving.value = true;
  try {
    const promises = [];
    for (const cat of categories.value) {
      const amount = budgetAmounts[cat.id];
      if (amount && amount > 0) {
        promises.push(
          setBudget({
            categoryId: cat.id,
            yearMonth: selectedMonth.value,
            amount,
          }),
        );
      }
    }
    await Promise.all(promises);
    message.success("预算保存成功");
    router.push("/budget");
  } catch {
    message.error("保存失败");
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped lang="scss">
.budget-form-page {
  /* no special styles needed */
}
</style>
