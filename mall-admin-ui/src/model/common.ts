/**
 * 组件数据源
 */
export interface OptionTree {
  /** 值 */
  key: string | number;
  /** 文本 */
  title: string;
  /** 子列表  */
  children?: OptionTree[];
}

// 枚举通用字段
export class EnumItem {
  code: number;

  name: string;

  constructor(code: number, name: string) {
    this.code = code;
    this.name = name;
  }
}

export class CardData {
  name: string;

  time: string;

  data: string | number;

  lastData: string | number;

  lastYearData: string | number;

  // 环比
  MOM?: string;

  // 同比
  YOY?: string;

  loading: boolean;

  constructor(data: any) {
    this.name = data.name;
    this.time = data.time;
    this.data = data.data;
    this.lastData = data.lastData;
    this.lastYearData = data.lastYearData;
    this.MOM = data.MOM || '无';
    this.YOY = data.YOY || '无';
    this.loading = data.loading;
  }

  update(data: any) {
    this.name = data.name;
    this.time = data.time;
    this.data = data.data;
    this.lastData = data.lastData;
    this.lastYearData = data.lastYearData;
    this.MOM = data.MOM || '无';
    this.YOY = data.YOY || '无';
    this.loading = data.loading;
  }

  addData(addData: number) {
    if (typeof this.data === 'number') {
      this.data = addData + this.data;
    }
  }
}

export class CardDataItem {
  name: string;

  key: string;

  constructor(name: string, key: string) {
    this.name = name;
    this.key = key;
  }
}

export class CodeData {
  code: number;

  name: string;

  constructor(code: number, name: string) {
    this.code = code;
    this.name = name;
  }

  update(data: CodeData) {
    this.code = data.code;
    this.name = data.name;
  }
}

export class ContrasData {
  name: string;

  oldValue: any;

  newValue: any;

  constructor(name: string, oldValue: any, newValue: any) {
    this.name = name;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }
}

export class CommonVariable {
  id: number;

  name: string;

  description: string;

  constructor(data: any) {
    this.id = data.id;
    this.name = data.name;
    this.description = data.description;
  }
}

export class Options<T> {
  label: string;

  value: T;

  constructor(data: any) {
    this.label = data.label;
    this.value = data.value;
  }
}

export function getLabelByValue(
  list: Array<Options<any>>,
  value: string
): string {
  const item = list.find((option) => option.value === value);
  return item ? item.label : '';
}
