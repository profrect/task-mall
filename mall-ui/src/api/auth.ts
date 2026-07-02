import { http } from './http';

/** 登录请求体（对齐后端 UserLoginDTO）。 */
export interface LoginPayload {
  account: string;
  password: string;
}

/** 注册请求体（对齐后端 UserRegisterDTO；nickname/inviteCode 可选）。 */
export interface RegisterPayload {
  account: string;
  password: string;
  email: string;
  nickname?: string;
  inviteCode?: string;
}

/** 登录响应（对齐后端 UserLoginVO）。 */
export interface LoginResult {
  accessToken: string;
  expiresIn: number;
  tokenType: string;
}

export interface ImpersonationExchangePayload {
  ticket: string;
}

const BASE = '/api/open/user/account';

/** 登录：成功返回 sa-token 访问令牌，调用方负责落库 token。 */
export function login(payload: LoginPayload): Promise<LoginResult> {
  return http.post<LoginResult>(`${BASE}/login`, payload, false);
}

export function exchangeImpersonationTicket(payload: ImpersonationExchangePayload): Promise<LoginResult> {
  return http.post<LoginResult>(`${BASE}/impersonation-exchange`, payload, false);
}

/** 注册：不要求任何实名/KYC；inviteCode 为空则不绑定邀请关系。 */
export function register(payload: RegisterPayload): Promise<void> {
  return http.post<void>(`${BASE}/register`, payload, false);
}