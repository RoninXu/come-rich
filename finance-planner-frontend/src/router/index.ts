import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import { isAuthenticated } from "@/utils/auth";

const routes: RouteRecordRaw[] = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/auth/LoginPage.vue"),
    meta: { requiresAuth: false },
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("@/views/auth/RegisterPage.vue"),
    meta: { requiresAuth: false },
  },
  {
    path: "/",
    component: () => import("@/components/layout/AppLayout.vue"),
    redirect: "/dashboard",
    children: [
      {
        path: "dashboard",
        name: "Dashboard",
        component: () => import("@/views/dashboard/DashboardPage.vue"),
        meta: { title: "棣栭〉", icon: "HomeFilled" },
      },
      {
        path: "accounting",
        name: "AccountingList",
        component: () => import("@/views/accounting/TransactionListPage.vue"),
        meta: { title: "璁拌处", icon: "Wallet" },
      },
      {
        path: "accounting/new",
        name: "AccountingNew",
        component: () => import("@/views/accounting/TransactionFormPage.vue"),
        meta: { title: "New Transaction", hidden: true },
      },
      {
        path: "accounting/edit/:id",
        name: "AccountingEdit",
        component: () => import("@/views/accounting/TransactionFormPage.vue"),
        meta: { title: "缂栬緫", hidden: true },
      },
      {
        path: "accounting/ocr",
        name: "OcrImport",
        component: () => import("@/views/accounting/OcrImportPage.vue"),
        meta: { title: "鎷嶇収璁拌处", hidden: true },
      },
      {
        path: "budget",
        name: "BudgetOverview",
        component: () => import("@/views/budget/BudgetOverviewPage.vue"),
        meta: { title: "棰勭畻绠＄悊", icon: "Coin" },
      },
      {
        path: "budget/edit",
        name: "BudgetForm",
        component: () => import("@/views/budget/BudgetFormPage.vue"),
        meta: { title: "璁剧疆棰勭畻", hidden: true },
      },
      {
        path: "budget/trend",
        name: "BudgetTrend",
        component: () => import("@/views/budget/BudgetTrendPage.vue"),
        meta: { title: "棰勭畻瓒嬪娍", hidden: true },
      },
      {
        path: "goals",
        name: "GoalList",
        component: () => import("@/views/goal/GoalListPage.vue"),
        meta: { title: "鐞嗚储鐩爣", icon: "Flag" },
      },
      {
        path: "goals/new",
        name: "GoalNew",
        component: () => import("@/views/goal/GoalFormPage.vue"),
        meta: { title: "鏂板缓鐩爣", hidden: true },
      },
      {
        path: "goals/:id",
        name: "GoalDetail",
        component: () => import("@/views/goal/GoalDetailPage.vue"),
        meta: { title: "鐩爣璇︽儏", hidden: true },
      },
      {
        path: "goals/edit/:id",
        name: "GoalEdit",
        component: () => import("@/views/goal/GoalFormPage.vue"),
        meta: { title: "缂栬緫鐩爣", hidden: true },
      },
      {
        path: "investment",
        name: "InvestmentAdvice",
        component: () => import("@/views/investment/InvestmentAdvicePage.vue"),
        meta: { title: "鎶曡祫寤鸿", icon: "TrendCharts" },
      },
      {
        path: "investment/quiz",
        name: "RiskQuiz",
        component: () => import("@/views/investment/RiskQuizPage.vue"),
        meta: { title: "椋庨櫓璇勪及", hidden: true },
      },
      {
        path: "investment/history",
        name: "AssessmentHistory",
        component: () => import("@/views/investment/AssessmentHistoryPage.vue"),
        meta: { title: "璇勪及鍘嗗彶", hidden: true },
      },
      {
        path: "career",
        name: "CareerRecommendations",
        component: () => import("@/views/career/RecommendationPage.vue"),
        meta: { title: "AI鎺ㄨ崘", icon: "Opportunity" },
      },
      {
        path: "career/plans",
        name: "CareerPlanList",
        component: () => import("@/views/career/CareerPlanListPage.vue"),
        meta: { title: "鎴戠殑璁″垝", hidden: true },
      },
      {
        path: "career/plans/:id",
        name: "CareerPlanDetail",
        component: () => import("@/views/career/CareerPlanDetailPage.vue"),
        meta: { title: "璁″垝璇︽儏", hidden: true },
      },
      {
        path: "career/profile",
        name: "CareerProfile",
        component: () => import("@/views/career/ProfilePage.vue"),
        meta: { title: "涓汉璧勬枡", hidden: true },
      },
      {
        path: "ai/chat",
        name: "AiChat",
        component: () => import("@/views/ai/ChatPage.vue"),
        meta: { title: "AI 椤鹃棶", icon: "ChatDotRound" },
      },
      {
        path: "ai/agent-metrics",
        name: "AgentMetrics",
        component: () => import("@/views/ai/AgentMetricsPage.vue"),
        meta: { title: "Agent 分析", hidden: true },
      },
      {
        path: "analysis/monthly",
        name: "MonthlyReport",
        component: () => import("@/views/analysis/MonthlyReportPage.vue"),
        meta: { title: "鏈堝害鎶ヨ〃", icon: "DataLine" },
      },
      {
        path: "analysis/health",
        name: "HealthScore",
        component: () => import("@/views/analysis/HealthScorePage.vue"),
        meta: { title: "璐㈠姟鍋ュ悍", icon: "TrendCharts" },
      },
    ],
  },
  {
    path: "/:pathMatch(.*)*",
    redirect: "/dashboard",
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// Navigation guard
router.beforeEach((to, _from, next) => {
  const requiresAuth = to.meta.requiresAuth !== false;

  if (requiresAuth && !isAuthenticated()) {
    next({ path: "/login", query: { redirect: to.fullPath } });
  } else if (
    !requiresAuth &&
    isAuthenticated() &&
    (to.path === "/login" || to.path === "/register")
  ) {
    next({ path: "/dashboard" });
  } else {
    next();
  }
});

export default router;
