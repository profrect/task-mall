import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const DASHBOARD: AppRouteRecordRaw = {
  path: '/dashboard',
  name: 'dashboard',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.dashboard',
    requiresAuth: true,
    icon: 'icon-home',
    order: 0,
    hideInMenu: false,
  },
  children: [
    {
      path: 'home',
      name: 'home',
      component: () => import('@/views/dashboard/index.vue'),
      meta: {
        locale: 'menu.dashboard.home',
        requiresAuth: true,
        roles: ['*'],
        hideInMenu: true,
        activeMenu: 'dashboard',
      },
    },
  ],
};

export default DASHBOARD;
