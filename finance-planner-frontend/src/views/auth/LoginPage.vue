<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <h1>Come Rich</h1>
        <p>你的 AI 个人理财规划师</p>
      </div>

      <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="auth-form"
        @keyup.enter="handleLogin"
      >
        <n-form-item path="username">
          <n-input v-model:value="form.username" placeholder="用户名" size="large">
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

        <n-form-item>
          <n-button type="primary" size="large" :loading="loading" block @click="handleLogin">
            登录
          </n-button>
        </n-form-item>

        <div class="auth-footer">
          <span>还没有账号？</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </n-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter, useRoute } from "vue-router";
import {
  NForm,
  NFormItem,
  NInput,
  NButton,
  NIcon,
  useMessage,
  type FormInst,
  type FormRules,
} from "naive-ui";
import { Person, LockClosed } from "@vicons/ionicons5";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const message = useMessage();

const formRef = ref<FormInst | null>(null);
const loading = ref(false);
const form = reactive({ username: "", password: "" });

const rules: FormRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

async function handleLogin() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }

  loading.value = true;
  try {
    await userStore.loginAction(form);
    message.success("登录成功");
    const redirect = route.query.redirect as string;
    router.push(redirect || "/dashboard");
  } catch (error: any) {
    const msg =
      error?.response?.data?.message ||
      error?.message ||
      "登录失败，请检查用户名和密码";
    message.error(msg, { duration: 0 });
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped lang="scss">
.auth-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 20px;
  background:
    radial-gradient(circle at 10% 10%, rgba(20, 99, 255, 0.35), transparent 35%),
    radial-gradient(circle at 90% 90%, rgba(2, 132, 199, 0.24), transparent 36%),
    var(--cr-bg-page);
}

.auth-card {
  width: min(440px, 100%);
  background: var(--cr-bg-card);
  border: 1px solid var(--cr-border-light);
  border-radius: var(--cr-radius-xl);
  box-shadow: var(--cr-shadow-lg);
  padding: 32px;
}

.auth-brand {
  text-align: center;
  margin-bottom: 24px;

  h1 {
    font-size: 30px;
    line-height: 1;
    margin-bottom: 8px;
    color: var(--cr-text-primary);
  }

  p {
    color: var(--cr-text-secondary);
    font-size: 14px;
  }
}

.auth-footer {
  text-align: center;
  color: var(--cr-text-secondary);

  a {
    margin-left: 6px;
    color: var(--cr-primary);
    font-weight: 600;
  }
}
</style>
