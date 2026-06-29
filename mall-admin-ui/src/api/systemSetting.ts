// eslint-disable-next-line import/prefer-default-export
export class SystemSetting {
  key: string;

  name: string;

  value: string | number;

  type: string;

  info?: string;

  toolTip?: string;

  constructor(data: any) {
    this.key = data.key;
    this.name = data.name;
    this.value = data.value;
    this.type = data.type;
    this.info = data.info;
    this.toolTip = data.toolTip;
  }
}
