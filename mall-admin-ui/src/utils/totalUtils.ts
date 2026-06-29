const hour = 60 * 60 * 1000;

const day = 60 * 60 * 1000 * 24;

export function getTimeScopeHourTimeList(
  beginTime: number,
  endTime: number
): Array<number> {
  const timeList = new Array<number>();
  while (beginTime < endTime) {
    timeList.push(beginTime);
    beginTime += hour;
  }
  return timeList;
}

export function getTimeScopeDayTimeList(
  beginTime: number,
  endTime: number
): Array<number> {
  const timeList = new Array<number>();
  while (beginTime < endTime) {
    timeList.push(beginTime);
    beginTime += day;
  }
  return timeList;
}

export function getTimeScopeMonthTimeList(
  beginTime: number,
  endTime: number
): Array<number> {
  const begin = new Date(beginTime);

  const timeList = new Array<number>();
  while (begin.getTime() < endTime) {
    timeList.push(begin.getTime());
    begin.setMonth(begin.getMonth() + 1);
  }
  return timeList;
}
