// 日期格式化枚举
const enum DateFormatType {
  // 年月日 时分秒
  YMDHMS = '{y}-{m}-{d} {h}:{i}:{s}',
  YMDHMS2 = '{y}{m}{d}{h}{i}{s}',
  // 年月日 时
  YMDH = '{y}-{m}-{d} {h}',
  // 年月日
  YMD = '{y}-{m}-{d}',
  // 年月
  YM = '{y}-{m}',
  // 时分
  HM = '{h}:{i}',
  // 年月日 周几
  YMDW = '{y}-{m}-{d} {w}',
}

// 时间转换（默认格式DateFormat.YMDHMS）
export function parseTime(time: any, cFormat?: string): string {
  if (!time) {
    return '';
  }
  const format = cFormat || DateFormatType.YMDHMS;
  let date;
  if (typeof time === 'object') {
    date = time;
  } else {
    if (typeof time === 'string') {
      if (/^[0-9]+$/.test(time)) {
        time = parseInt(time, 10);
      } else {
        time = time.replace(new RegExp(/-/gm), '/');
      }
    } else if (typeof time === 'number' && time.toString().length === 10) {
      time *= 1000;
    }
    date = new Date(time);
  }
  const formatObj = new Map<string, number>([
    ['y', date.getFullYear()],
    ['m', date.getMonth() + 1],
    ['d', date.getDate()],
    ['h', date.getHours()],
    ['i', date.getMinutes()],
    ['s', date.getSeconds()],
    ['w', date.getDay()],
  ]);
  return format.replace(/{([ymdhisw])+}/g, (result: any, key: string) => {
    const value = formatObj.get(key) || 0;
    if (key === 'w') {
      return `周${['日', '一', '二', '三', '四', '五', '六'][value]}`;
    }
    return value.toString().padStart(2, '0');
  });
}

// 时间戳的日期变化计算（num：变化天数；type：变化类型；time：待处理时间（默认值当前时间，类型时间戳或Date））
// type(min, hour, day, week, month, year)
// 正数 增加
export function timeAdd(num: number, typeUnit: number, time: number | Date) {
  let timeLong;
  if (time) {
    timeLong = time instanceof Date ? time.getTime() : time;
  } else {
    timeLong = new Date().getTime();
  }

  timeLong += typeUnit * num;
  return timeLong;
}

export enum DateChangeType {
  MIN = 1000 * 60,
  HOUR = 1000 * 60 * 60,
  DAY = 1000 * 60 * 60 * 24,
  WEEK = 1000 * 60 * 60 * 24 * 7,
  MONTH = 1000 * 60 * 60 * 24 * 30,
  YEAR = 1000 * 60 * 60 * 24 * 365,
}

export enum DateFormat {
  // 年月日 时分秒
  YMDHMS = '{y}-{m}-{d} {h}:{i}:{s}',
  // 年月日 时
  YMDH = '{y}-{m}-{d} {h}',
  // 年月日
  YMD = '{y}-{m}-{d}',
  // 年月
  YM = '{y}-{m}',
  // 时分
  HM = '{h}:{i}',
  // 年月日 周几
  YMDW = '{y}-{m}-{d} {w}',
}
