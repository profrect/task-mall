// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';

/** VIP 等级配置（对应 mall-user VipLevelConfigResp）。 */
export interface VipLevelConfig {
  id?: number;

  level: number;

  levelName: string;

  price: number;

  rebateRate: number;

  dailyTasks: number;

  benefits: string;

  sortOrder: number;

  status: number;

  createTime?: number;

  updateTime?: number;
}

export type VipLevelConfigPayload = Omit<VipLevelConfig, 'createTime' | 'updateTime'>;

const uri = '/api/admin/vip';

export function vipConfigList(params?: {
  status?: number;
}): Promise<ResultInfo<Array<VipLevelConfig>>> {
  return request({
    url: `${uri}/config/list`,
    method: 'get',
    params: {
      ...(params?.status !== undefined ? { status: params.status } : {}),
    },
  });
}

export function saveVipConfig(
  data: VipLevelConfigPayload
): Promise<ResultInfo<VipLevelConfig>> {
  return request({
    url: `${uri}/config`,
    method: data.id ? 'put' : 'post',
    data,
  });
}

export function deleteVipConfig(id: number): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/config/${id}`,
    method: 'delete',
  });
}