import * as XLSX from 'xlsx';
import { Message } from '@arco-design/web-vue';
import { DateFormat, parseTime } from '@/utils/dateUtils';

export function exportCsv(data: any[][], fileName: string) {
  try {
    // 将二维数组转为工作表
    const worksheet = XLSX.utils.aoa_to_sheet(data);
    // 将工作表转为CSV格式  UTF-8 BOM，解决Excel打开CSV中文乱码
    const csvContent = XLSX.utils.sheet_to_csv(worksheet);

    // 创建Blob并触发下载
    const blob = new Blob([csvContent], { type: 'text/csv;charset=UTF-8' });
    const downloadUrl = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = `${fileName}.csv`;
    link.click();

    // 释放URL对象，避免内存泄漏
    URL.revokeObjectURL(downloadUrl);
    Message.success('CSV导出成功！');
  } catch (error) {
    Message.warning('CSV导出失败！');
    console.error('CSV导出失败：', error);
  }
}

export function exportExcel(
  exportData: Record<string, any>[],
  enTitles: string[],
  zhTitles: string[],
  fileName: string
) {
  try {
    const ws = XLSX.utils.json_to_sheet(exportData, {
      header: enTitles, // 按显示列的key排序
      skipHeader: false, // 跳过默认的key表头，手动设置
    });
    // 手动设置Excel表头（替换为中文标题）
    zhTitles.forEach((title: string, index: number) => {
      const cell = XLSX.utils.encode_cell({ r: 0, c: index });
      ws[cell] = { v: title };
    });

    // 生成Excel文件并下载
    const wb = XLSX.utils.book_new();
    const currentDate = parseTime(new Date().getTime(), DateFormat.YMD);
    XLSX.utils.book_append_sheet(wb, ws, fileName);
    XLSX.writeFile(wb, `${fileName}_${currentDate}.xlsx`);

    Message.success('导出成功！');
  } catch (error) {
    console.error('导出失败：', error);
    Message.error('导出失败，请重试！');
  }
}
