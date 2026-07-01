// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';

export type LeaderboardType = 'EARNING' | 'RECHARGE' | 'TASK';

export interface LeaderboardItem {
  rankNo: number;

  userId: number;

  displayName?: string;

  vipLevel?: number;

  type: LeaderboardType;

  metricLabel: string;

  metricValue: number;

  currency: string;

  recordCount: number;

  lastEventTime?: number;
}

export interface LeaderboardQuery {
  type?: LeaderboardType;
  startTime?: number;
  endTime?: number;
  limit?: number;
}

const uri = '/api/admin/leaderboard';

export function leaderboardList(params?: LeaderboardQuery): Promise<ResultInfo<Array<LeaderboardItem>>> {
  return request({
    url: `${uri}/list`,
    method: 'get',
    params,
  });
}