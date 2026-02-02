<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1>Come Rich</h1>
        <p>创建您的账户</p>
      </div>

      <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="register-form"
        @keyup.enter="handleRegister"
      >
        <n-form-item path="username">
          <n-input
            v-model:value="form.username"
            placeholder="用户名"
            size="large"
          >
            <template #prefix>
              <n-icon :component="Person" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="password">
          <n-input
            v-model:value="form.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password-on="click"
          >
            <template #prefix>
              <n-icon :component="LockClosed" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="confirmPassword">
          <n-input
            v-model:value="form.confirmPassword"
            type="password"
            placeholder="确认密码"
            size="large"
            show-password-on="click"
          >
            <template #prefix>
              <n-icon :component="LockClosed" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="email">
          <n-input
            v-model:value="form.email"
            placeholder="邮箱 (选填)"
            size="large"
          >
            <template #prefix>
              <n-icon :component="Mail" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item>
          <n-button
            type="primary"
            size="large"
            :loading="loading"
            block
            @click="handleRegister"
          >
            注册
          </n-button>
        </n-form-item>

        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </n-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import {
  NForm,
  NFormItem,
  NInput,
  NButton,
  NIcon,
  useMessage,
  type FormInst,
  type FormRules,
} from 'naive-ui'
import { Person, LockClosed, Mail } from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const message = useMessage()

const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string) => {
        return value === form.password || new Error('两次输入的密码不一致')
      },
      trigger: 'blur'
    }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

async function handleRegister() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await userStore.registerAction({
      username: form.username,
      password: form.password,
      email: form.email || undefined
    })
    message.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // Error is handled by request interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

[data-theme='dark'] .register-container {
  background: linear-gradient(135deg, #2d3561 0%, #4a2a6b 100%);
}

.register-box {
  width: 400px;
  padding: 40px;
  background: var(--cr-bg-card);
  backdrop-filter: blur(var(--cr-blur-md));
  -webkit-backdrop-filter: blur(var(--cr-blur-md));
  border: 1px solid var(--cr-border-light);
  border-radius: var(--cr-radius-xxl);
  box-shadow: var(--cr-shadow-xl);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;

  h1 {
    font-size: 28px;
    color: var(--cr-text-primary);
    font-weight: 700;
    letter-spacing: -0.02em;
    margin-bottom: 8px;
  }

  p {
    color: var(--cr-text-secondary);
    font-size: 14px;
  }
}

.register-footer {
  text-align: center;
  color: var(--cr-text-secondary);
  font-size: 14px;

  a {
    color: var(--cr-primary);
    margin-left: 4px;
    font-weight: 500;
  }
}
</style>
