// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';

/** 综合报表快照（对应 mall-admin AdminSummaryReportResp）。 */
export interface SummaryReport {
  totalUsers: number;

  todayNewUsers: number;

  totalBalance: number;

  availableBalance: number;

  frozenBalance: number;

  walletAccounts: number;

  rechargeOrders: number;

  rechargeAmount: number;

  todayRechargeOrders: number;

  todayRechargeAmount: number;

  withdrawOrders: number;

  withdrawAmount: number;

  reviewingWithdrawOrders: number;

  todayWithdrawOrders: number;

  todayWithdrawAmount: number;

  collectOrders: number;

  collectedAmount: number;

  activeCollectOrders: number;

  todayCollectOrders: number;

  todayCollectedAmount: number;

  generatedAt: number;
}

const uri = '/api/admin/report';

export function summaryReport(): Promise<ResultInfo<SummaryReport>> {
  return request({
    url: `${uri}/summary`,
    method: 'get',
  });
}