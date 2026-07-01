// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';

export interface OrderApprovalItem {
  sourceType: 'WITHDRAW' | 'MISSION_TASK' | string;

  sourceId: string;

  title: string;

  userId: number;

  currency: string;

  amount: number;

  status: string;

  detail?: string;

  submittedAt?: number;

  createTime?: number;
}

export interface OrderApprovalReview {
  sourceType: string;

  sourceId: string;

  remark?: string;
}

const uri = '/api/admin/order-approval';

export function pendingApprovalList(): Promise<ResultInfo<Array<OrderApprovalItem>>> {
  return request({
    url: `${uri}/pending`,
    method: 'get',
  });
}

export function approveOrderApproval(
  data: OrderApprovalReview
): Promise<ResultInfo<OrderApprovalItem>> {
  return request({
    url: `${uri}/approve`,
    method: 'post',
    data,
  });
}

export function rejectOrderApproval(
  data: OrderApprovalReview
): Promise<ResultInfo<OrderApprovalItem>> {
  return request({
    url: `${uri}/reject`,
    method: 'post',
    data,
  });
}