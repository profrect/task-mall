import PageData from '@/model/pageData';
import request from '@/api/request';
import { ResultInfo } from '@/model/resultInfo';
import Page from '@/model/Page';
import requestFile from '@/api/requestFile';

export class ServerApi {
  id: number;

  apiName: string;

  apiUrl: string;

  method: string;

  status: number;

  type: number;

  remark: string;

  createTime: number;

  updateTime: number;

  constructor(data: any) {
    this.id = data.id;
    this.apiName = data.apiName;
    this.apiUrl = data.apiUrl;
    this.method = data.method;
    this.status = data.status;
    this.type = data.type;
    this.remark = data.remark;
    this.createTime = data.createTime;
    this.updateTime = data.updateTime;
  }

  update(data: ServerApi) {
    this.id = data.id;
    this.apiName = data.apiName;
    this.apiUrl = data.apiUrl;
    this.method = data.method;
    this.status = data.status;
    this.type = data.type;
    this.remark = data.remark;
    this.createTime = data.createTime;
    this.updateTime = data.updateTime;
  }
}

export class ApiSearch extends PageData {
  apiName: string;

  apiUrl: string;

  status: number;

  type: number;

  constructor(data: any) {
    super(data.pageNumber, data.pageSize);
    this.apiName = data.apiName;
    this.apiUrl = data.apiUrl;
    this.status = data.status;
    this.type = data.type;
  }

  update(data: ApiSearch) {
    this.apiName = data.apiName;
    this.apiUrl = data.apiUrl;
    this.status = data.status;
    this.type = data.type;
  }
}

export class ApiTypeList {
  typeName: string;

  apis: Array<ServerApi>;

  constructor(data: any) {
    this.typeName = data.typeName;
    this.apis = data.apis;
  }

  update(data: ApiTypeList) {
    this.typeName = data.typeName;
    this.apis = data.apis;
  }
}

const uri = '/api/manage/server-api';

export function selectApi(
  data: ApiSearch
): Promise<ResultInfo<Page<ServerApi>>> {
  return request({
    url: `${uri}/page-list`,
    method: 'get',
    params: data,
  });
}
export function addApi(data: ServerApi): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/add`,
    method: 'post',
    data,
  });
}

export function updateApi(data: ServerApi): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/update`,
    method: 'post',
    data,
  });
}

export function deleteApi(id: number): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/delete/${id}`,
    method: 'delete',
  });
}

export function allApisByType(): Promise<ResultInfo<Array<ApiTypeList>>> {
  return request({
    url: `${uri}/getAllTypeApis`,
    method: 'get',
  });
}

export function allApiType(): Promise<ResultInfo<Map<number, string>>> {
  return request({
    url: `${uri}/getAllType`,
    method: 'get',
  });
}

export function uploadServerApi(data: FormData): Promise<ResultInfo<null>> {
  return request({
    url: `${uri}/upload`,
    method: 'post',
    data,
  });
}

export function downloadServerApi(): Promise<Blob> {
  return requestFile({
    url: `${uri}/download`,
    method: 'get',
  });
}
