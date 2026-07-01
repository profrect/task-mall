// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';

export interface PageResult<T> {
  records: T[];
  pageNumber: number;
  pageSize: number;
  totalRow: number;
  totalPage: number;
}

/** 邀请返佣记录：来源于 mall-user 的真实返佣事实，钱包流水只做入账对账。 */
export interface InviteCommissionRecord {
  id: number;
  recordNo: string;
  inviterUserId: number;
  sourceUserId: number;
  sourceOrderNo: string;
  businessType: string;
  currency: string;
  sourceAmount: number;
  commissionRate: number;
  commissionAmount: number;
  status: string;
  walletFlowNo?: string;
  failReason?: string;
  settledAt?: number;
  createTime?: number;
  updateTime?: number;
}

const uri = '/api/admin/invite-commission';

export function inviteCommissionPage(params: {
  inviterUserId?: number;
  sourceUserId?: number;
  sourceOrderNo?: string;
  businessType?: string;
  status?: string;
  keyword?: string;
  pageNumber?: number;
  pageSize?: number;
}): Promise<ResultInfo<PageResult<InviteCommissionRecord>>> {
  return request({
    url: `${uri}/list`,
    method: 'get',
    params,
  });
}