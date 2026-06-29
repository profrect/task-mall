import { defineStore } from 'pinia';
// eslint-disable-next-line import/no-cycle
import { getUserInfo, LoginData, User, login, logoutUser } from '@/api/user';
import { removeRouteListener } from '@/utils/route-listener';
import {
  getItem,
  getItemJson,
  removeItem,
  setItem,
  setItemJson,
} from '@/store/storage/storage';
import router from '@/router';
import { Message } from '@arco-design/web-vue';
import useAppStore from '../app';

export const AES_KEY = 'aes_key';
export const USER_TOKEN_KEY = 'user_token_key';
export const USER_INFO = 'user_info';

const useUserStore = defineStore('user', {
  state() {
    return {
      userInfo: new User({}),
      gamePagePower: false,
    };
  },

  actions: {
    setUserToken(token: string) {
      setItem(USER_TOKEN_KEY, `Bearer ${token}`);
    },
    getUserToken() {
      return getItem(USER_TOKEN_KEY);
    },
    cleanUserToken() {
      return removeItem(USER_TOKEN_KEY);
    },

    setGamePagePower(flag: boolean) {
      this.gamePagePower = flag;
    },
    getGamePagePower() {
      return this.gamePagePower;
    },

    setAesKey(aesKey: string) {
      setItem(AES_KEY, aesKey);
    },
    getAesKey() {
      return getItem(AES_KEY);
    },
    cleanAesKey() {
      return removeItem(AES_KEY);
    },

    setUserInfo(user: User) {
      this.userInfo = user;
      setItemJson(USER_INFO, user);
    },
    async getUserInfo(): Promise<User> {
      if (this.userInfo.accessToken) {
        if (this.userInfo.username === null) {
          this.userInfo = getItemJson(USER_INFO) as User;
        }
        return this.userInfo;
      }
      await getUserInfo().then((rep) => {
        if (rep.success) {
          this.setUserInfo(rep.data);
        }
      });
      return this.userInfo;
    },
    // Reset user's information
    resetInfo() {
      this.$reset();
    },

    // Login
    async login(loginForm: LoginData) {
      const res = await login(loginForm).then((rep) => {
        if (rep.success) {
          // 保存登陆的用户信息
          const newUserInfo = new User(rep.data);
          this.setUserInfo(newUserInfo);
          this.setUserToken(newUserInfo.accessToken);
          Message.success(rep.msg);
          // 跳转到游戏首页
          router.push('/dashboard/home').then();
        }
        return rep;
      });
      return res;
    },
    logoutCallBack() {
      const appStore = useAppStore();
      this.resetInfo();
      this.cleanUserToken();
      removeRouteListener();
      appStore.clearServerMenu();
    },
    // 退出登录
    logout(b?: boolean): Promise<boolean> {
      return new Promise((resolve) => {
        logoutUser().then((rep) => {
          if (rep && rep.success) {
            this.resetInfo();
            this.cleanUserToken();
            this.cleanAesKey();
            if (b) {
              Message.success(rep.msg);
            }
            resolve(true);
          }
        });
      });
    },
  },
});

export default useUserStore;
