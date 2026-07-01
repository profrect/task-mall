// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';

/** 归集订单（管理端只读视图，对应 wallet CollectOrderResp）。归集是平台内部资金搬运，不涉及用户账务。 */
export interface CollectOrder {
  orderNo: string;

  userId: number;

  chainCode: string;

  coin: string;

  fromAddress: string;

  toAddress: string;

  amount: number;

  sweptAmount: number;

  gasTxHash: string;

  sweepTxHash: string;

  confirmations: number;

  status: string;

  remark: string;

  gasSentAt: number;

  sweptAt: number;

  finishedAt: number;

  createTime: number;
}

const uri = '/api/admin/collect';

export function activeCollectList(): Promise<ResultInfo<Array<CollectOrder>>> {
  return request({
    url: `${uri}/active`,
    method: 'get',
  });
}

export function collectListByStatus(
  status: string
): Promise<ResultInfo<Array<CollectOrder>>> {
  return request({
    url: `${uri}/list`,
    method: 'get',
    params: { status },
  });
}

export function scanCollect(chainCode?: string): Promise<ResultInfo<number>> {
  return request({
    url: `${uri}/scan`,
    method: 'post',
    params: chainCode ? { chainCode } : {},
  });
}

export function advanceCollect(): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/advance`,
    method: 'post',
  });
}