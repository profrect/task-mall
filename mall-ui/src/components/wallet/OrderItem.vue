<template>
  <div class="order-item">
    <div class="order-left">
      <div class="order-icon" :class="iconClass">
        <van-icon :name="iconName" />
      </div>
      <div class="order-info">
        <span class="order-title">{{ title }}</span>
        <span class="order-desc">{{ desc }}</span>
      </div>
    </div>
    <div class="order-right">
      <span class="order-amount" :class="amountClass"> {{ amountPrefix }}{{ order.amount }} </span>
      <van-tag :type="statusTagType" size="medium">{{ statusLabel }}</van-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  order: any;
  type: 'flow' | 'deposit' | 'withdraw' | 'transfer' | 'payment';
}>();

// 图标映射
const iconMap = {
  flow: 'balance-o', deposit: 'plus', withdraw: 'minus',
  transfer: 'exchange', payment: 'credit-pay'
};
const iconName = computed(() => iconMap[props.type]);

// 图标背景色
const iconClass = computed(() => {
  if (props.type === 'flow') return props.order.direction === 'in' ? 'in' : 'out';
  return props.type;
});

// 标题与描述
const titleMap = {
  flow: 'Wallet Flow', deposit: 'Deposit Order', withdraw: 'Withdrawal',
  transfer: 'Internal Transfer', payment: 'Payment Order'
};
const title = computed(() => titleMap[props.type]);

const desc = computed(() => {
  if (props.type === 'transfer') return `To: ${props.order.counterparty}`;
  if (props.type === 'payment') return props.order.payMethod;
  if (props.type === 'flow') return props.order.direction === 'in' ? 'Income' : 'Expense';
  return props.order.time;
});

// 金额方向与颜色
const isIncoming = computed(() => {
  if (props.type === 'deposit' || props.type === 'flow' && props.order.direction === 'in') return true;
  return false;
});
const amountPrefix = computed(() => isIncoming.value ? '+' : '-');
const amountClass = computed(() => isIncoming.value ? 'in' : 'out');

// 状态标签
const statusTagType = computed(() => {
  if (props.order.status === 'success') return 'success';
  if (props.order.status === 'processing') return 'warning';
  return 'danger';
});
const statusLabel = computed(() => {
  const map = { success: 'Completed', processing: 'Processing', failed: 'Failed' };
  return map[props.order.status as keyof typeof map] || props.order.status;
});
</script>

<style scoped>
.order-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 10px;
  padding: 14px 16px;
}
.order-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.order-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #fff;
}
.order-icon.in,
.order-icon.deposit {
  background: #e8f5e9;
  color: #4caf50;
}
.order-icon.out,
.order-icon.withdraw {
  background: #fff3e0;
  color: #ff9800;
}
.order-icon.transfer {
  background: #e3f2fd;
  color: #2196f3;
}
.order-icon.payment {
  background: #f3e5f5;
  color: #9c27b0;
}
.order-icon.flow {
  background: #eceff1;
  color: #607d8b;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.order-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.order-desc {
  font-size: 11px;
  color: #bbb;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.order-right {
  text-align: right;
  flex-shrink: 0;
}
.order-amount {
  display: block;
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 4px;
}
.order-amount.in {
  color: #4caf50;
}
.order-amount.out {
  color: #333;
}
</style>
