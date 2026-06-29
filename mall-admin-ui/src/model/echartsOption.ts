// Echarts的 图表 Option
export class EchartsOption {
  // x轴
  xAxis: EchartsOptionXAxis;

  // y轴
  yAxis: EchartsOptionYAxis;

  // 数据
  series: Array<EchartsOptionSeries>;

  // 图表标题
  title?: EchartsOptionTitle;

  // 图例组件
  legend?: EchartsOptionLegend;

  // 提示框组件
  tooltip?: EchartsOptionTooltip;

  // 网格设置
  grid?: EchartsOptionGrid;

  constructor(data: any) {
    data = data || {};
    this.xAxis = data.xAxis;
    this.yAxis = data.yAxis;
    this.series = data.series;
    this.title = data.title;
    this.legend = data.legend;
    this.tooltip = data.tooltip;
    this.grid = data.grid;
  }

  update(data: EchartsOption) {
    this.xAxis = data.xAxis;
    this.yAxis = data.yAxis;
    this.series = data.series;
    this.title = data.title;
    this.legend = data.legend;
    this.tooltip = data.tooltip;
    this.grid = data.grid;
  }

  clearOptionData() {
    this.xAxis.data.splice(0);
    this.series.splice(0);
    this.legend?.data?.splice(0);
  }
}

// 图表标题
export class EchartsOptionTitle {
  // 主标题文本
  text: string;

  // 是否显示标题组件
  show: boolean;

  // 主标题文本超链接
  link: string;

  constructor(text: string, show: boolean, link: string) {
    this.text = text;
    this.show = show;
    this.link = link;
  }
}

// x轴数据类
export class EchartsOptionXAxis {
  // 坐标轴类型, EchartsOptionAxisTypeEnum
  type: string;

  // 坐标轴两边留白策略，类目轴和非类目轴的设置和表现不一样。
  // 类目轴中 boundaryGap 可以配置为 true 和 false。默认为 true，这时候刻度只是作为分隔线，标签和数据点都会在两个刻度之间的带(band)中间
  boundaryGap: boolean;

  // 类目数据，在类目轴（type: 'category'）中有效。
  data: Array<string>;

  constructor(type: string, boundaryGap: boolean, data: Array<string>) {
    this.type = type;
    this.boundaryGap = boundaryGap;
    this.data = data;
  }
}

// Y轴数据类
export class EchartsOptionYAxis {
  // 坐标轴类型, EchartsOptionAxisTypeEnum
  type: string;

  // 坐标轴名称
  name?: string;

  // 坐标轴刻度最大值
  max?: number;

  // 坐标轴刻度标签的相关设置
  axisLabel?: EchartsOptionYAxisAxisLabel;

  constructor(
    type: string,
    name?: string,
    max?: number,
    axisLabel?: EchartsOptionYAxisAxisLabel
  ) {
    this.type = type;
    this.name = name;
    this.max = max;
    this.axisLabel = axisLabel;
  }
}

export class EchartsOptionYAxisAxisLabel {
  // 是否显示刻度标签
  show: boolean;

  // 坐标轴刻度标签的显示间隔，在类目轴中有效
  interval: 'auto';

  // 刻度标签的内容格式器，支持字符串模板和回调函数两种形式，例子：{value} %
  formatter?: string;

  constructor(show: boolean, interval: 'auto', formatter?: string) {
    this.show = show;
    this.interval = interval;
    this.formatter = formatter;
  }
}

// series
export class EchartsOptionSeries {
  // 类型, EchartsOptionSeriesTypeEnum
  type: string;

  // 名称，用于tooltip的显示，legend 的图例筛选，在 setOption 更新数据和配置项时用于指定对应的系列。
  name?: string;

  // 数据
  data: Array<number>;

  constructor(type: string, data: Array<number>, name?: string) {
    this.type = type;
    this.data = data;
    if (name) this.name = name;
  }
}

// 图例组件展现了不同系列的标记(symbol)，颜色和名字。可以通过点击图例控制哪些系列不显示
export class EchartsOptionLegend {
  // 图例数据展示名称列表
  data: Array<string>;

  // 内容格式器，支持字符串模板和回调函数两种形式，例子：{value} %
  formatter?: string;

  constructor(data: Array<string>, formatter?: string) {
    this.data = data;
    this.formatter = formatter;
  }
}

// 提示框组件。
export class EchartsOptionTooltip {
  // 触发方式, EchartsOptionTriggerEnum
  trigger: string;

  valueFormatter?: any;

  constructor(trigger: string, valueFormatter?: any) {
    this.trigger = trigger;
    this.valueFormatter = valueFormatter;
  }
}

export class EchartsOptionGrid {
  // 离容器左侧的距离
  left: string;

  // 离容器上侧的距离
  top: string;

  // 离容器右侧的距离
  right: string;

  // 离容器下侧的距离
  bottom: string;

  constructor(left: string, top: string, right: string, bottom: string) {
    this.left = left;
    this.top = top;
    this.right = right;
    this.bottom = bottom;
  }
}

// echarts 数据轴类型
export enum EchartsOptionAxisTypeEnum {
  // 数值轴，适用于连续数据。
  VALUE = 'value',

  // 类目轴，适用于离散的类目数据。为该类型时类目数据可自动从 series.data 或 dataset.source 中取，或者可通过 xAxis.data 设置类目数据。
  CATEGORY = 'category',

  // 时间轴，适用于连续的时序数据，与数值轴相比时间轴带有时间的格式化，在刻度计算上也有所不同，例如会根据跨度的范围来决定使用月，星期，日还是小时范围的刻度。
  TIME = 'time',

  // 对数轴。适用于对数数据。
  LOG = 'log',
}

export enum EchartsOptionSeriesTypeEnum {
  // 折线图
  LINE = 'line',
}

export enum EchartsOptionTriggerEnum {
  // 坐标轴触发，主要在柱状图，折线图等会使用类目轴的图表中使用。
  AXIS = 'axis',

  // 数据项图形触发，主要在散点图，饼图等无类目轴的图表中使用。
  ITEM = 'item',

  // 什么都不触发。
  NONE = 'none',
}
