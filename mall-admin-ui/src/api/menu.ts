import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';
import requestFile from '@/api/requestFile';

const uri = '/api/sys/manage/menu';

export class MenuPo {
  /** 菜单ID */
  id?: number;

  /** 组件路径 */
  component?: string;

  /** ICON */
  icon?: string;

  /** 菜单名称 */
  menuKey?: string;

  /** 父节点 */
  pno?: number;

  /** 路由名称 */
  name?: string;

  /** 路由相对路径 */
  path?: string;

  /** 接口请求对应路径 */
  uri?: string;

  /** 菜单排序(数字越小排名越靠前) */
  order?: number;

  /** 菜单类型 */
  type?: number;

  /** 菜单是否可见(1:显示;0:隐藏) */
  hideInMenu?: number;

  /** 菜单状态(1:正常) */
  status?: number;

  constructor(data: any) {
    this.id = data.id;
    this.component = data.component;
    this.icon = data.icon;
    this.menuKey = data.menuKey;
    this.pno = data.pno;
    this.name = data.name;
    this.path = data.path;
    this.uri = data.uri;
    this.order = data.order;
    this.type = data.type;
    this.hideInMenu = data.hideInMenu;
    this.status = data.status;
  }

  update(data: MenuInfo) {
    this.id = data.key;
    this.component = data.component;
    this.icon = data.icon;
    this.menuKey = data.menuKey;
    this.pno = data.pno;
    this.name = data.name;
    this.path = data.path;
    this.uri = data.uri;
    this.order = data.order;
    this.type = data.type;
    this.hideInMenu = data.hideInMenu;
  }
}

/** */
export interface MenuInfo {
  /** 子菜单 */
  children?: MenuInfo[];
  /** 组件路径 */
  component?: string;
  /** ICON */
  icon?: string;
  /** 菜单ID */
  key?: number;
  /** 菜单名称 */
  menuKey?: string;
  /** 父节点 */
  pno?: number;
  /** 路由名称 */
  name?: string;
  /** 路由相对路径 */
  path?: string;
  /** 接口请求对应路径 */
  uri?: string;
  /** 菜单排序(数字越小排名越靠前) */
  order?: number;
  /** 菜单 */
  type?: number;
  /** 菜单是否可见(1:显示;0:隐藏) */
  hideInMenu?: number;
}

/** Meta，路由属性 */
export interface Meta {
  /** 【目录】只有一个子路由是否始终显示 */
  alwaysShow?: boolean;
  /** 是否隐藏(true-是 false-否) */
  hideInMenu?: boolean;
  /** ICON */
  icon?: string;
  /** 【菜单】是否开启页面缓存 */
  keepAlive?: boolean;
  /** 路由title */
  menuKey: string;
  /** 顺序 */
  order?: number;
}

/** RouteVO，路由对象 */
export interface RouteVO {
  /** 路由名称 */
  name?: string;
  /** 路由路径 */
  path?: string;
  /** 组件路径 */
  component?: string;
  /** 路由属性 */
  meta?: Meta;
  /** 跳转链接 */
  redirect?: string;
  /** 子路由列表 */
  children: RouteVO[];
}

/**
 * 获取用户角色权限对应的路由
 * @param tag
 */
export function getUserMenuList(): Promise<any> {
  return request({
    url: `${uri}/list`,
    method: 'get',
  });
}

/**
 * 获取总的系统或游戏树形路由列表
 */
export function getMenuTreeList(params: MenuInfo): Promise<Array<MenuInfo>> {
  return request({
    url: `${uri}/tree-list`,
    method: 'get',
    params,
  });
}

/**
 * 获取总的系统以及游戏树形路由列表
 */
export function getTwoMenuTreeList(): Promise<
  ResultInfo<{ systemMenuTreeList: MenuInfo[]; gameMenuTreeList: MenuInfo[] }>
> {
  return request({
    url: `${uri}/all-tree-list`,
    method: 'get',
  });
}

/**
 * 获取角色对应菜单id列表
 */
export function getRoleMenuIdList(
  code: string
): Promise<ResultInfo<{ systemMenuIds: []; gameMenuIds: [] }>> {
  return request({
    url: `${uri}/ids/${code}`,
    method: 'get',
  });
}

export function addMenu(data: MenuPo): Promise<ResultInfo<any>> {
  return request({
    url: `${uri}/add`,
    method: 'post',
    data,
  });
}

export function updateMenu(data: MenuPo): Promise<ResultInfo<any>> {
  return request({
    url: `${uri}/update`,
    method: 'put',
    data,
  });
}

export function deleteMenu(id: number): Promise<ResultInfo<any>> {
  return request({
    url: `${uri}/delete/${id}`,
    method: 'delete',
  });
}
export function uploadSysMenu(data: FormData): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/upload`,
    method: 'post',
    data,
  });
}

export function downloadSysMenu(): Promise<Blob> {
  return requestFile({
    url: `${uri}/download`,
    method: 'get',
  });
}
