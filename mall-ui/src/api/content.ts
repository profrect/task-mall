import { http } from './http';

export interface ContentNotice {
  id: number;
  type: 'NOTICE';
  languageCode: string;
  title: string;
  summary?: string;
  content: string;
  sortOrder: number;
  status: number;
  createTime?: number;
  updateTime?: number;
}

const BASE = '/api/open/content';

export function getNotices(languageCode = 'zh-CN'): Promise<ContentNotice[]> {
  return http.get<ContentNotice[]>(`${BASE}/notices`, { languageCode }, false);
}