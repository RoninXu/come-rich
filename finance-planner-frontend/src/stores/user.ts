import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { login, register, getCurrentUser } from "@/api/auth";
import { getToken, setToken, setUser, getUser, clearAuth } from "@/utils/auth";
import type { User, LoginRequest, RegisterRequest } from "@/types/user";

export const useUserStore = defineStore("user", () => {
  const token = ref<string | null>(getToken());
  const user = ref<User | null>(getUser());

  const isLoggedIn = computed(() => !!token.value);
  const username = computed(() => user.value?.username || "");

  async function loginAction(loginData: LoginRequest) {
    const response = await login(loginData);
    const data = response.data.data;

    token.value = data.token;
    user.value = data.user;

    setToken(data.token);
    setUser(data.user);

    return data;
  }

  async function registerAction(registerData: RegisterRequest) {
    await register(registerData);
  }

  async function fetchCurrentUser() {
    const response = await getCurrentUser();
    user.value = response.data.data;
    setUser(response.data.data);
    return response.data.data;
  }

  function logout() {
    token.value = null;
    user.value = null;
    clearAuth();
  }

  return {
    token,
    user,
    isLoggedIn,
    username,
    loginAction,
    registerAction,
    fetchCurrentUser,
    logout,
  };
});
