import { createRouter, createWebHashHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import { store } from '@/store';
import { Dialog } from 'vant';
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
    path: '/wallet',
    name: 'Wallet',
    component: () => import('@/views/Wallet.vue'),
    meta: { title: '我的钱包', requiresAuth: false },
  },
  {
    path: '/team',
    name: 'team',
    component: () => import('@/views/Team.vue'),
    meta: { title: '我的团队', requiresAuth: false },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: false },
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
  if (to.meta.requiresAuth && !store.state.isLoggedIn) {
    // Dialog.alert({ title: '提示', message: '请先登录后再访问该页面' })
    //   .then(() => next({ name: 'Login', query: { redirect: to.fullPath } }))
    //   .catch(() => next('/home'));
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
