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

export interface PromotionPrize {
  id?: number;
  prizeCode?: string;
  prizeName: string;
  prizeType: string;
  currency: string;
  amount: number;
  stockTotal: number;
  stockUsed: number;
  sortOrder: number;
  status: number;
  remark?: string;
  createTime?: number;
  updateTime?: number;
}

export interface PromotionLotteryActivity {
  id?: number;
  activityCode?: string;
  title: string;
  description?: string;
  dailyLimit: number;
  startAt?: number;
  endAt?: number;
  sortOrder: number;
  status: number;
  createTime?: number;
  updateTime?: number;
}

export interface PromotionLotteryPrize {
  id?: number;
  activityId: number;
  prizeId: number;
  prizeCode?: string;
  prizeName?: string;
  prizeType?: string;
  currency?: string;
  amount?: number;
  weight: number;
  dailyLimit: number;
  sortOrder: number;
  status: number;
  createTime?: number;
  updateTime?: number;
}

export interface PromotionLotteryRecord {
  id: number;
  recordNo: string;
  userId: number;
  activityId: number;
  activityTitle: string;
  prizeId: number;
  prizeCode: string;
  prizeName: string;
  prizeType: string;
  currency: string;
  amount: number;
  status: string;
  walletFlowNo?: string;
  failReason?: string;
  drawnAt: number;
  settledAt?: number;
  createTime?: number;
  updateTime?: number;
}

const uri = '/api/admin/promotion/lottery';

export function promotionPrizePage(params: {
  keyword?: string;
  prizeType?: string;
  status?: number;
  pageNumber?: number;
  pageSize?: number;
}): Promise<ResultInfo<PageResult<PromotionPrize>>> {
  return request({
    url: `${uri}/prizes/list`,
    method: 'get',
    params,
  });
}

export function savePromotionPrize(data: PromotionPrize): Promise<ResultInfo<PromotionPrize>> {
  return request({
    url: `${uri}/prizes`,
    method: data.id ? 'put' : 'post',
    data,
  });
}

export function deletePromotionPrize(id: number): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/prizes/${id}`,
    method: 'delete',
  });
}

export function promotionActivityPage(params: {
  keyword?: string;
  status?: number;
  pageNumber?: number;
  pageSize?: number;
}): Promise<ResultInfo<PageResult<PromotionLotteryActivity>>> {
  return request({
    url: `${uri}/activities/list`,
    method: 'get',
    params,
  });
}

export function savePromotionActivity(
  data: PromotionLotteryActivity
): Promise<ResultInfo<PromotionLotteryActivity>> {
  return request({
    url: `${uri}/activities`,
    method: data.id ? 'put' : 'post',
    data,
  });
}

export function deletePromotionActivity(id: number): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/activities/${id}`,
    method: 'delete',
  });
}

export function promotionActivityPrizes(
  activityId: number
): Promise<ResultInfo<PromotionLotteryPrize[]>> {
  return request({
    url: `${uri}/activities/${activityId}/prizes`,
    method: 'get',
  });
}

export function savePromotionActivityPrize(
  data: PromotionLotteryPrize
): Promise<ResultInfo<PromotionLotteryPrize>> {
  return request({
    url: `${uri}/activity-prizes`,
    method: data.id ? 'put' : 'post',
    data,
  });
}

export function deletePromotionActivityPrize(id: number): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/activity-prizes/${id}`,
    method: 'delete',
  });
}

export function promotionLotteryRecordPage(params: {
  userId?: number;
  activityId?: number;
  status?: string;
  keyword?: string;
  pageNumber?: number;
  pageSize?: number;
}): Promise<ResultInfo<PageResult<PromotionLotteryRecord>>> {
  return request({
    url: `${uri}/records/list`,
    method: 'get',
    params,
  });
}