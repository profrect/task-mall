import { showFailToast } from 'vant';
import StorageUtil from '@/utils/storage';

/**
 * 后端统一返回包络。约定 code===0 为成功，data 为业务载荷。
 */
export interface ApiResult<T> {
  code: number;
  msg: string;
  data: T;
  success?: boolean;
}

/** 业务成功码（对齐后端 CommonRespCode.OK）。 */
const OK_CODE = 0;
/** 认证失效码（对齐后端 CommonRespCode.AUTH_DENIED）。 */
const AUTH_DENIED_CODE = 10010;

const TOKEN_KEY = 'token';

/**
 * 基地址：开发期留空，由 vite 代理把 /api 转发到各后端；生产经 VITE_API_BASE 指向网关/同源。
 */
const BASE = import.meta.env.VITE_API_BASE ?? '';

export const tokenStore = {
  get: () => StorageUtil.getItem<string>(TOKEN_KEY),
  set: (token: string) => StorageUtil.setItem(TOKEN_KEY, token),
  clear: () => StorageUtil.removeItem(TOKEN_KEY),
};

/** 业务错误：携带后端 code，便于调用方按码分支处理。 */
export class ApiError extends Error {
  constructor(public readonly code: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT';
  /** 查询参数（GET 常用）。 */
  query?: Record<string, string | number | undefined>;
  /** JSON 请求体（POST 常用）。 */
  body?: unknown;
  /** 是否携带登录态。默认 true；登录/注册等开放接口传 false。 */
  auth?: boolean;
}

/**
 * 统一请求内核：拼地址 → 注入鉴权头 → 解包 ApiResult → 异常归一化。
 * 所有领域 api（auth/wallet/...）都建立在此之上，避免每处重复处理 token、错误与 Toast。
 */
async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', query, body, auth = true } = options;

  const url = new URL(BASE + path, window.location.origin);
  if (query) {
    Object.entries(query).forEach(([k, v]) => {
      if (v !== undefined && v !== null) url.searchParams.set(k, String(v));
    });
  }

  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  if (auth) {
    const token = tokenStore.get();
    // sa-token 配置 token-name=Authorization, token-prefix=Bearer
    if (token) headers['Authorization'] = `Bearer ${token}`;
  }

  let resp: Response;
  try {
    resp = await fetch(url.toString(), {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    showFailToast('网络异常，请稍后重试');
    throw new ApiError(-1, 'network error');
  }

  if (resp.status === 401) {
    handleAuthExpired();
    throw new ApiError(AUTH_DENIED_CODE, '登录已失效');
  }

  let result: ApiResult<T>;
  try {
    result = await resp.json();
  } catch {
    showFailToast('响应解析失败');
    throw new ApiError(-1, 'invalid json');
  }

  if (result.code === AUTH_DENIED_CODE) {
    handleAuthExpired();
    throw new ApiError(result.code, result.msg);
  }
  if (result.code !== OK_CODE) {
    showFailToast(result.msg || '请求失败');
    throw new ApiError(result.code, result.msg);
  }
  return result.data;
}

/** 登录态失效：清 token 并跳登录页（hash 路由，避免引入 router 造成循环依赖）。 */
function handleAuthExpired() {
  tokenStore.clear();
  if (!window.location.hash.startsWith('#/login')) {
    window.location.hash = '#/login';
  }
}

export const http = {
  get: <T>(path: string, query?: RequestOptions['query'], auth = true) =>
    request<T>(path, { method: 'GET', query, auth }),
  post: <T>(path: string, body?: unknown, auth = true) =>
    request<T>(path, { method: 'POST', body, auth }),
  put: <T>(path: string, body?: unknown, auth = true) =>
    request<T>(path, { method: 'PUT', body, auth }),
};