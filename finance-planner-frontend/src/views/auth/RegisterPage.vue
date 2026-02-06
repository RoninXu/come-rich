<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-brand">
        <h1>创建账户</h1>
        <p>开始构建你的长期理财计划</p>
      </div>

      <n-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="auth-form"
        @keyup.enter="handleRegister"
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
          <n-input v-model:value="form.email" placeholder="邮箱（选填）" size="large">
            <template #prefix>
              <n-icon :component="Mail" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item>
          <n-button type="primary" size="large" :loading="loading" block @click="handleRegister">
            注册
          </n-button>
        </n-form-item>

        <div class="auth-footer">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </n-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
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
import { Person, LockClosed, Mail } from "@vicons/ionicons5";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const userStore = useUserStore();
const message = useMessage();

const formRef = ref<FormInst | null>(null);
const loading = ref(false);

const form = reactive({
  username: "",
  password: "",
  confirmPassword: "",
  email: "",
});

const rules: FormRules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 50, message: "用户名长度需在 3 到 50 个字符", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, message: "密码至少 6 位", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请再次输入密码", trigger: "blur" },
    {
      validator: (_rule: any, value: string) =>
        value === form.password || new Error("两次输入的密码不一致"),
      trigger: "blur",
    },
  ],
  email: [{ type: "email", message: "请输入正确的邮箱格式", trigger: "blur" }],
};

async function handleRegister() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }

  loading.value = true;
  try {
    await userStore.registerAction({
      username: form.username,
      password: form.password,
      email: form.email || undefined,
    });
    message.success("注册成功，请登录");
    router.push("/login");
  } catch {
    message.error("注册失败，请稍后重试");
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
    font-size: 28px;
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
