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

    <!-- 钱包记录 Tabs：只展示已有真实接口的数据域。 -->
    <van-tabs v-model:active="activeTab" sticky offset-top="46" shrink swipeable>
      <van-tab title="账变流水" name="flow">
        <OrderList type="flow" />
      </van-tab>
      <van-tab title="充值记录" name="deposit">
        <OrderList type="deposit" />
      </van-tab>
      <van-tab title="提现记录" name="withdraw">
        <OrderList type="withdraw" />
      </van-tab>
      <van-tab title="转账记录" name="transfer">
        <OrderList type="transfer" />
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import OrderList from '@/components/wallet/OrderList.vue'
import { getOverview } from '@/api/wallet'
import { tokenStore } from '@/api/http'

const router = useRouter()
const route = useRoute()
const activeTab = ref(String(route.query.tab || 'flow'))

const assets = ref({
  totalBalance: '0.00',
  available: '0.00',
  frozen: '0.00',
})

const fmt = (n: number) =>
  Number(n ?? 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

async function loadOverview() {
  // 未登录则不拉取，避免触发 401 跳转；展示占位 0
  if (!tokenStore.get()) return
  const o = await getOverview()
  assets.value = {
    totalBalance: fmt(o.totalBalance),
    available: fmt(o.availBalance),
    frozen: fmt(o.frozenBalance),
  }
}

const handleAction = (type: string) => {
  if (type === 'deposit') {
    router.push('/wallet/deposit')
    return
  }
  if (type === 'withdraw') {
    router.push('/wallet/withdraw')
    return
  }
  if (type === 'transfer') {
    router.push('/wallet/transfer')
    return
  }
  showToast('该功能即将开放')
}

watch(
  () => route.query.tab,
  (tab) => {
    if (typeof tab === 'string' && ['flow', 'deposit', 'withdraw', 'transfer'].includes(tab)) {
      activeTab.value = tab
    }
  }
)

onMounted(loadOverview)
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
