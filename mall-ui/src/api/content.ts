import { http } from './http';

export type ContentItemType =
  | 'NOTICE'
  | 'COMPANY_PROFILE'
  | 'PLATFORM_PROFILE'
  | 'REGULATOR'
  | 'USER_AGREEMENT'
  | 'USER_PRIVACY';

export interface ContentItem {
  id: number;
  type: ContentItemType;
  languageCode: string;
  title: string;
  summary?: string;
  content: string;
  sortOrder: number;
  status: number;
  createTime?: number;
  updateTime?: number;
}

export interface ContentNotice extends Omit<ContentItem, 'type'> {
  type: 'NOTICE';
}

export interface OpenServiceBot {
  botName: string;
  description?: string;
}

export interface OpenServiceConfig {
  title: string;
  message: string;
  bots: OpenServiceBot[];
}

const BASE = '/api/open/content';

export function getNotices(languageCode = 'zh-CN'): Promise<ContentNotice[]> {
  return http.get<ContentNotice[]>(`${BASE}/notices`, { languageCode }, false);
}

export function getContentItems(type: ContentItemType, languageCode = 'zh-CN'): Promise<ContentItem[]> {
  return http.get<ContentItem[]>(`${BASE}/list`, { type, languageCode }, false);
}

export function getServiceConfig(): Promise<OpenServiceConfig> {
  return http.get<OpenServiceConfig>(`${BASE}/service`, undefined, false);
}