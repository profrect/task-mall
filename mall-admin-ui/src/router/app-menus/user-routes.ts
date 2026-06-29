import { RouteRecordRaw } from 'vue-router';
import { defineStore } from 'pinia';
import { constantRoutes } from '@/router';
import { getUserMenuList, RouteVO } from '@/api/menu';
import { ref } from 'vue';
import { DEFAULT_LAYOUT } from '@/router/routes/base';

const modules = import.meta.glob('../../views/**/**/**.vue');

/**
 * 转换路由数据为组件
 */
const transformRoutes = (routes: RouteVO[]) => {
  const asyncRoutes: RouteRecordRaw[] = [];
  routes.forEach((route) => {
    const tmpRoute = { ...route } as RouteRecordRaw;
    // 顶级目录，替换为 Layout 组件
    if (tmpRoute.component?.toString() === 'DEFAULT_LAYOUT') {
      tmpRoute.component = DEFAULT_LAYOUT;
    } else {
      // 其他菜单，根据组件路径动态加载组件
      const component = modules[`../../views/${tmpRoute.component}.vue`];
      if (component) {
        tmpRoute.component = component;
      } else {
        tmpRoute.component = modules[`../../views/not-found/index.vue`];
      }
    }

    if (tmpRoute.children) {
      tmpRoute.children = transformRoutes(route.children);
    }

    asyncRoutes.push(tmpRoute);
  });

  return asyncRoutes;
};

const userRoutesStore = defineStore('user-store', () => {
  /** 所有路由，包括静态和动态路由 */
  const routes = ref<RouteRecordRaw[]>([]);

  /**
   * 生成动态路由
   */
  function generateRoute(flag: boolean) {
    // 添加路由缓存 向后端请求系统路由数据，存入缓存；下次generateRoute时先判断缓存是否存在； 登出后清除缓存
    return new Promise<RouteRecordRaw[]>((resolve, reject) => {
      if (routes.value && routes.value.length > 0 && !flag) {
        resolve(routes.value);
      } else {
        getUserMenuList()
          .then((data) => {
            const dynamicRoutes = transformRoutes(data.data);
            routes.value = constantRoutes.concat(dynamicRoutes);
            resolve(dynamicRoutes);
          })
          .catch((error) => {
            reject(error);
          });
      }
    });
  }

  return {
    routes,
    generateRoute,
  };
});

export default userRoutesStore;
