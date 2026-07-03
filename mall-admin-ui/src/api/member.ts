// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';
import { getRequestParamDeal } from '@/utils/apiUtils';
import Page from '@/model/Page';

/** 会员管理视图（对应 mall-admin UserInfoVO）。 */
export interface MemberUser {
  userId: number;

  userName: string;

  nickname?: string;

  email?: string;

  vipLevel: number;

  inviteCode: string;

  status: number;

  parentUserName: string;

  parentUserId?: number;

  groupId?: number;

  groupName?: string;

  registerTime: number;

  availableAmt: number;

  freezeAmt: number;
}

export interface MemberImpersonationTicket {
  ticket: string;

  expiresIn: number;

  expiresAt: number;
}

export interface MemberGroup {
  id: number;

  groupName: string;

  remark?: string;

  status: number;

  sortOrder: number;

  memberCount: number;
}

export interface MemberSavePayload {
  userId?: number;

  userName: string;

  password?: string;

  nickname?: string;

  email?: string;

  vipLevel?: number;

  status?: number;

  parentUserId?: number;

  groupId?: number;
}

export interface MemberLineNode {
  userId: number;

  userName?: string;

  nickname?: string;

  vipLevel: number;

  status: number;

  parentUserId?: number;

  parentUserName?: string;

  relation: string;

  depth: number;
}

export interface MemberLineage {
  current?: MemberLineNode;

  ancestors: MemberLineNode[];

  children: MemberLineNode[];
}

const uri = '/api/admin/user';

export function queryMemberList(
  data: Map<string, string>
): Promise<ResultInfo<Page<MemberUser>>> {
  return request({
    url: getRequestParamDeal(`${uri}/page`, data),
    method: 'get',
  });
}

export function updateMemberStatus(data: {
  userId: number;
  status: number;
}): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/status`,
    method: 'post',
    data,
  });
}

export function queryMemberDetail(userId: number): Promise<ResultInfo<MemberUser>> {
  return request({
    url: `${uri}/detail/${userId}`,
    method: 'get',
  });
}

export function saveMember(data: MemberSavePayload): Promise<ResultInfo<MemberUser>> {
  return request({
    url: `${uri}/save`,
    method: 'post',
    data,
  });
}

export function exportMemberList(
  data: Map<string, string>
): Promise<ResultInfo<MemberUser[]>> {
  return request({
    url: getRequestParamDeal(`${uri}/export`, data),
    method: 'get',
  });
}

export function queryMemberGroups(): Promise<ResultInfo<MemberGroup[]>> {
  return request({
    url: `${uri}/group/list?status=1`,
    method: 'get',
  });
}

export function saveMemberGroup(data: {
  id?: number;
  groupName: string;
  remark?: string;
  status?: number;
  sortOrder?: number;
}): Promise<ResultInfo<MemberGroup>> {
  return request({
    url: `${uri}/group/save`,
    method: 'post',
    data,
  });
}

export function assignMemberGroup(data: {
  userIds: number[];
  groupId?: number;
}): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/group/assign`,
    method: 'post',
    data,
  });
}

export function queryMemberLineage(userId: number): Promise<ResultInfo<MemberLineage>> {
  return request({
    url: `${uri}/line/${userId}`,
    method: 'get',
  });
}

export function changeMemberParent(data: {
  userId: number;
  parentUserId: number;
}): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/line/change`,
    method: 'post',
    data,
  });
}

export function updateMemberStatusBatch(data: {
  userIds: number[];
  status: number;
}): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/batch-status`,
    method: 'post',
    data,
  });
}

export function logoutMember(data: { id: number }): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/logout`,
    method: 'post',
    data,
  });
}

export function createMemberImpersonationTicket(data: {
  id: number;
}): Promise<ResultInfo<MemberImpersonationTicket>> {
  return request({
    url: `${uri}/impersonation-ticket`,
    method: 'post',
    data,
  });
}