<template>
  <div class="invest-item">
    <div class="item-header">
      <span class="plan-name">{{ plan.name }}</span>
      <van-tag :type="riskTagType" plain size="medium">{{ plan.riskLevel }}</van-tag>
    </div>

    <!-- 可投资状态：突出收益率 -->
    <template v-if="plan.status === 'available'">
      <div class="rate-row">
        <span class="rate-value">{{ plan.dailyRate }}%</span>
        <span class="rate-label">Daily Return</span>
      </div>
      <div class="footer-row">
        <span class="cycle-text">{{ plan.cycle }} Days · Min {{ plan.amount }}</span>
        <van-button size="small" type="primary" round @click.stop="emit('action')"
          >Subscribe</van-button
        >
      </div>
    </template>

    <!-- 进行中状态：突出进度与已投金额 -->
    <template v-else-if="plan.status === 'in_progress'">
      <div class="progress-info">
        <span>Invested: {{ plan.amount }}</span>
        <span>{{ plan.progress }}%</span>
      </div>
      <van-progress :percentage="plan.progress" stroke-width="6" color="#1a237e" />
      <div class="footer-row" style="margin-top: 10px">
        <span class="cycle-text">{{ plan.dailyRate }}% Daily · {{ plan.cycle }} Days</span>
        <van-button size="small" type="warning" round @click.stop="emit('action')"
          >Detail</van-button
        >
      </div>
    </template>

    <!-- 历史记录状态：突出累计收益 -->
    <template v-else>
      <div class="profit-row">
        <span class="profit-label">Total Profit</span>
        <span class="profit-value">+{{ plan.profit }}</span>
      </div>
      <div class="footer-row">
        <span class="cycle-text">Invested: ${{ plan.amount }} · {{ plan.cycle }} Days</span>
        <van-tag type="default" size="large">Settled</van-tag>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  plan: {
    id: string
    name: string
    dailyRate: number
    cycle: number
    amount: number
    status: 'available' | 'in_progress' | 'history'
    progress?: number
    profit?: number
    riskLevel: string
  }
}>()

defineEmits(['action'])

const riskTagType = computed(() => {
  if (props.plan.riskLevel === 'Low') return 'success'
  if (props.plan.riskLevel === 'Medium') return 'warning'
  return 'danger'
})
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
  margin-bottom: 12px;
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

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
}

.profit-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}
.profit-label {
  font-size: 12px;
  color: #999;
}
.profit-value {
  font-size: 22px;
  font-weight: 700;
  color: #4caf50;
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
