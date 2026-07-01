// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';

/** 站内转账订单（管理端只读审计视图，对应 wallet WalletTransferOrderResp）。 */
export interface TransferOrderRecord {
  orderNo: string;

  fromUserId: number;

  toUserId: number;

  coin: string;

  amount: number;

  status: string;

  remark: string;

  finishedAt: number;

  createTime: number;
}

const uri = '/api/admin/transfer';

export function transferOrderList(params?: {
  userId?: number;
  limit?: number;
}): Promise<ResultInfo<Array<TransferOrderRecord>>> {
  return request({
    url: `${uri}/list`,
    method: 'get',
    params: {
      ...(params?.userId ? { userId: params.userId } : {}),
      ...(params?.limit ? { limit: params.limit } : {}),
    },
  });
}