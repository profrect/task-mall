<template>
  <div class="digital_scroll">
    <div
      v-for="index in orderNum"
      :key="index"
      class="digital_scroll_number_item"
      :style="getDigitalStyle"
    >
      <span>
        <i ref="numberItem" :style="getDigitalTopStyle">0123456789</i>
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { onMounted, onUpdated, reactive, ref } from 'vue';

  const props = defineProps<{
    value: number | string;
    time: number;
    itemWidth?: string;
    itemHeight?: string;
    itemTop?: string;
  }>();

  const numberItem = ref();

  const orderNum = reactive(new Array<string>());

  let timeInterval: NodeJS.Timeout;

  let timeIntervalRunNum = 0;

  const newNumber = ref(0);

  onMounted(async () => {
    await toOrderNum(String(props.value));
    await increaseNumber();
  });

  onUpdated(() => {
    clearInterval(timeInterval);
    timeIntervalRunNum = 0;
  });

  // 处理传过来的具体值value
  function toOrderNum(num: string) {
    orderNum.splice(0);
    orderNum.push(...num.split(''));
  }

  // 定时增长数字
  function increaseNumber() {
    timeInterval = setInterval(() => {
      newNumber.value += getRandomNumber(1, 100);
      setNumberTransform();
    }, props.time * 1000);
  }

  // 设置文字滚动
  function setNumberTransform() {
    // 结合CSS 对数字字符进行滚动,显示订单数量
    if (!numberItem.value) {
      clearInterval(timeInterval);
      return;
    }

    // eslint-disable-next-line no-plusplus
    for (let index = 0; index < numberItem.value.length; index++) {
      const elem = numberItem.value[index];
      elem.style.transform = `translate(-50%, -${
        Number(orderNum[index]) * 10
      }%)`;
    }

    if (timeIntervalRunNum > 8) {
      clearInterval(timeInterval);
    }
    // eslint-disable-next-line no-plusplus
    timeIntervalRunNum++;
  }

  function getRandomNumber(min: number, max: number) {
    return Math.floor(Math.random() * (max - min + 1) + min);
  }

  function getDigitalStyle() {
    if (props.itemWidth && props.itemHeight) {
      return `width: ${props.itemWidth}; height: ${props.itemHeight}`;
    }
    return null;
  }

  function getDigitalTopStyle() {
    if (props.itemTop) {
      return `top: ${props.itemTop}`;
    }
    return null;
  }
</script>

<style scoped lang="scss">
  /*具体值value总量滚动数字设置*/
  .digital_scroll {
    position: relative;
    text-align: center;
    list-style: none;
    writing-mode: vertical-lr;
    text-orientation: upright;
    /*文字禁止编辑*/
    -moz-user-select: none; /*火狐*/
    -webkit-user-select: none; /*webkit浏览器*/
    -ms-user-select: none; /*IE10*/
    user-select: none;
    overflow: hidden;

    /*滚动数字设置*/
    .digital_scroll_number_item {
      width: 30px;
      height: 40px;
      list-style: none;

      & > span {
        position: relative;
        display: inline-block;
        margin-right: 5px;
        width: 100%;
        height: 100%;
        writing-mode: vertical-rl;
        text-orientation: upright;
        overflow: hidden;

        & > i {
          font-style: normal;
          position: absolute;
          top: 0;
          left: 50%;
          transform: translate(-50%, 0);
          transition: transform 1s ease-in-out;
          letter-spacing: 10px;
          font-size: 28px;
        }
      }
    }

    .digital_scroll_number_item:last-child {
      margin-right: 0;
    }
  }
</style>
