import { setRouteEmitter } from '@/utils/route-listener';
import router from '@/router';
import setupUserLoginInfoGuard from './userLoginInfo';
import setupPermissionGuard from './permission';

function setupPageGuard() {
  router.beforeEach(async (to) => {
    // emit route change
    setRouteEmitter(to);
  });
}

export default function createRouteGuard() {
  setupPageGuard();
  setupUserLoginInfoGuard();
  setupPermissionGuard();
}
