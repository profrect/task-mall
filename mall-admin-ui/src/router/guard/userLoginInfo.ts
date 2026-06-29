import type { LocationQueryRaw } from 'vue-router';
import NProgress from 'nprogress'; // progress bar

import { useUserStore } from '@/store';
import { isItem } from '@/store/storage/storage';
import { USER_TOKEN_KEY } from '@/store/user';
import router from '@/router';

export default function setupUserLoginInfoGuard() {
  router.beforeEach(async (to, from, next) => {
    NProgress.start();
    const userStore = useUserStore();
    if (isItem(USER_TOKEN_KEY)) {
      if (userStore.userInfo) {
        next();
      } else {
        try {
          await userStore.getUserInfo();
          next();
        } catch (error) {
          await userStore.logout();
          next({
            name: 'login',
            query: {
              redirect: to.name,
              ...to.query,
            } as LocationQueryRaw,
          });
        }
      }
    } else {
      if (to.name === 'login') {
        next();
        return;
      }
      next({
        name: 'login',
        query: {
          redirect: to.name,
          ...to.query,
        } as LocationQueryRaw,
      });
    }
  });
}
