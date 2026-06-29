import axios, { AxiosResponse } from 'axios';
import { useUserStore } from '@/store';
import { Message } from '@arco-design/web-vue';
import { httpBaseUrl } from '@/utils/env';

const service = axios.create({
  baseURL: httpBaseUrl,
  withCredentials: false,
  timeout: 30000,
});

service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore();
    const userToken = userStore.getUserToken();
    // 发送请求之前的预处理
    if (config.url !== '/api/manage/sys/user/login' && userToken) {
      // token 登陆后获取的token，身份验证
      config.headers.Authorization = userToken;
    }

    // 设置返回类型
    config.responseType = 'blob';
    return config;
  },
  (error) => {
    // 请求失败处理
    console.log('发送请求报错: ', error);
    return Promise.reject(error);
  }
);

service.interceptors.response.use(
  (response: AxiosResponse): any => {
    if (!response.data.size) {
      Message.warning(
        response.data.message ? response.data.message : '获取数据失败，请重试'
      );
      return null;
    }

    return new Blob([response.data]);
  },
  (error) => {
    console.log(`err${error}`);
    let { message } = error;
    if (message.includes('timeout')) {
      message = '获取数据超时，请重试';
    }
    Message.error(message);
    return Promise.reject(error);
  }
);

export default service;
