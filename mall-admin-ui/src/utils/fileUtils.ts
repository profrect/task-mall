// 读取上传文件内容（excel：按列返回数据列表；json：返回数据列表长度1，返回json字符串）
import { Message } from '@arco-design/web-vue';
import * as XLSX from 'xlsx';

export function readUploadFileContentList<T>(
  file: any,
  fileList: Array<any>
): Promise<Array<T>> {
  return new Promise((resolve) => {
    if (typeof FileReader === 'undefined') {
      Message.warning('没有读取到文件，或者您的浏览器不支持文件读取。');
      fileList.splice(0, fileList.length);
      return;
    }

    const { name } = file;
    const reader = new FileReader();

    if (name.endsWith('.xls') || name.endsWith('.xlsx')) {
      reader.readAsBinaryString(file);
      reader.onload = function (e) {
        fileList.splice(0, fileList.length);
        const result = e.target?.result;
        const wb = XLSX.read(result, { type: 'binary' });
        let list = new Array<any>();
        // 循环取每一张表
        // eslint-disable-next-line guard-for-in,no-restricted-syntax
        for (const sheet in wb.Sheets) {
          list = list.concat(XLSX.utils.sheet_to_json(wb.Sheets[sheet]));
        }

        fileList.push(...list);
        resolve(list);
      };
    } else if (name.endsWith('.json')) {
      reader.onload = function (e) {
        fileList.splice(0, fileList.length);
        const result = e.target?.result as string;

        const data = JSON.parse(result);
        if (data instanceof Array) {
          fileList.push(...data);
          resolve(data);
        } else {
          Message.warning('json文件内容格式错误');
        }
      };
      reader.readAsText(file, 'utf-8');
    } else {
      Message.warning('文件格式错误，仅可使用excel或者json文件导入数据');
      fileList.splice(0, fileList.length);
    }
  });
}

// 弹出下载文件框。blob：先加载文件数据BLOB格式，name：下载文件名称
export function fileDown(blob: Blob, name: string) {
  const linkNode = document.createElement('a');
  // a标签的download属性规定下载文件的名称
  linkNode.download = name;
  linkNode.style.display = 'none';
  // 生成一个Blob URL
  linkNode.href = URL.createObjectURL(blob);
  document.body.appendChild(linkNode);
  // 模拟在按钮上的一次鼠标单击
  linkNode.click();
  // 释放URL 对象
  URL.revokeObjectURL(linkNode.href);
  document.body.removeChild(linkNode);
}
