<template>
  <el-container class="app-layout">
    <el-aside width="200px" class="app-sidebar">
      <div class="logo">
        <span class="logo-text">Come Rich</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/accounting">
          <el-icon><Wallet /></el-icon>
          <span>记账</span>
        </el-menu-item>
        <el-menu-item index="/budget">
          <el-icon><Coin /></el-icon>
          <span>预算管理</span>
        </el-menu-item>
        <el-menu-item index="/goals">
          <el-icon><Flag /></el-icon>
          <span>理财目标</span>
        </el-menu-item>
        <el-sub-menu index="investment">
          <template #title>
            <el-icon><TrendCharts /></el-icon>
            <span>投资建议</span>
          </template>
          <el-menu-item index="/investment">资产配置</el-menu-item>
          <el-menu-item index="/investment/quiz">风险评估</el-menu-item>
          <el-menu-item index="/investment/history">评估历史</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="career">
          <template #title>
            <el-icon><Opportunity /></el-icon>
            <span>副业规划</span>
          </template>
          <el-menu-item index="/career">AI推荐</el-menu-item>
          <el-menu-item index="/career/plans">我的计划</el-menu-item>
          <el-menu-item index="/career/profile">个人资料</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/ai/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI 顾问</span>
        </el-menu-item>
        <el-sub-menu index="analysis">
          <template #title>
            <el-icon><DataLine /></el-icon>
            <span>统计分析</span>
          </template>
          <el-menu-item index="/analysis/monthly">月度报表</el-menu-item>
          <el-menu-item index="/analysis/health">财务健康</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ userStore.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { HomeFilled, Wallet, DataLine, ChatDotRound, Flag, Opportunity, Coin, TrendCharts } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

function handleCommand(command: string) {
  if (command === 'logout') {
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
  background-color: #304156;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #263445;

    .logo-text {
      color: #fff;
      font-size: 20px;
      font-weight: bold;
    }
  }

  .el-menu {
    border-right: none;
  }
}

.app-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;

      .username {
        margin-left: 8px;
        color: #333;
      }
    }
  }
}

.app-main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
