import { createRouter, createWebHashHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { store } from '@/store';
import { tokenStore } from '@/api/http';
import { showDialog } from 'vant';

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/home' },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页', requiresAuth: false },
  },
  {
    path: '/tasks',
    name: 'Tasks',
    component: () => import('@/views/Tasks.vue'),
    meta: { title: '任务中心', requiresAuth: false },
  },
  {
    path: '/investment',
    name: 'Investment',
    component: () => import('@/views/Investment.vue'),
    meta: { title: '投资项目', requiresAuth: false },
  },
  {
    path: '/lottery',
    name: 'Lottery',
    component: () => import('@/views/Lottery.vue'),
    meta: { title: '抽奖活动', requiresAuth: true },
  },
  {
    path: '/wallet',
    name: 'Wallet',
    component: () => import('@/views/Wallet.vue'),
    meta: { title: '我的钱包', requiresAuth: true },
  },
  {
    path: '/wallet/deposit',
    name: 'WalletDeposit',
    component: () => import('@/views/wallet/Deposit.vue'),
    meta: { title: '充值', requiresAuth: true },
  },
  {
    path: '/wallet/withdraw',
    name: 'WalletWithdraw',
    component: () => import('@/views/wallet/Withdraw.vue'),
    meta: { title: '提现', requiresAuth: true },
  },
  {
    path: '/wallet/transfer',
    name: 'WalletTransfer',
    component: () => import('@/views/wallet/Transfer.vue'),
    meta: { title: '站内转账', requiresAuth: true },
  },
  {
    path: '/team',
    name: 'team',
    component: () => import('@/views/Team.vue'),
    meta: { title: '我的团队', requiresAuth: true },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  {
    path: '/leaderboard',
    name: 'Leaderboard',
    component: () => import('@/views/Leaderboard.vue'),
    meta: { title: 'Leaderboard' },
  },
]

const router = createRouter({
  // 使用 Hash 模式，部署无需服务器配置
  history: createWebHashHistory(),
  routes
});

router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string) || '移动端H5';
  if (to.meta.requiresAuth && !store.state.isLoggedIn && !tokenStore.get()) {
    showDialog({
      title: '提示',
      message: '请先登录后再访问该页面'
    }).then(() => {
      next({ name: 'Login', query: { redirect: to.fullPath } });
    });
    return;
  }
  next();
});

export default router;
