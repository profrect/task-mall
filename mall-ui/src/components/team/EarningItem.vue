<template>
  <div class="earning-item">
    <div class="left">
      <div class="icon-box" :class="earning.type">
        <van-icon :name="iconMap[earning.type]" />
      </div>
      <div class="info">
        <span class="title">{{ labelMap[earning.type] }}</span>
        <span class="source">From: {{ earning.source }}</span>
      </div>
    </div>
    <div class="right">
      <span class="amount">+${{ earning.amount }}</span>
      <span class="time">{{ earning.time }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  earning: { id: string; type: string; amount: number; source: string; time: string }
}>()

const iconMap: Record<string, string> = {
  direct_reward: 'gift-o',
  level_bonus: 'medal-o',
  team_dividend: 'balance-o',
}
const labelMap: Record<string, string> = {
  direct_reward: 'Direct Reward',
  level_bonus: 'Level Bonus',
  team_dividend: 'Team Dividend',
}
</script>

<style scoped>
.earning-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 10px;
  padding: 14px 16px;
}
.left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}
.icon-box {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #fff;
  flex-shrink: 0;
}
.icon-box.direct_reward {
  background: #e8f5e9;
  color: #4caf50;
}
.icon-box.level_bonus {
  background: #fff8e1;
  color: #ffab00;
}
.icon-box.team_dividend {
  background: #e3f2fd;
  color: #2196f3;
}
.info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.source {
  font-size: 11px;
  color: #bbb;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.right {
  text-align: right;
  flex-shrink: 0;
}
.amount {
  display: block;
  font-size: 16px;
  font-weight: 700;
  color: #4caf50;
}
.time {
  display: block;
  font-size: 10px;
  color: #bbb;
  margin-top: 2px;
}
</style>
