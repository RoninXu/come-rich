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
        meta: { title: "首页", icon: "HomeFilled" },
      },
      {
        path: "accounting",
        name: "AccountingList",
        component: () => import("@/views/accounting/TransactionListPage.vue"),
        meta: { title: "记账", icon: "Wallet" },
      },
      {
        path: "accounting/new",
        name: "AccountingNew",
        component: () => import("@/views/accounting/TransactionFormPage.vue"),
        meta: { title: "记一笔", hidden: true },
      },
      {
        path: "accounting/edit/:id",
        name: "AccountingEdit",
        component: () => import("@/views/accounting/TransactionFormPage.vue"),
        meta: { title: "编辑", hidden: true },
      },
      {
        path: "accounting/ocr",
        name: "OcrImport",
        component: () => import("@/views/accounting/OcrImportPage.vue"),
        meta: { title: "拍照记账", hidden: true },
      },
      {
        path: "budget",
        name: "BudgetOverview",
        component: () => import("@/views/budget/BudgetOverviewPage.vue"),
        meta: { title: "预算管理", icon: "Coin" },
      },
      {
        path: "budget/edit",
        name: "BudgetForm",
        component: () => import("@/views/budget/BudgetFormPage.vue"),
        meta: { title: "设置预算", hidden: true },
      },
      {
        path: "budget/trend",
        name: "BudgetTrend",
        component: () => import("@/views/budget/BudgetTrendPage.vue"),
        meta: { title: "预算趋势", hidden: true },
      },
      {
        path: "goals",
        name: "GoalList",
        component: () => import("@/views/goal/GoalListPage.vue"),
        meta: { title: "理财目标", icon: "Flag" },
      },
      {
        path: "goals/new",
        name: "GoalNew",
        component: () => import("@/views/goal/GoalFormPage.vue"),
        meta: { title: "新建目标", hidden: true },
      },
      {
        path: "goals/:id",
        name: "GoalDetail",
        component: () => import("@/views/goal/GoalDetailPage.vue"),
        meta: { title: "目标详情", hidden: true },
      },
      {
        path: "goals/edit/:id",
        name: "GoalEdit",
        component: () => import("@/views/goal/GoalFormPage.vue"),
        meta: { title: "编辑目标", hidden: true },
      },
      {
        path: "investment",
        name: "InvestmentAdvice",
        component: () => import("@/views/investment/InvestmentAdvicePage.vue"),
        meta: { title: "投资建议", icon: "TrendCharts" },
      },
      {
        path: "investment/quiz",
        name: "RiskQuiz",
        component: () => import("@/views/investment/RiskQuizPage.vue"),
        meta: { title: "风险评估", hidden: true },
      },
      {
        path: "investment/history",
        name: "AssessmentHistory",
        component: () => import("@/views/investment/AssessmentHistoryPage.vue"),
        meta: { title: "评估历史", hidden: true },
      },
      {
        path: "career",
        name: "CareerRecommendations",
        component: () => import("@/views/career/RecommendationPage.vue"),
        meta: { title: "AI推荐", icon: "Opportunity" },
      },
      {
        path: "career/plans",
        name: "CareerPlanList",
        component: () => import("@/views/career/CareerPlanListPage.vue"),
        meta: { title: "我的计划", hidden: true },
      },
      {
        path: "career/plans/:id",
        name: "CareerPlanDetail",
        component: () => import("@/views/career/CareerPlanDetailPage.vue"),
        meta: { title: "计划详情", hidden: true },
      },
      {
        path: "career/profile",
        name: "CareerProfile",
        component: () => import("@/views/career/ProfilePage.vue"),
        meta: { title: "个人资料", hidden: true },
      },
      {
        path: "ai/chat",
        name: "AiChat",
        component: () => import("@/views/ai/ChatPage.vue"),
        meta: { title: "AI 顾问", icon: "ChatDotRound" },
      },
      {
        path: "analysis/monthly",
        name: "MonthlyReport",
        component: () => import("@/views/analysis/MonthlyReportPage.vue"),
        meta: { title: "月度报表", icon: "DataLine" },
      },
      {
        path: "analysis/health",
        name: "HealthScore",
        component: () => import("@/views/analysis/HealthScorePage.vue"),
        meta: { title: "财务健康", icon: "TrendCharts" },
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
