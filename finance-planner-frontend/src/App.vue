<template>
  <n-config-provider
    :theme="naiveTheme"
    :theme-overrides="naiveThemeOverrides"
  >
    <n-message-provider>
      <n-dialog-provider>
        <n-notification-provider>
          <n-loading-bar-provider>
            <router-view />
          </n-loading-bar-provider>
        </n-notification-provider>
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import {
  NConfigProvider,
  NMessageProvider,
  NDialogProvider,
  NNotificationProvider,
  NLoadingBarProvider,
} from 'naive-ui'
import { useTheme } from '@/composables/useTheme'

const { naiveTheme, naiveThemeOverrides } = useTheme()
</script>

<style>
#app {
  width: 100%;
  min-height: 100vh;
}
</style>
