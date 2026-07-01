// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';

export type ContentItemType =
  | 'COMPANY_PROFILE'
  | 'PLATFORM_PROFILE'
  | 'REGULATOR'
  | 'NOTICE'
  | 'USER_AGREEMENT'
  | 'USER_PRIVACY';

export interface ContentItem {
  id?: number;

  type: ContentItemType;

  languageCode: string;

  title: string;

  summary: string;

  content: string;

  sortOrder: number;

  status: number;

  createTime?: number;

  updateTime?: number;
}

export interface ContentItemQuery {
  type?: ContentItemType;

  languageCode?: string;

  status?: number;
}

export type ContentItemPayload = Omit<ContentItem, 'createTime' | 'updateTime'>;

const uri = '/api/admin/content';

export function contentList(
  query: ContentItemQuery
): Promise<ResultInfo<Array<ContentItem>>> {
  return request({
    url: `${uri}/list`,
    method: 'get',
    params: query,
  });
}

export function saveContent(
  data: ContentItemPayload
): Promise<ResultInfo<ContentItem>> {
  return request({
    url: uri,
    method: data.id ? 'put' : 'post',
    data,
  });
}

export function deleteContent(id: number): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/${id}`,
    method: 'delete',
  });
}