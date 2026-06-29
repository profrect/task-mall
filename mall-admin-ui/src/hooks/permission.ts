import { RouteLocationNormalized, RouteRecordRaw } from 'vue-router';
import { useUserStore } from '@/store';

export default function usePermission() {
  return {
    accessRouter(
      route: RouteLocationNormalized | RouteRecordRaw,
      routes: RouteRecordRaw[]
    ) {
      const newRoute = routes.filter((obj) => obj.path === route.path);
      return newRoute.length > 0;
    },
    accessGamePage(routes: RouteRecordRaw[]): void {
      const flag = routes.filter((obj) => obj.path === '/dashboard/game-page');
      useUserStore().setGamePagePower(flag.length !== 0);
    },
    findFirstPermissionRoute(_routers: any, role = 'admin') {
      const cloneRouters = [..._routers];
      while (cloneRouters.length) {
        const firstElement = cloneRouters.shift();
        if (
          firstElement?.meta?.roles?.find((el: string[]) => {
            return el.includes('*') || el.includes(role);
          })
        )
          return { name: firstElement.name };
        if (firstElement?.children) {
          cloneRouters.push(...firstElement.children);
        }
      }
      return null;
    },
  };
}
