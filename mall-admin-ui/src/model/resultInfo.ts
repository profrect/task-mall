// eslint-disable-next-line import/prefer-default-export
export class ResultInfo<T> {
  data: T;

  code: number;

  msg: string;

  success: boolean;

  constructor(rep: any) {
    this.data = rep.data;
    this.code = rep.code;
    this.msg = rep.msg;
    this.success = rep.success;
  }
}
