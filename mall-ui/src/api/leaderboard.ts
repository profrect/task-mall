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
  return http.get<LeaderboardItem[]>(`${BASE}/list`, query, false);
}