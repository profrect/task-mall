/*
 * 验证类函数工具
 */

// 判断是否是外部链接
export function isExternalLink(path: string): boolean {
  return /^(https?:|mailto:|tel:)/.test(path);
}

// 用户名检查
export function validUsername(v: string): string {
  if (v === '') {
    return '用户名格式错误';
  }

  if (v.length < 1) {
    return '请输入用户名';
  }

  const reg = '^[ ]+$';
  const re = new RegExp(reg);
  const b = re.test(v);
  return b ? '名称不能有空格' : '';
}

// 密码检查
export function validPassword(v: string): string {
  if (v !== '' && v.length >= 6) {
    return '';
  }
  return '密码至少为6位';
}

// 手机号格式校验函数（匹配中国大陆手机号规则：1开头，11位数字）
export const phoneCheckValid = (value: string, cb: any) => {
  // 手机号正则：1开头，第二位为3-9，后面跟9位数字
  const phoneReg = /^1[3-9]\d{9}$/;
  if (!value) {
    cb('请输入手机号'); // 空值提示
  } else if (value.includes(' ')) {
    cb('请检查是否有空格');
  } else if (!phoneReg.test(value)) {
    cb('请输入正确的中国大陆手机号'); // 格式错误提示
  } else {
    cb(); // 校验通过，无参数调用回调
  }
};

// 通用邮箱格式校验函数
export const emailCheckValid = (value: string, cb: any) => {
  // 通用邮箱正则：支持字母、数字、下划线、中划线，域名支持多级（如xxx@xxx.com.cn）
  const emailReg = /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/;
  if (!value) {
    cb('请输入邮箱地址'); // 空值提示
  } else if (value.includes(' ')) {
    cb('请检查是否有空格');
  } else if (!emailReg.test(value)) {
    cb('请输入正确的邮箱格式（如：xxx@xxx.com）'); // 格式错误提示
  } else {
    cb(); // 校验通过，无参数调用回调
  }
};
