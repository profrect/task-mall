import { App } from 'vue';
import createRouteGuard from '@/router/guard';
import { setupRouter } from '@/router';

export default {
  install(app: App<Element>) {
    // 路由
    setupRouter(app);
    // 路由守卫
    createRouteGuard();
  },
};
