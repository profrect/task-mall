import { http } from './http';

export interface PromotionLotteryActivity {
  id: number;
  activityCode: string;
  title: string;
  description?: string;
  dailyLimit: number;
  startAt?: number;
  endAt?: number;
  sortOrder: number;
  status: number;
}

export interface PromotionLotteryPrize {
  id: number;
  activityId: number;
  prizeId: number;
  prizeCode: string;
  prizeName: string;
  prizeType: string;
  currency: string;
  amount: number;
  weight: number;
  dailyLimit: number;
  sortOrder: number;
  status: number;
}

export interface PromotionLotteryDetail {
  activity: PromotionLotteryActivity;
  prizes: PromotionLotteryPrize[];
  todayDrawCount: number;
  todayRemainCount: number;
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
}

const BASE = '/api/open/promotion/lottery';

export function getLotteryActivities(): Promise<PromotionLotteryDetail[]> {
  return http.get<PromotionLotteryDetail[]>(`${BASE}/activities`);
}

export function drawLottery(activityId: number): Promise<PromotionLotteryRecord> {
  return http.post<PromotionLotteryRecord>(`${BASE}/activities/${activityId}/draw`);
}

export function getLotteryRecords(limit = 30): Promise<PromotionLotteryRecord[]> {
  return http.get<PromotionLotteryRecord[]>(`${BASE}/records`, { limit });
}