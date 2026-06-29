import { useRouter } from 'vue-router';
import { Message } from '@arco-design/web-vue';
import { resetRouter } from '@/router';

import { useTabBarStore, useUserStore } from '@/store';
import userRoutesStore from '@/router/app-menus/user-routes';

export default function useUser() {
  const router = useRouter();
  const userStore = useUserStore();
  const logout = async (logoutTo?: string) => {
    await userStore.logout();
    const currentRoute = router.currentRoute.value;
    Message.success('登出成功');
    // 清除路由缓存
    resetRouter();
    // 重置tabs菜单列表
    useTabBarStore().resetTabList();
    router.push({
      name: logoutTo && typeof logoutTo === 'string' ? logoutTo : 'login',
      query: {
        ...router.currentRoute.value.query,
        redirect: currentRoute.name as string,
      },
    });
  };
  return {
    logout,
  };
}
