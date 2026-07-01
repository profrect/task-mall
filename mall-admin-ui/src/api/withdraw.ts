// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';

/** 提现订单（管理端只读视图，对应 wallet WithdrawOrderResp）。 */
export interface WithdrawOrder {
  orderNo: string;

  userId: number;

  chainCode: string;

  coin: string;

  amount: number;

  fee: number;

  receiveAmount: number;

  toAddress: string;

  fromAddress: string;

  txHash: string;

  confirmations: number;

  status: string;

  reviewer: string;

  reviewRemark: string;

  reviewedAt: number;

  broadcastAt: number;

  finishedAt: number;

  createTime: number;
}

/** 审核入参：审核人由服务端从登录态解析，前端只传单号与备注。 */
export interface WithdrawReview {
  orderNo: string;

  remark?: string;
}

const uri = '/api/admin/withdraw';

export function reviewingList(): Promise<ResultInfo<Array<WithdrawOrder>>> {
  return request({
    url: `${uri}/reviewing`,
    method: 'get',
  });
}

export function approveWithdraw(
  data: WithdrawReview
): Promise<ResultInfo<WithdrawOrder>> {
  return request({
    url: `${uri}/approve`,
    method: 'post',
    data,
  });
}

export function rejectWithdraw(
  data: WithdrawReview
): Promise<ResultInfo<WithdrawOrder>> {
  return request({
    url: `${uri}/reject`,
    method: 'post',
    data,
  });
}