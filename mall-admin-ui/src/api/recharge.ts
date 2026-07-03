// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';

/** 充值订单（管理端只读视图，对应 wallet RechargeOrderResp）。充值为链上自动入账，管理端仅监控不干预。 */
export interface RechargeOrder {
  orderNo: string;

  userId: number;

  chainCode: string;

  coin: string;

  amount: number;

  fromAddress: string;

  toAddress: string;

  txHash: string;

  confirmations: number;

  status: string;

  creditedAt: number;

  createTime: number;
}

export interface RechargeManualPayload {
  userId: number;

  amount: number;

  coin?: string;

  referenceNo?: string;

  remark?: string;
}

const uri = '/api/admin/recharge';

export function rechargeList(
  status?: string,
  limit?: number,
  userId?: number
): Promise<ResultInfo<Array<RechargeOrder>>> {
  return request({
    url: `${uri}/list`,
    method: 'get',
    params: {
      ...(status ? { status } : {}),
      ...(limit ? { limit } : {}),
      ...(userId ? { userId } : {}),
    },
  });
}

export function manualRechargeCredit(
  data: RechargeManualPayload
): Promise<ResultInfo<RechargeOrder>> {
  return request({
    url: `${uri}/manual-credit`,
    method: 'post',
    data,
  });
}