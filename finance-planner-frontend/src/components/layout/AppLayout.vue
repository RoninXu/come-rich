<template>
  <n-layout class="app-layout">
    <n-layout-header bordered class="app-header">
      <div class="brand" @click="router.push('/dashboard')">
        <div class="brand__dot" />
        <div>
          <p class="brand__name">Come Rich</p>
          <p class="brand__sub">Personal Finance OS</p>
        </div>
      </div>

      <n-menu
        mode="horizontal"
        responsive
        :value="activePrimary"
        :options="primaryMenu"
        class="top-nav"
        @update:value="handlePrimarySelect"
      />

      <div class="app-header__right">
        <n-button quaternary circle size="small" @click="toggleTheme">
          <template #icon>
            <n-icon :size="18">
              <Moon v-if="!isDark" />
              <Sunny v-else />
            </n-icon>
          </template>
        </n-button>

        <n-dropdown :options="userMenuOptions" @select="handleUserMenuSelect">
          <div class="user-chip">
            <n-avatar :size="30" round>
              <n-icon><Person /></n-icon>
            </n-avatar>
            <span>{{ userStore.username }}</span>
          </div>
        </n-dropdown>
      </div>
    </n-layout-header>

    <n-layout-content class="app-main">
      <div v-if="secondaryMenu.length" class="secondary-nav-wrap">
        <n-menu
          mode="horizontal"
          :value="route.path"
          :options="secondaryMenu"
          class="secondary-nav"
          @update:value="handleSecondarySelect"
        />
      </div>

      <router-view v-slot="{ Component }">
        <transition name="cr-page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </n-layout-content>
  </n-layout>
</template>

<script setup lang="ts">
import { computed, h } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NMenu,
  NDropdown,
  NAvatar,
  NButton,
  NIcon,
} from "naive-ui";
import type { MenuOption } from "naive-ui";
import {
  PieChart,
  Wallet,
  Cash,
  Chatbubbles,
  Flag,
  TrendingUp,
  Rocket,
  StatsChart,
  Moon,
  Sunny,
  Person,
  LogOut,
} from "@vicons/ionicons5";
import { useUserStore } from "@/stores/user";
import { useTheme } from "@/composables/useTheme";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const { isDark, toggleTheme } = useTheme();

function renderIcon(icon: any) {
  return () => h(NIcon, null, { default: () => h(icon) });
}

const primaryMenu: MenuOption[] = [
  { label: "Overview", key: "overview", icon: renderIcon(PieChart) },
  { label: "Transactions", key: "transactions", icon: renderIcon(Wallet) },
  { label: "Budgets", key: "budgets", icon: renderIcon(Cash) },
  { label: "Advisor", key: "advisor", icon: renderIcon(Chatbubbles) },
];

const secondaryByPrimary: Record<string, MenuOption[]> = {
  overview: [
    { label: "首页", key: "/dashboard" },
    { label: "财务健康", key: "/analysis/health" },
    { label: "月度报告", key: "/analysis/monthly" },
    { label: "目标", key: "/goals" },
    { label: "投资", key: "/investment" },
    { label: "职业", key: "/career" },
  ],
  transactions: [
    { label: "交易记录", key: "/accounting" },
    { label: "拍照记账", key: "/accounting/ocr" },
  ],
  budgets: [
    { label: "预算总览", key: "/budget" },
    { label: "预算编辑", key: "/budget/edit" },
    { label: "预算趋势", key: "/budget/trend" },
  ],
  advisor: [
    { label: "AI 顾问", key: "/ai/chat" },
    { label: "Agent 分析", key: "/ai/agent-metrics" },
  ],
};

const activePrimary = computed(() => {
  if (route.path.startsWith("/accounting")) return "transactions";
  if (route.path.startsWith("/budget")) return "budgets";
  if (route.path.startsWith("/ai")) return "advisor";
  return "overview";
});

const secondaryMenu = computed(() => secondaryByPrimary[activePrimary.value]);

function handlePrimarySelect(key: string) {
  const first = secondaryByPrimary[key]?.[0] as MenuOption | undefined;
  if (first?.key) {
    router.push(String(first.key));
  }
}

function handleSecondarySelect(key: string) {
  router.push(key);
}

const userMenuOptions = [
  {
    label: "退出登录",
    key: "logout",
    icon: renderIcon(LogOut),
  },
];

function handleUserMenuSelect(key: string) {
  if (key === "logout") {
    userStore.logout();
    router.push("/login");
  }
}
</script>

<style scoped lang="scss">
.app-layout {
  min-height: 100vh;
  background: var(--cr-bg-page);
}

.app-header {
  height: 68px;
  display: grid;
  grid-template-columns: 260px 1fr auto;
  align-items: center;
  gap: var(--cr-space-lg);
  padding: 0 var(--cr-space-xxxl);
  background: var(--cr-bg-header) !important;
  border-bottom: 1px solid var(--cr-border);
  backdrop-filter: blur(var(--cr-blur-md));
  position: sticky;
  top: 0;
  z-index: 50;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;

  &__dot {
    width: 14px;
    height: 14px;
    border-radius: 999px;
    background: radial-gradient(circle at 30% 30%, #7cb0ff, var(--cr-primary));
    box-shadow: 0 0 0 6px color-mix(in srgb, var(--cr-primary) 18%, transparent);
  }

  &__name {
    font-size: 16px;
    font-weight: 700;
    color: var(--cr-text-primary);
  }

  &__sub {
    font-size: 12px;
    color: var(--cr-text-tertiary);
  }
}

.top-nav {
  min-width: 360px;
}

.app-header__right {
  display: flex;
  align-items: center;
  gap: var(--cr-space-md);
}

.user-chip {
  display: flex;
  align-items: center;
  gap: var(--cr-space-sm);
  padding: 4px 8px;
  border-radius: var(--cr-radius-md);
  cursor: pointer;
  color: var(--cr-text-secondary);

  &:hover {
    background: var(--cr-bg-hover);
  }
}

.app-main {
  padding: var(--cr-space-xl) var(--cr-space-xxxl) var(--cr-space-xxxl);
}

.secondary-nav-wrap {
  margin-bottom: var(--cr-space-xl);
  background: var(--cr-bg-elevated);
  border: 1px solid var(--cr-border-light);
  border-radius: var(--cr-radius-lg);
  box-shadow: var(--cr-shadow-sm);
  padding: 0 var(--cr-space-sm);
}

@media (max-width: 1100px) {
  .app-header {
    grid-template-columns: 1fr auto;
    padding: 0 var(--cr-space-xl);
  }

  .top-nav {
    display: none;
  }

  .app-main {
    padding: var(--cr-space-lg);
  }
}
</style>
