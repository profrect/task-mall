import router from '@/router';
import axios, { InternalAxiosRequestConfig } from 'axios';
// eslint-disable-next-line import/no-cycle
import { useAppStore, useUserStore } from '@/store';
import { ResultInfo } from '@/model/resultInfo';
import { Message } from '@arco-design/web-vue';
import { httpBaseUrl } from '@/utils/env';

const service = axios.create({
  baseURL: httpBaseUrl,
  withCredentials: false,
  timeout: 0,
});

const notLoginList = ['/api/manage/sys/user/login'];
service.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  useAppStore().openPageLoading();
  const userStore = useUserStore();

  const userToken = userStore.getUserToken();

  // 没有token也不是访问登陆则重定向到登陆
  if (!userToken && notLoginList.includes(config.url as string)) {
    router.push('/login');
  }

  config.headers.Authorization = userToken;

  if (config.method === 'post' || config.method === 'put') {
    config.headers['Content-Type'] = 'application/json';
  }

  if (config.data instanceof FormData) {
    delete config.headers['Content-Type'];
  }

  return config;
});

service.interceptors.response.use(
  (response) => {
    useAppStore().closePageLoading();

    if (
      response.config.responseType === 'blob' ||
      response.config.responseType === 'arraybuffer'
    ) {
      return response;
    }

    const { data } = response;
    // 所有错误请求失败信息显示
    if (data.code === 0) {
      return data;
    }

    Message.error(data.msg);
    if (data.code === 10010) {
      // 登录过期清除缓存
      useUserStore().cleanUserToken();
      router.push('/login').then();
    }
    return data;
  },
  (error: ResultInfo<null>) => {
    console.log(error);
    Message.error(error.msg || '请求失败，请稍后再试');
    useAppStore().closePageLoading();
  }
);

export default service;
