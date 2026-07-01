// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';
import { getRequestParamDeal } from '@/utils/apiUtils';
import Page from '@/model/Page';

/** 会员管理视图（对应 mall-admin UserInfoVO）。 */
export interface MemberUser {
  userId: number;

  userName: string;

  vipLevel: number;

  inviteCode: string;

  status: number;

  parentUserName: string;

  registerTime: number;

  availableAmt: number;

  freezeAmt: number;
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