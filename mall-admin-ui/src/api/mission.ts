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

export interface MissionGoods {
  id?: number;
  goodsType: string;
  goodsCode?: string;
  title: string;
  description?: string;
  currency: string;
  minAmount: number;
  maxAmount: number;
  displayRate: number;
  cycleDays: number;
  riskLevel: string;
  sortOrder: number;
  status: number;
  createTime?: number;
  updateTime?: number;
}

export interface MissionTask {
  id?: number;
  taskCode?: string;
  title: string;
  description?: string;
  taskType: string;
  currency: string;
  rewardAmount: number;
  requiredVipLevel: number;
  dailyLimit: number;
  startAt?: number;
  endAt?: number;
  sortOrder: number;
  status: number;
  createTime?: number;
  updateTime?: number;
}

export interface MissionUserTask {
  id: number;
  userId: number;
  taskId: number;
  taskCode: string;
  taskTitle: string;
  taskType: string;
  currency: string;
  rewardAmount: number;
  status: string;
  submitContent?: string;
  reviewRemark?: string;
  walletFlowNo?: string;
  claimedAt?: number;
  submittedAt?: number;
  reviewedAt?: number;
  finishedAt?: number;
  createTime?: number;
  updateTime?: number;
}

const uri = '/api/admin/mission';

export function missionGoodsPage(params: {
  goodsType?: string;
  keyword?: string;
  status?: number;
  pageNumber?: number;
  pageSize?: number;
}): Promise<ResultInfo<PageResult<MissionGoods>>> {
  return request({
    url: `${uri}/goods/list`,
    method: 'get',
    params,
  });
}

export function saveMissionGoods(data: MissionGoods): Promise<ResultInfo<MissionGoods>> {
  return request({
    url: `${uri}/goods`,
    method: data.id ? 'put' : 'post',
    data,
  });
}

export function deleteMissionGoods(id: number): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/goods/${id}`,
    method: 'delete',
  });
}

export function missionTaskPage(params: {
  taskType?: string;
  keyword?: string;
  status?: number;
  pageNumber?: number;
  pageSize?: number;
}): Promise<ResultInfo<PageResult<MissionTask>>> {
  return request({
    url: `${uri}/tasks/list`,
    method: 'get',
    params,
  });
}

export function saveMissionTask(data: MissionTask): Promise<ResultInfo<MissionTask>> {
  return request({
    url: `${uri}/tasks`,
    method: data.id ? 'put' : 'post',
    data,
  });
}

export function deleteMissionTask(id: number): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/tasks/${id}`,
    method: 'delete',
  });
}

export function missionRecordPage(params: {
  userId?: number;
  taskId?: number;
  status?: string;
  keyword?: string;
  pageNumber?: number;
  pageSize?: number;
}): Promise<ResultInfo<PageResult<MissionUserTask>>> {
  return request({
    url: `${uri}/records/list`,
    method: 'get',
    params,
  });
}

export function approveMissionRecord(data: {
  recordId: number;
  reviewRemark?: string;
}): Promise<ResultInfo<MissionUserTask>> {
  return request({
    url: `${uri}/records/approve`,
    method: 'post',
    data,
  });
}

export function rejectMissionRecord(data: {
  recordId: number;
  reviewRemark?: string;
}): Promise<ResultInfo<MissionUserTask>> {
  return request({
    url: `${uri}/records/reject`,
    method: 'post',
    data,
  });
}