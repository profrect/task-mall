import { showFailToast } from 'vant';
import { store } from '@/store';

const READONLY_MESSAGE = '当前为后台模拟登录，仅可查看，不能操作';

export function isImpersonatedSession(): boolean {
  return Boolean(store.state.isImpersonated);
}

export function rejectIfImpersonated(message = READONLY_MESSAGE): boolean {
  if (!isImpersonatedSession()) {
    return false;
  }
  showFailToast(message);
  return true;
}