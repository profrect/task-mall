export interface AnyObject {
  [key: string]: unknown;
}

export interface Options {
  value: any;
  label: any;
  children?: Options[];
}

export interface NodeOptions extends Options {
  children?: NodeOptions[];
}

export interface GetParams {
  body: null;
  type: string;
  url: string;
}

export interface GeneralChart {
  xAxis: string[];
  data: Array<{ name: string; value: number[] }>;
}
