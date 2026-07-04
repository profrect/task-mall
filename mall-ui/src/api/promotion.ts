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

const LOTTERY_BASE = '/api/open/promotion/lottery';
const COUPON_BASE = '/api/open/promotion/coupon';
const CHECKIN_BASE = '/api/open/promotion/checkin';

export function getLotteryActivities(): Promise<PromotionLotteryDetail[]> {
  return http.get<PromotionLotteryDetail[]>(`${LOTTERY_BASE}/activities`);
}

export function drawLottery(activityId: number): Promise<PromotionLotteryRecord> {
  return http.post<PromotionLotteryRecord>(`${LOTTERY_BASE}/activities/${activityId}/draw`);
}

export function getLotteryRecords(limit = 30): Promise<PromotionLotteryRecord[]> {
  return http.get<PromotionLotteryRecord[]>(`${LOTTERY_BASE}/records`, { limit });
}

export interface PromotionCouponTemplate {
  id: number;
  couponCode: string;
  title: string;
  description?: string;
  couponType: string;
  currency: string;
  discountAmount: number;
  minOrderAmount: number;
  totalStock: number;
  claimedStock: number;
  remainStock: number;
  perUserLimit: number;
  userClaimedCount: number;
  validDays: number;
  startAt?: number;
  endAt?: number;
  status: number;
}

export interface PromotionCouponRecord {
  id: number;
  recordNo: string;
  templateId: number;
  couponCode: string;
  title: string;
  couponType: string;
  currency: string;
  discountAmount: number;
  minOrderAmount: number;
  status: string;
  validFrom: number;
  validTo: number;
  claimedAt: number;
  lockedAt?: number;
  usedAt?: number;
  expiredAt?: number;
}

export interface PromotionCheckinRule {
  id: number;
  ruleCode: string;
  title: string;
  requiredConsecutiveDays: number;
  currency: string;
  rewardAmount: number;
  status: number;
}

export interface PromotionCheckinRecord {
  id: number;
  recordNo: string;
  checkinDate: number;
  consecutiveDays: number;
  currency: string;
  rewardAmount: number;
  status: string;
  walletFlowNo?: string;
  failReason?: string;
  checkedAt: number;
  settledAt?: number;
}

export interface PromotionCheckinState {
  checkedToday: boolean;
  todayDate: number;
  consecutiveDays: number;
  todayRule?: PromotionCheckinRule;
  todayRecord?: PromotionCheckinRecord;
  rules: PromotionCheckinRule[];
  recentRecords: PromotionCheckinRecord[];
}

export function getCouponTemplates(limit = 30): Promise<PromotionCouponTemplate[]> {
  return http.get<PromotionCouponTemplate[]>(`${COUPON_BASE}/templates`, { limit });
}

export function claimCoupon(templateId: number): Promise<PromotionCouponRecord> {
  return http.post<PromotionCouponRecord>(`${COUPON_BASE}/templates/${templateId}/claim`);
}

export function getCouponRecords(status?: string, limit = 30): Promise<PromotionCouponRecord[]> {
  return http.get<PromotionCouponRecord[]>(`${COUPON_BASE}/records`, { status, limit });
}

export function getCheckinState(limit = 30): Promise<PromotionCheckinState> {
  return http.get<PromotionCheckinState>(`${CHECKIN_BASE}/state`, { limit });
}

export function checkin(): Promise<PromotionCheckinRecord> {
  return http.post<PromotionCheckinRecord>(CHECKIN_BASE);
}

export function getCheckinRecords(limit = 30): Promise<PromotionCheckinRecord[]> {
  return http.get<PromotionCheckinRecord[]>(`${CHECKIN_BASE}/records`, { limit });
}
