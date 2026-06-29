import { Message } from '@arco-design/web-vue';
import { InternalAxiosRequestConfig } from 'axios';
// @ts-ignore
import CryptoJS from 'crypto-js';
import StatusItem from '@/model/statusItem';
import { Ref } from 'vue';
// 公共方法

export function copyTextToClipboard(content: string) {
  const clipboardObj = navigator.clipboard;
  if (clipboardObj) {
    clipboardObj.writeText(content);
  } else {
    const textarea = document.createElement('textarea');
    textarea.value = content;
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('Copy');
    document.body.removeChild(textarea);
  }
  Message.success('复制成功');
}

export function listOrJsonIsEmpty(o: any) {
  if (o === 0) {
    return false;
  }

  if (o) {
    const s = JSON.stringify(o);
    if (s !== '{}' && s !== '[]') {
      return false;
    }
  }
  return true;
}

export function encryptAes(config: InternalAxiosRequestConfig, token: string) {
  const key = token.slice(0, 8) + token.slice(-8);
  const iv = key.split('').reverse().join('');
  const aesConfig = {
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7,
    iv: CryptoJS.enc.Utf8.parse(iv),
  };

  const { data } = config;
  if (data) {
    const dataStr = JSON.stringify(data);
    const encrypt = CryptoJS.AES.encrypt(
      CryptoJS.enc.Utf8.parse(dataStr),
      CryptoJS.enc.Utf8.parse(key),
      aesConfig
    );
    config.data = { data: encrypt.toString() };
  }
}

export const formatMoneyWithComma = (amount: number): string => {
  // eslint-disable-next-line no-restricted-globals
  if (amount === null || amount === undefined || isNaN(Number(amount))) {
    return String(amount); // 格式错误返回原数据
  }

  const options = {
    style: 'currency',
    currency: 'CNY',
  };
  return amount.toLocaleString('zh-CN', options);
};
