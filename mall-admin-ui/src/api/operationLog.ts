// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';

export interface OperationLogRecord {
  id: number;

  adminAccount: string;

  method: string;

  path: string;

  queryString: string;

  action: string;

  statusCode: number;

  success: number;

  durationMs: number;

  ipAddress: string;

  userAgent: string;

  createTime: number;
}

export interface OperationLogQuery {
  adminAccount?: string;

  action?: string;

  success?: number;

  startTime?: number;

  endTime?: number;

  limit?: number;
}

const uri = '/api/admin/operation-log';

export function operationLogList(
  query: OperationLogQuery
): Promise<ResultInfo<Array<OperationLogRecord>>> {
  return request({
    url: `${uri}/list`,
    method: 'get',
    params: query,
  });
}