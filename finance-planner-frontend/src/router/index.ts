import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { isAuthenticated } from '@/utils/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginPage.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterPage.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/layout/AppLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardPage.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'accounting',
        name: 'AccountingList',
        component: () => import('@/views/accounting/TransactionListPage.vue'),
        meta: { title: '记账', icon: 'Wallet' }
      },
      {
        path: 'accounting/new',
        name: 'AccountingNew',
        component: () => import('@/views/accounting/TransactionFormPage.vue'),
        meta: { title: '记一笔', hidden: true }
      },
      {
        path: 'accounting/edit/:id',
        name: 'AccountingEdit',
        component: () => import('@/views/accounting/TransactionFormPage.vue'),
        meta: { title: '编辑', hidden: true }
      },
      {
        path: 'analysis/monthly',
        name: 'MonthlyReport',
        component: () => import('@/views/analysis/MonthlyReportPage.vue'),
        meta: { title: '月度报表', icon: 'DataLine' }
      },
      {
        path: 'analysis/health',
        name: 'HealthScore',
        component: () => import('@/views/analysis/HealthScorePage.vue'),
        meta: { title: '财务健康', icon: 'TrendCharts' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard
router.beforeEach((to, _from, next) => {
  const requiresAuth = to.meta.requiresAuth !== false

  if (requiresAuth && !isAuthenticated()) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (!requiresAuth && isAuthenticated() && (to.path === '/login' || to.path === '/register')) {
    next({ path: '/dashboard' })
  } else {
    next()
  }
})

export default router
