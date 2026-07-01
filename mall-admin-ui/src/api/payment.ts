// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';

export interface PaymentPageResult<T> {
  records: T[];
  pageNumber: number;
  pageSize: number;
  totalRow: number;
  totalPage: number;
}

export interface PaymentOrderRecord {
  id: number;

  orderNo: string;

  userId: number;

  businessType: string;

  businessOrderNo?: string;

  channelCode: string;

  channelOrderNo?: string;

  currency: string;

  amount: number;

  status: string;

  payAddress?: string;

  payerAddress?: string;

  txHash?: string;

  auditRemark?: string;

  paidAt?: number;

  expiredAt?: number;

  createTime?: number;

  updateTime?: number;
}

export interface PaymentOrderQuery {
  userId?: number;

  orderNo?: string;

  businessType?: string;

  channelCode?: string;

  status?: string;

  startTime?: number;

  endTime?: number;

  pageNumber?: number;

  pageSize?: number;
}

const uri = '/api/admin/payment';

export function paymentOrderPage(
  params: PaymentOrderQuery
): Promise<ResultInfo<PaymentPageResult<PaymentOrderRecord>>> {
  return request({
    url: `${uri}/list`,
    method: 'get',
    params,
  });
}