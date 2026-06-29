import type {
  NavigationGuardNext,
  RouteLocationNormalized,
  RouteLocationNormalizedGeneric,
  RouteRecordRaw,
} from 'vue-router';
import NProgress from 'nprogress'; // progress bar

import usePermission from '@/hooks/permission';
import { USER_TOKEN_KEY } from '@/store/user';
import userRoutesStore from '@/router/app-menus/user-routes';
import router from '@/router';
import { useAppStore, useUserStore } from '@/store';

/** 重定向到登录页 */
function redirectToLogin(
  to: RouteLocationNormalized,
  next: NavigationGuardNext
) {
  const params = new URLSearchParams(to.query as Record<string, string>);
  const queryString = params.toString();
  const redirect = queryString ? `${to.path}?${queryString}` : to.path;
  next(`/login?redirect=${encodeURIComponent(redirect)}`);
}

const cleanRedirectPath = (to: RouteLocationNormalizedGeneric) => {
  // 解码URL编码（%2F会被自动还原为/，同时兼容其他编码如%20等）
  const decodedPath = decodeURIComponent(to.path);

  // 清理首尾多余的/（如需严格规范路径，可保留）
  to.path = decodedPath.replace(/^\/+|\/+$/g, '/').replace(/\/\/+/g, '/');
};

export default function setupPermissionGuard() {
  router.beforeEach(async (to, from, next) => {
    NProgress.start();
    // 查看是否是重定向url并进行处理
    if (to.name === 'redirect') {
      cleanRedirectPath(to);
    }
    const token = window.localStorage.getItem(USER_TOKEN_KEY);
    if (token) {
      if (to.path.startsWith('/login')) {
        // 如果已登录，跳转到首页
        next({ path: '/dashboard/home' });
      } else {
        const permissionStore = userRoutesStore();
        useAppStore().setRouterType(false);
        const dynamicRoutes = await permissionStore.generateRoute(false);
        dynamicRoutes.forEach((route: RouteRecordRaw) =>
          router.addRoute(route)
        );

        const routes = router.getRoutes();
        // 添加权限路由是否包含当前页面判断，无则跳转错误页
        const permissionsAllow = usePermission().accessRouter(to, routes);
        if (permissionsAllow) {
          if (to.matched.length < 2) {
            // 如果未匹配到任何路由，重新添加，还是无匹配则跳转到not-found页面
            const newRoute = routes.filter((obj) => obj.path === to.path);
            to.matched.push(...newRoute);
            to.name = newRoute[0].name;
            if (
              newRoute.length !== 0 &&
              to.matched.length !== 0 &&
              to.name !== ''
            ) {
              next({ name: to.name });
            } else {
              next(from.name ? { name: from.name } : '/not-found');
            }
          } else {
            next();
          }
        } else if (to.path !== '' && to.name !== '') {
          next({ path: '/dashboard/home' });
        } else {
          next(from.name ? { name: from.name } : '/not-found');
        }
      }
    } else if (to.path === '/login') {
      useUserStore().cleanAesKey();
      next();
    } else {
      useUserStore().cleanAesKey();
      next('/login');
    }
  });

  // 在导航真正完成后停止进度条
  router.afterEach((to, from) => {
    NProgress.done();
  });
}
