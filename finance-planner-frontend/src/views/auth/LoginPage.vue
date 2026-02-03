<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>Come Rich</h1>
        <p>AI个人理财规划师</p>
      </div>

      <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
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

        <n-form-item>
          <n-button
            type="primary"
            size="large"
            :loading="loading"
            block
            @click="handleLogin"
          >
            登录
          </n-button>
        </n-form-item>

        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register">
            立即注册
          </router-link>
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

const form = reactive({
  username: "",
  password: "",
});

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
  } catch (e: any) {
    const msg =
      e?.response?.data?.message ||
      e?.message ||
      "登录失败，请检查用户名和密码";
    message.error(msg, { duration: 0 });
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

[data-theme="dark"] .login-container {
  background: linear-gradient(135deg, #2d3561 0%, #4a2a6b 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: var(--cr-bg-card);
  backdrop-filter: blur(var(--cr-blur-md));
  -webkit-backdrop-filter: blur(var(--cr-blur-md));
  border: 1px solid var(--cr-border-light);
  border-radius: var(--cr-radius-xxl);
  box-shadow: var(--cr-shadow-xl);
}

.login-header {
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

.login-footer {
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
