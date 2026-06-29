import { defineStore } from 'pinia';
import type { RouteRecordNormalized } from 'vue-router';
import defaultSettings from '@/config/settings.json';
import { AppState } from './types';

const useAppStore = defineStore('app', {
  state: (): AppState => ({
    ...defaultSettings,
    pageLoading: false,
    routerType: false,
  }),

  getters: {
    appCurrentSetting(state: AppState): AppState {
      return { ...state };
    },
    appDevice(state: AppState) {
      return state.device;
    },
    appAsyncMenus(state: AppState): RouteRecordNormalized[] {
      return state.serverMenu as unknown as RouteRecordNormalized[];
    },
  },

  actions: {
    setRouterType(type: boolean) {
      this.routerType = type;
    },
    getRouterType() {
      return this.routerType;
    },
    // Update app settings
    updateSettings(partial: Partial<AppState>) {
      // @ts-ignore-next-line
      this.$patch(partial);
    },

    // Change theme color
    toggleTheme(dark: boolean) {
      if (dark) {
        this.theme = 'dark';
        document.body.setAttribute('arco-theme', 'dark');
      } else {
        this.theme = 'light';
        document.body.removeAttribute('arco-theme');
      }
    },
    toggleDevice(device: string) {
      this.device = device;
    },
    toggleMenu(value: boolean) {
      this.hideMenu = value;
    },
    // async fetchServerMenuConfig() {
    //   let notifyInstance: NotificationReturn | null = null;
    //   try {
    //     notifyInstance = Notification.info({
    //       id: 'menuNotice', // Keep the instance id the same
    //       content: 'loading',
    //       closable: true,
    //     });
    //     const { data } = await getMenuList(0);
    //     this.serverMenu = data;
    //     notifyInstance = Notification.success({
    //       id: 'menuNotice',
    //       content: 'success',
    //       closable: true,
    //     });
    //   } catch (error) {
    //     // eslint-disable-next-line @typescript-eslint/no-unused-vars
    //     notifyInstance = Notification.error({
    //       id: 'menuNotice',
    //       content: 'error',
    //       closable: true,
    //     });
    //   }
    // },
    clearServerMenu() {
      this.serverMenu = [];
    },
    openPageLoading() {
      this.pageLoading = true;
    },
    closePageLoading() {
      this.pageLoading = false;
    },
  },
});

export default useAppStore;
