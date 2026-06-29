import { getRequestParamDeal } from '@/utils/apiUtils';
import ResultPageData from '@/model/resultPageData';
import { ResultInfo } from '@/model/resultInfo';
import request from '@/api/request';
import PageData from '@/model/pageData';

export class UserLog extends PageData {
  userId: number;

  /**
   * 操作结果类型
   */
  type?: number;

  /**
   * 操作内容
   */
  content: string;

  /**
   * 操作结果(0：失败。1：成功)
   */
  result: number;

  /**
   * 操作ip
   */
  ip: string;

  /**
   * 模糊查询gm日志用字段
   */
  vCmd?: string;

  resultData: UserLogContent;

  createTimeLong: number;

  constructor(data: any) {
    super();
    this.userId = data.userId;
    this.result = data.result;
    this.content = data.content;
    this.ip = data.ip;
    this.type = data.type;
    this.vCmd = data.vCmd;
    this.resultData = data.resultData;
    this.createTimeLong = data.createTimeLong;
  }
}

export class UserLogType {
  type: number;

  message: string;

  constructor(data: any) {
    this.type = data.type;
    this.message = data.message;
  }
}

export class UserLogContent {
  userId: number;

  userName: string;

  cmd: string;

  result: Array<UserLogContentResult>;

  constructor(data: any) {
    this.userId = data.userId;
    this.userName = data.userName;
    this.cmd = data.cmd;
    this.result = data.result;
  }

  update(data: any) {
    this.userId = data.userId;
    this.userName = data.userName;
    this.cmd = data.cmd;
    this.result = data.result;
  }
}

export class UserLogContentResult {
  serverId: string;

  serverName: string;

  success: number;

  result: any;

  constructor(data: any) {
    this.serverId = data.serverId;
    this.serverName = data.serverName;
    this.success = data.success;
    this.result = data.result;
  }
}

const userLogUrlPrefix = '/userLog/';

export function searchUserLog(
  data: Map<string, any>
): Promise<ResultInfo<ResultPageData<UserLog>>> {
  return request({
    url: getRequestParamDeal(`${userLogUrlPrefix}getNewUserLog.json`, data),
    method: 'get',
  });
}

export function searchUserLogType(): Promise<ResultInfo<Array<UserLogType>>> {
  return request({
    url: `${userLogUrlPrefix}getUserLogType.json`,
    method: 'get',
  });
}
