// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';

export interface BotConfig {
  id?: number;

  botName: string;

  botToken?: string;

  maskedToken?: string;

  webhookUrl: string;

  description: string;

  sortOrder: number;

  status: number;

  createTime?: number;

  updateTime?: number;
}

export interface BotConfigQuery {
  keyword?: string;

  status?: number;
}

export interface BotAutoReply {
  id?: number;

  botId: number;

  keyword: string;

  replyContent: string;

  matchType: number;

  sortOrder: number;

  status: number;

  createTime?: number;

  updateTime?: number;
}

export interface BotAutoReplyQuery {
  botId?: number;

  keyword?: string;

  status?: number;
}

const uri = '/api/admin/bot';

export function botConfigList(
  query: BotConfigQuery
): Promise<ResultInfo<Array<BotConfig>>> {
  return request({
    url: `${uri}/config/list`,
    method: 'get',
    params: query,
  });
}

export function saveBotConfig(
  data: BotConfig
): Promise<ResultInfo<BotConfig>> {
  return request({
    url: `${uri}/config`,
    method: data.id ? 'put' : 'post',
    data,
  });
}

export function deleteBotConfig(id: number): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/config/${id}`,
    method: 'delete',
  });
}

export function botReplyList(
  query: BotAutoReplyQuery
): Promise<ResultInfo<Array<BotAutoReply>>> {
  return request({
    url: `${uri}/reply/list`,
    method: 'get',
    params: query,
  });
}

export function saveBotReply(
  data: BotAutoReply
): Promise<ResultInfo<BotAutoReply>> {
  return request({
    url: `${uri}/reply`,
    method: data.id ? 'put' : 'post',
    data,
  });
}

export function deleteBotReply(id: number): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/reply/${id}`,
    method: 'delete',
  });
}