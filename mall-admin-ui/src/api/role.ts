// eslint-disable-next-line import/prefer-default-export,import/no-cycle
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';
import { Options } from '@/types/global';
import Page from '@/model/Page';
import PageData from '@/model/pageData';

export class Role {
  id: number;

  roleCode: string;

  roleName: string;

  level: number;

  desc: string;

  createTime: number;

  updateTime: number;

  menuIds: Array<number>;

  apiIds: Array<number>;

  constructor(data: any) {
    this.id = data.id;
    this.roleCode = data.roleCode;
    this.roleName = data.roleName;
    this.level = data.level;
    this.desc = data.desc;
    this.createTime = data.createTime;
    this.updateTime = data.updateTime;
    this.menuIds = data.menuIds;
    this.apiIds = data.apiIds;
  }

  update(data: Role) {
    this.id = data.id;
    this.roleCode = data.roleCode;
    this.roleName = data.roleName;
    this.level = data.level;
    this.desc = data.desc;
    this.createTime = data.createTime;
    this.updateTime = data.updateTime;
    this.menuIds = data.menuIds;
    this.apiIds = data.apiIds;
  }
}

export class RoleSearch extends PageData {
  roleCode: string;

  roleName: string;

  constructor(data: any) {
    super(data.pageNumber, data.pageSize);
    this.roleCode = data.roleCode;
    this.roleName = data.roleName;
  }

  update(data: RoleSearch) {
    this.roleCode = data.roleCode;
    this.roleName = data.roleName;
  }
}

export class RoleAssignIds {
  roleCode: string;

  idList: Array<number>;

  constructor(data: any) {
    this.roleCode = data.roleCode;
    this.idList = data.idList;
  }

  update(data: RoleAssignIds) {
    this.roleCode = data.roleCode;
    this.idList = data.idList;
  }
}

const uri = '/api/sys/manage/role';

export function userRoleOptions(): Promise<ResultInfo<Array<Options>>> {
  return request({
    url: `${uri}/option-list`,
    method: 'get',
  });
}

export function selectRole(data: RoleSearch): Promise<ResultInfo<Page<Role>>> {
  return request({
    url: `${uri}/page-list`,
    method: 'get',
    params: data,
  });
}

export function addUserRole(data: Role): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/add`,
    method: 'post',
    data,
  });
}

export function updateUserRole(data: Role): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/update`,
    method: 'post',
    data,
  });
}

export function deleteUserRole(id: number): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/delete/${id}`,
    method: 'delete',
  });
}

export function assignRoleMenu(data: RoleAssignIds): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/assign-menu`,
    method: 'post',
    data,
  });
}

export function assignRoleApi(data: RoleAssignIds): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/assign-api`,
    method: 'post',
    data,
  });
}
