import { http } from './http';

export interface MissionTaskStats {
  completedCount: number;
  inProgressCount: number;
  totalEarnings: number;
}

export interface MissionTaskItem {
  id: number;
  taskId: number;
  recordId?: number;
  taskCode: string;
  title: string;
  description?: string;
  taskType: string;
  currency: string;
  rewardAmount: number;
  requiredVipLevel: number;
  userStatus?: string;
  submitContent?: string;
  reviewRemark?: string;
  claimedAt?: number;
  submittedAt?: number;
  reviewedAt?: number;
}

export interface MissionUserTaskRecord {
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
}

export interface MissionInvestProject {
  id: number;
  goodsCode: string;
  title: string;
  description?: string;
  currency: string;
  minAmount: number;
  maxAmount: number;
  displayRate: number;
  cycleDays: number;
  riskLevel: string;
}

const BASE = '/api/open/mission';

export function getMissionStats(): Promise<MissionTaskStats> {
  return http.get<MissionTaskStats>(`${BASE}/stats`);
}

export function getMissionTasks(status: string, limit = 50): Promise<MissionTaskItem[]> {
  return http.get<MissionTaskItem[]>(`${BASE}/tasks`, { status, limit });
}

export function claimMissionTask(taskId: number): Promise<MissionUserTaskRecord> {
  return http.post<MissionUserTaskRecord>(`${BASE}/tasks/${taskId}/claim`);
}

export function submitMissionTask(recordId: number, submitContent: string): Promise<MissionUserTaskRecord> {
  return http.post<MissionUserTaskRecord>(`${BASE}/tasks/submit`, { recordId, submitContent });
}

export function getMissionRecords(status?: string, limit = 50): Promise<MissionUserTaskRecord[]> {
  return http.get<MissionUserTaskRecord[]>(`${BASE}/records`, { status, limit });
}

export function getInvestProjects(limit = 50): Promise<MissionInvestProject[]> {
  return http.get<MissionInvestProject[]>(`${BASE}/invest/projects`, { limit });
}