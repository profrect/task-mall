import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router';
import NProgress from 'nprogress'; // progress bar
import 'nprogress/nprogress.css';

import { App } from 'vue';
import { REDIRECT_MAIN, NOT_FOUND_ROUTE, DEFAULT_LAYOUT } from './routes/base';

NProgress.configure({ showSpinner: false }); // NProgress Configuration

export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: 'login',
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/login/index.vue'),
    meta: {
      requiresAuth: false,
    },
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    meta: {
      menuKey: 'menu.dashboard',
      requiresAuth: true,
      icon: 'icon-home',
      order: 0,
      hideInMenu: false,
    },
    component: DEFAULT_LAYOUT,
    children: [
      {
        path: 'home',
        name: 'home',
        meta: {
          menuKey: 'menu.dashboard.home',
          requiresAuth: true,
          roles: ['*'],
          hideInMenu: true,
          activeMenu: 'dashboard',
        },
        component: () => import('@/views/dashboard/index.vue'),
      },
    ],
  },
  REDIRECT_MAIN,
  NOT_FOUND_ROUTE,
];

const router = createRouter({
  // createWebHashHistory() 保证网页输入url路径时router有历史记录路由数据
  history: createWebHashHistory(),
  routes: constantRoutes,
  scrollBehavior() {
    return { top: 0 };
  },
});

// 重置router
export function resetRouter() {
  router.clearRoutes();
  constantRoutes.forEach((item) => router.addRoute(item));
}

// 全局注册 router
export function setupRouter(app: App<Element>) {
  app.use(router);
}

export default router;
