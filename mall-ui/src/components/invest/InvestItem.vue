<template>
  <div class="invest-item">
    <div class="item-header">
      <span class="plan-name">{{ plan.title }}</span>
      <van-tag :type="riskTagType" plain size="medium">{{ plan.riskLevel }}</van-tag>
    </div>

    <div class="rate-row">
      <span class="rate-value">{{ Number(plan.displayRate || 0).toFixed(4) }}%</span>
      <span class="rate-label">Display Rate</span>
    </div>

    <div class="desc" v-if="plan.description">{{ plan.description }}</div>

    <div class="footer-row">
      <span class="cycle-text">
        {{ plan.cycleDays }} Days · {{ moneyText(plan.minAmount) }} - {{ moneyText(plan.maxAmount) }} {{ plan.currency }}
      </span>
      <van-tag type="primary" size="large">Display</van-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { MissionInvestProject } from '@/api/mission';

const props = defineProps<{
  plan: MissionInvestProject;
}>();

const riskTagType = computed(() => {
  if (props.plan.riskLevel === 'LOW') return 'success';
  if (props.plan.riskLevel === 'MEDIUM') return 'warning';
  return 'danger';
});

const moneyText = (value?: number) => Number(value || 0).toFixed(6);
</script>

<style scoped>
.invest-item {
  background: #fff;
  border-radius: 10px;
  padding: 14px 16px;
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.plan-name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.rate-row {
  margin-bottom: 10px;
}
.rate-value {
  font-size: 24px;
  font-weight: 700;
  color: #ff5722;
  margin-right: 4px;
}
.rate-label {
  font-size: 11px;
  color: #999;
}

.desc {
  font-size: 12px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 10px;
}

.footer-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 10px;
  border-top: 1px dashed #eee;
}
.cycle-text {
  font-size: 12px;
  color: #999;
}
</style>