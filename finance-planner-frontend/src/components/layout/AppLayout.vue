<template>
  <n-layout class="app-layout" has-sider>
    <n-layout-sider
      bordered
      :width="220"
      :collapsed-width="0"
      collapse-mode="width"
      class="app-sidebar"
      content-class="app-sidebar__content"
    >
      <div class="app-sidebar__logo">
        <span class="app-sidebar__logo-text">Come Rich</span>
        <span class="app-sidebar__logo-sub">AI</span>
      </div>
      <n-menu
        :value="activeMenu"
        :options="menuOptions"
        :root-indent="20"
        :indent="16"
        @update:value="handleMenuSelect"
      />
    </n-layout-sider>

    <n-layout>
      <n-layout-header bordered class="app-header">
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
            <div class="app-header__user">
              <n-avatar :size="32" round>
                <n-icon><Person /></n-icon>
              </n-avatar>
              <span class="app-header__username">{{ userStore.username }}</span>
            </div>
          </n-dropdown>
        </div>
      </n-layout-header>

      <n-layout-content class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="cr-page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NLayout,
  NLayoutSider,
  NLayoutHeader,
  NLayoutContent,
  NMenu,
  NDropdown,
  NAvatar,
  NButton,
  NIcon,
} from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import {
  Home,
  Wallet,
  Cash,
  Flag,
  TrendingUp,
  Rocket,
  Chatbubbles,
  StatsChart,
  Moon,
  Sunny,
  Person,
  LogOut,
} from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'
import { useTheme } from '@/composables/useTheme'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { isDark, toggleTheme } = useTheme()

const activeMenu = computed(() => route.path)

function renderIcon(icon: any) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions: MenuOption[] = [
  {
    label: '首页',
    key: '/dashboard',
    icon: renderIcon(Home),
  },
  {
    label: '记账',
    key: '/accounting',
    icon: renderIcon(Wallet),
  },
  {
    label: '预算管理',
    key: '/budget',
    icon: renderIcon(Cash),
  },
  {
    label: '理财目标',
    key: '/goals',
    icon: renderIcon(Flag),
  },
  {
    label: '投资建议',
    key: 'investment',
    icon: renderIcon(TrendingUp),
    children: [
      { label: '资产配置', key: '/investment' },
      { label: '风险评估', key: '/investment/quiz' },
      { label: '评估历史', key: '/investment/history' },
    ],
  },
  {
    label: '副业规划',
    key: 'career',
    icon: renderIcon(Rocket),
    children: [
      { label: 'AI推荐', key: '/career' },
      { label: '我的计划', key: '/career/plans' },
      { label: '个人资料', key: '/career/profile' },
    ],
  },
  {
    label: 'AI 顾问',
    key: '/ai/chat',
    icon: renderIcon(Chatbubbles),
  },
  {
    label: '统计分析',
    key: 'analysis',
    icon: renderIcon(StatsChart),
    children: [
      { label: '月度报表', key: '/analysis/monthly' },
      { label: '财务健康', key: '/analysis/health' },
    ],
  },
]

function handleMenuSelect(key: string) {
  router.push(key)
}

const userMenuOptions = [
  {
    label: '退出登录',
    key: 'logout',
    icon: renderIcon(LogOut),
  },
]

function handleUserMenuSelect(key: string) {
  if (key === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.app-layout {
  height: 100vh;
}

.app-sidebar {
  background: var(--cr-bg-sidebar) !important;
  backdrop-filter: blur(var(--cr-blur-lg));
  -webkit-backdrop-filter: blur(var(--cr-blur-lg));

  &__content {
    display: flex;
    flex-direction: column;
  }

  &__logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    border-bottom: 1px solid var(--cr-border-light);
  }

  &__logo-text {
    color: var(--cr-text-primary);
    font-size: 20px;
    font-weight: 700;
    letter-spacing: -0.02em;
  }

  &__logo-sub {
    font-size: 11px;
    font-weight: 600;
    color: var(--cr-primary);
    background: rgba(0, 122, 255, 0.1);
    padding: 2px 6px;
    border-radius: 4px;
    line-height: 1;
  }
}

.app-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
  background: var(--cr-bg-header) !important;
  backdrop-filter: blur(var(--cr-blur-md));
  -webkit-backdrop-filter: blur(var(--cr-blur-md));

  &__right {
    display: flex;
    align-items: center;
    gap: var(--cr-space-md);
  }

  &__user {
    display: flex;
    align-items: center;
    gap: var(--cr-space-sm);
    cursor: pointer;
    padding: 4px 8px;
    border-radius: var(--cr-radius-md);
    transition: background 0.15s ease;

    &:hover {
      background: var(--cr-bg-hover);
    }
  }

  &__username {
    color: var(--cr-text-primary);
    font-size: 14px;
    font-weight: 500;
  }
}

.app-main {
  padding: var(--cr-space-xxl);
  background: var(--cr-bg-page) !important;
  overflow-y: auto;
}
</style>
