import { ResultInfo } from '@/model/resultInfo';
import request from '@/api/request';

const uri = '/api/idempotent';

export function getIdemToken(): Promise<ResultInfo<string>> {
  return request({
    url: `${uri}/token`,
    method: 'get',
  });
}

export default getIdemToken;
