// eslint-disable-next-line import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';
import { getRequestParamDeal } from '@/utils/apiUtils';
import Page from '@/model/Page';

const userUrlPrefix = '/api/sys/manage/user';
const loginUrlPrefix = '/api/sys/admin';
export interface LoginData {
  name: string;
  password: string;
}

export const login = async (data: LoginData): Promise<ResultInfo<User>> => {
  return request({
    url: `${loginUrlPrefix}/login`,
    method: 'post',
    data,
  });
};

export function logoutUser(): Promise<ResultInfo<null>> {
  return request({
    url: `${loginUrlPrefix}/logout`,
    method: 'post',
  });
}

export function getUserInfo(): Promise<ResultInfo<User>> {
  return request({
    url: `${userUrlPrefix}/user-detail`,
    method: 'get',
  });
}

export class User {
  username: string;

  realName: string;

  email: string;

  id: number;

  accessToken: string;

  roles: string;

  level: number;

  constructor(data: any) {
    this.username = data.username;
    this.realName = data.realName;
    this.email = data.email;
    this.id = data.id;
    this.accessToken = data.accessToken;
    this.roles = data.roles;
    this.level = data.level;
  }

  updateUserInfo(data: User) {
    this.username = data.username;
    this.realName = data.realName;
    this.email = data.email;
    this.id = data.id;
    this.accessToken = data.accessToken;
    this.roles = data.roles;
    this.level = data.level;
  }
}

export class UserFull extends User {
  roleCodeList: Array<string>;

  creator: string;

  createTime: number;

  updater: string;

  updateTime: number;

  password: string;

  current?: number;

  pageSize?: number;

  constructor(data: any) {
    super(data);
    this.roleCodeList = data.roleCodeList;
    this.creator = data.creator;
    this.createTime = data.createTime;
    this.updater = data.updater;
    this.updateTime = data.updateTime;
    this.password = data.password;
    this.current = data.current;
    this.pageSize = data.pageSize;
  }

  updateUser(data: any) {
    this.username = data.username;
    this.realName = data.realName;
    this.email = data.email;
    this.id = data.id;
    this.accessToken = data.accessToken;
    this.roles = data.roles;
    this.level = data.level;
    this.roleCodeList = data.roleCodeList;
    this.creator = data.creator;
    this.createTime = data.createTime;
    this.updater = data.updater;
    this.updateTime = data.updateTime;
    this.password = data.password;
    this.current = data.current;
    this.pageSize = data.pageSize;
  }
}

export function queryUserInfoList(
  data: Map<string, string>
): Promise<ResultInfo<Page<UserFull>>> {
  return request({
    url: getRequestParamDeal(`${userUrlPrefix}/page-list`, data),
    method: 'get',
  });
}

export function addNewUser(data: UserFull): Promise<ResultInfo<null>> {
  return request({
    url: `${userUrlPrefix}/add`,
    method: 'post',
    data,
  });
}

export function deleteUserById(id: number): Promise<ResultInfo<null>> {
  return request({
    url: `${userUrlPrefix}/delete/${id}`,
    method: 'DELETE',
  });
}

export function updateUser(data: UserFull): Promise<ResultInfo<null>> {
  return request({
    url: `${userUrlPrefix}/update`,
    method: 'post',
    data,
  });
}

export function updatePassword(data: {
  username: string;
  newPswd: string;
  oldPswd: string;
}): Promise<ResultInfo<null>> {
  return request({
    url: `${userUrlPrefix}/update-pswd`,
    method: 'post',
    data,
  });
}

export function resetUserPasswd(name: string): Promise<ResultInfo<null>> {
  return request({
    url: `${userUrlPrefix}/reset-pswd/${name}`,
    method: 'post',
  });
}
