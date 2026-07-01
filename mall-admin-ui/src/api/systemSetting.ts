// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';

export interface SystemParam {
  id?: number;

  paramKey: string;

  paramValue: string;

  description: string;

  sortOrder: number;

  status: number;

  createTime?: number;

  updateTime?: number;
}

export interface SystemParamQuery {
  keyword?: string;

  status?: number;
}

export type SystemParamPayload = Omit<SystemParam, 'createTime' | 'updateTime'>;

const uri = '/api/admin/system-param';

export function systemParamList(
  query: SystemParamQuery
): Promise<ResultInfo<Array<SystemParam>>> {
  return request({
    url: `${uri}/list`,
    method: 'get',
    params: query,
  });
}

export function saveSystemParam(
  data: SystemParamPayload
): Promise<ResultInfo<SystemParam>> {
  return request({
    url: uri,
    method: data.id ? 'put' : 'post',
    data,
  });
}

export function deleteSystemParam(id: number): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/${id}`,
    method: 'delete',
  });
}