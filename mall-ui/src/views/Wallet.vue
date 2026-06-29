<template>
  <div class="wallet-page">
    <van-nav-bar title="钱包" left-arrow fixed placeholder @click-left="$router.back()" />
    <!-- 资产总览卡片 -->
    <div class="asset-card">
      <div class="balance-section">
        <span class="label">Total Balance (USD)</span>
        <span class="value">{{ assets.totalBalance }}</span>
      </div>
      <div class="sub-row">
        <div class="sub-item">
          <span class="sub-val">{{ assets.available }}</span>
          <span class="sub-label">Available</span>
        </div>
        <div class="sub-item">
          <span class="sub-val">{{ assets.frozen }}</span>
          <span class="sub-label">Frozen</span>
        </div>
      </div>
    </div>

    <!-- 核心功能矩阵 (3项) -->
    <div class="action-grid">
      <div class="action-item" @click="handleAction('deposit')">
        <div class="icon-box deposit"><van-icon name="plus" /></div>
        <span>Deposit</span>
      </div>
      <div class="action-item" @click="handleAction('withdraw')">
        <div class="icon-box withdraw"><van-icon name="minus" /></div>
        <span>Withdraw</span>
      </div>
      <div class="action-item" @click="handleAction('transfer')">
        <div class="icon-box transfer"><van-icon name="exchange" /></div>
        <span>Transfer</span>
      </div>
    </div>

    <!-- 五维订单 Tabs -->
    <van-tabs v-model:active="activeTab" sticky offset-top="46" shrink swipeable>
      <van-tab title="Wallet Flow" name="flow">
        <OrderList type="flow" />
      </van-tab>
      <van-tab title="Deposits" name="deposit">
        <OrderList type="deposit" />
      </van-tab>
      <van-tab title="Withdrawals" name="withdraw">
        <OrderList type="withdraw" />
      </van-tab>
      <van-tab title="Transfers" name="transfer">
        <OrderList type="transfer" />
      </van-tab>
      <van-tab title="Payments" name="payment">
        <OrderList type="payment" />
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import OrderList from '@/components/wallet/OrderList.vue'

const activeTab = ref('flow')

// TODO: 替换为 /api/wallet/assets
const assets = ref({
  totalBalance: '1,250.00',
  available: '450.00',
  frozen: '800.00',
})

const handleAction = (type: string) => {
  console.log('Wallet action:', type)
  // TODO: 跳转至对应操作页或弹出弹窗
}
</script>

<style scoped>
.wallet-page {
  background: #f5f6fa;
  min-height: 100vh;
}

.asset-card {
  margin: 12px;
  padding: 24px 20px;
  background: linear-gradient(135deg, #1a237e 0%, #283593 100%);
  border-radius: 12px;
  color: #fff;
}
.balance-section .label {
  font-size: 12px;
  opacity: 0.8;
}
.balance-section .value {
  display: block;
  font-size: 34px;
  font-weight: 700;
  margin-top: 8px;
}
.sub-row {
  display: flex;
  gap: 32px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}
.sub-val {
  display: block;
  font-size: 16px;
  font-weight: 600;
}
.sub-label {
  display: block;
  font-size: 11px;
  opacity: 0.7;
  margin-top: 2px;
}

/* 3列功能网格 */
.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin: 12px;
  padding: 16px 8px;
  background: #fff;
  border-radius: 12px;
}
.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.action-item span {
  font-size: 12px;
  color: #333;
  font-weight: 500;
}
.icon-box {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
}
.deposit {
  background: #4caf50;
}
.withdraw {
  background: #ff9800;
}
.transfer {
  background: #2196f3;
}

/* Tabs 样式覆盖 */
:deep(.van-tabs__nav) {
  background: transparent;
  margin: 0 12px;
  width: calc(100% - 24px);
}
:deep(.van-tabs__content) {
  min-height: 60vh;
}
</style>
