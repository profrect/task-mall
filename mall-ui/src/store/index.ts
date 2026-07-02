import { reactive, readonly, watch } from 'vue';
import StorageUtil from '@/utils/storage';

interface State {
  isLoggedIn: boolean;
  isImpersonated: boolean;
}

function toState(value: Partial<State> | null): State {
  return {
    isLoggedIn: Boolean(value?.isLoggedIn),
    isImpersonated: Boolean(value?.isImpersonated),
  };
}

const state = reactive<State>(toState(StorageUtil.getItem<Partial<State>>('appState')));

watch(state, (val) => StorageUtil.setItem('appState', toState(val)), { deep: true });

export const store = {
  state: readonly(state),
  setLoggedIn(status: boolean) {
    state.isLoggedIn = status;
  },
  setImpersonated(status: boolean) {
    state.isImpersonated = status;
  },
};
