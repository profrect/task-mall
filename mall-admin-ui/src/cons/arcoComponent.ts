import dayjs from 'dayjs';
import { parseTime } from '@/utils/dateUtils';

export const SHORTCUTS = [
  {
    label: '过去24小时',
    value: () => dayjs().subtract(1, 'day').format('YYYY-MM-DD HH:mm:ss'),
  },
  {
    label: '今天',
    value: () => dayjs().format('YYYY-MM-DD HH:mm:ss'),
  },
  {
    label: '过去7天',
    value: () => dayjs().subtract(1, 'week').format('YYYY-MM-DD HH:mm:ss'),
  },
  {
    label: '过去30天',
    value: () => dayjs().subtract(30, 'days').format('YYYY-MM-DD HH:mm:ss'),
  },
  {
    label: '过去90天',
    value: () => dayjs().subtract(90, 'days').format('YYYY-MM-DD HH:mm:ss'),
  },
  {
    label: '过去365天',
    value: () => dayjs().subtract(1, 'year').format('YYYY-MM-DD HH:mm:ss'),
  },
];

export const RANGE_SHORTCUTS = [
  {
    label: '过去24小时',
    value: () => [
      dayjs().subtract(1, 'day').format('YYYY-MM-DD HH:mm:ss'),
      dayjs().format('YYYY-MM-DD HH:mm:ss'),
    ],
  },
  {
    label: '过去7天',
    value: () => [
      dayjs().subtract(7, 'days').format('YYYY-MM-DD HH:mm:ss'),
      dayjs().format('YYYY-MM-DD HH:mm:ss'),
    ],
  },
  {
    label: '过去30天',
    value: () => [
      dayjs().subtract(30, 'days').format('YYYY-MM-DD HH:mm:ss'),
      dayjs().format('YYYY-MM-DD HH:mm:ss'),
    ],
  },
  {
    label: '过去90天',
    value: () => [
      dayjs().subtract(90, 'days').format('YYYY-MM-DD HH:mm:ss'),
      dayjs().format('YYYY-MM-DD HH:mm:ss'),
    ],
  },
  {
    label: '过去365天',
    value: () => [
      dayjs().subtract(1, 'year').format('YYYY-MM-DD HH:mm:ss'),
      dayjs().format('YYYY-MM-DD HH:mm:ss'),
    ],
  },
];

const today = new Date();
export const timeRangeToday = [
  parseTime(
    new Date(
      today.getFullYear(),
      today.getMonth(),
      today.getDate(),
      0, // 小时：0（零点）
      0, // 分钟：0
      0, // 秒：0
      0 // 毫秒：0
    )
  ),
  parseTime(
    new Date(
      today.getFullYear(),
      today.getMonth(),
      today.getDate() + 1, // 日期 +1 得到第二天
      0,
      0,
      0,
      0
    )
  ),
];

export const timestampRangeToday = [
  new Date(
    today.getFullYear(),
    today.getMonth(),
    today.getDate(),
    0, // 小时：0（零点）
    0, // 分钟：0
    0, // 秒：0
    0 // 毫秒：0
  ).getTime(),
  new Date(
    today.getFullYear(),
    today.getMonth(),
    today.getDate() + 1, // 日期 +1 得到第二天
    0,
    0,
    0,
    0
  ).getTime(),
];

export const timestampRangeYesterday = [
  new Date(
    today.getFullYear(),
    today.getMonth(),
    today.getDate() - 1,
    0, // 小时：0（零点）
    0, // 分钟：0
    0, // 秒：0
    0 // 毫秒：0
  ).getTime(),
  new Date(
    today.getFullYear(),
    today.getMonth(),
    today.getDate(),
    0,
    0,
    0,
    0
  ).getTime(),
];
