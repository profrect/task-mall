import { DEFAULT_LAYOUT } from '../base';
import { AppRouteRecordRaw } from '../types';

const ACCOUNT: AppRouteRecordRaw = {
  path: '/account',
  name: 'account',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.account',
    requiresAuth: true,
    icon: 'icon-list',
    order: 2,
  },
  children: [
    {
      path: 'userControl', // The midline path complies with SEO specifications
      name: 'UserControl',
      component: () => import('@/views/config/user-control/index.vue'),
      meta: {
        locale: 'menu.account.user-control',
        requiresAuth: true,
        roles: ['*'],
      },
    },
    {
      path: 'userGroupControl',
      name: 'UserGroupControl',
      component: () => import('@/views/config/role-control/index.vue'),
      meta: {
        locale: 'menu.account.role-control',
        requiresAuth: true,
        roles: ['*'],
      },
    },
  ],
};

export default ACCOUNT;
