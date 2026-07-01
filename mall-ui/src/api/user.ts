import { http } from './http';

/** 当前登录用户资料（对齐后端 UserDetailVO）。 */
export interface UserDetail {
  userId: number;
  account?: string;
  nickName?: string;
  vipLevel: string;
  inviteCode?: string;
  inviteUser?: string;
  teamMemberNum: number;
}

/** 直属团队成员（对齐后端 TeamMembersVo）。 */
export interface TeamMemberRecord {
  nickname?: string;
  vipLevel: string;
  invitateTime: number;
}

/** 邀请返佣记录（当前用户作为邀请人获得的真实收益）。 */
export interface InviteCommissionRecord {
  id: number;
  recordNo: string;
  inviterUserId: number;
  sourceUserId: number;
  sourceOrderNo: string;
  businessType: string;
  currency: string;
  sourceAmount: number;
  commissionRate: number;
  commissionAmount: number;
  status: string;
  walletFlowNo?: string;
  failReason?: string;
  settledAt?: number;
  createTime?: number;
  updateTime?: number;
}

/** MyBatis-Flex Page 返回结构。 */
export interface PageResult<T> {
  records: T[];
  pageNumber: number;
  pageSize: number;
  totalRow: number;
  totalPage: number;
}

export interface UpdateUserInfoPayload {
  nickname?: string;
  email?: string;
}

export interface VipLevelConfig {
  level: number;
  levelName: string;
  price: number;
  rebateRate: number;
  dailyTasks: number;
  benefits: string;
}

export interface VipLevelOverview {
  currentLevel: number;
  levels: VipLevelConfig[];
}

export interface VipUpgradeOrder {
  orderNo: string;
  fromLevel: number;
  toLevel: number;
  currency: string;
  amount: number;
  status: string;
  walletFlowNo?: string;
  finishedAt?: number;
}

const BASE = '/api/open/user';

export function getCurrentUser(): Promise<UserDetail> {
  return http.get<UserDetail>(`${BASE}/detail`);
}

export function updateCurrentUser(data: UpdateUserInfoPayload): Promise<void> {
  return http.put<void>(`${BASE}/update`, data);
}

export function getTeamMembers(pageNumber = 1, pageSize = 10): Promise<PageResult<TeamMemberRecord>> {
  return http.get<PageResult<TeamMemberRecord>>(`${BASE}/team/members`, { pageNumber, pageSize });
}

export function getTeamEarnings(pageNumber = 1, pageSize = 10): Promise<PageResult<InviteCommissionRecord>> {
  return http.get<PageResult<InviteCommissionRecord>>(`${BASE}/team/earnings`, { pageNumber, pageSize });
}

export function getVipLevelOverview(): Promise<VipLevelOverview> {
  return http.get<VipLevelOverview>(`${BASE}/vip-level`);
}

export function upgradeVipLevel(level: number): Promise<VipUpgradeOrder> {
  return http.post<VipUpgradeOrder>(`${BASE}/vip-level-up`, { level });
}