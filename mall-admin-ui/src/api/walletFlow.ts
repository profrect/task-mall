// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';

/** 账务流水（管理端只读审计视图，对应 wallet WalletFlowResp）。 */
export interface WalletFlowRecord {
  flowNo: string;

  userId: number;

  bizType: string;

  bizId: string;

  direction: string;

  changeAmt: number;

  balanceBefore: number;

  balanceAfter: number;

  remark: string;

  createTime: number;
}

const uri = '/api/admin/wallet-flow';

export function walletFlowList(params?: {
  userId?: number;
  limit?: number;
}): Promise<ResultInfo<Array<WalletFlowRecord>>> {
  return request({
    url: `${uri}/list`,
    method: 'get',
    params: {
      ...(params?.userId ? { userId: params.userId } : {}),
      ...(params?.limit ? { limit: params.limit } : {}),
    },
  });
}