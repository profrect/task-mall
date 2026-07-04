import { http } from './http';

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

const BASE = '/api/open/leaderboard';

export function getLeaderboard(query: LeaderboardQuery): Promise<LeaderboardItem[]> {
  const params: Record<string, string | number | undefined> = {
    type: query.type,
    startTime: query.startTime,
    endTime: query.endTime,
    limit: query.limit,
  };
  return http.get<LeaderboardItem[]>(`${BASE}/list`, params, false);
}