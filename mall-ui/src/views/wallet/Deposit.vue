<template>
  <div class="deposit-page">
    <van-nav-bar title="充值" left-arrow fixed placeholder @click-left="$router.back()" />

    <!-- 链/币种：阶段1仅 TRON-USDT 可用 -->
    <div class="chain-bar">
      <span class="chain-badge">TRON</span>
      <span class="chain-text">USDT · TRC20</span>
    </div>

    <!-- 收款地址 + 二维码 -->
    <div class="addr-card">
      <van-loading v-if="loadingAddr" class="addr-loading" />
      <template v-else-if="address">
        <div class="qr-box">
          <img v-if="qrDataUrl" :src="qrDataUrl" alt="deposit qr" class="qr-img" />
        </div>
        <div class="addr-label">本人 USDT-TRC20 收款地址</div>
        <div class="addr-value">{{ address }}</div>
        <van-button type="primary" size="small" round icon="orders-o" @click="copyAddress">
          复制地址
        </van-button>
        <div class="addr-tip">
          仅向该地址转入 <b>USDT-TRC20</b>，转入其它币种或链将无法到账。到账需 {{ MIN_CONF }} 个网络确认。
        </div>
      </template>
      <van-empty v-else description="充值地址暂不可用" image="error" />
    </div>

    <!-- 充值记录 -->
    <div class="records">
      <div class="records-head">充值记录</div>
      <van-pull-refresh v-model="refreshing" @refresh="loadRecords">
        <van-empty v-if="!loadingRec && !records.length" description="暂无充值记录" image="search" />
        <div v-else class="record-list">
          <div v-for="r in records" :key="r.orderNo" class="record-item">
            <div class="rec-main">
              <span class="rec-amount">+{{ fmtAmount(r.amount) }} {{ r.coin }}</span>
              <span class="rec-status" :class="statusClass(r.status)">{{ statusText(r.status) }}</span>
            </div>
            <div class="rec-sub">
              <span>{{ formatTime(r.createTime) }}</span>
              <span v-if="r.status !== 'CREDITED'">确认 {{ r.confirmations }}/{{ MIN_CONF }}</span>
            </div>
            <div class="rec-tx" v-if="r.txHash">Tx: {{ shorten(r.txHash) }}</div>
          </div>
        </div>
      </van-pull-refresh>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { showSuccessToast, showFailToast } from 'vant'
import { toDataURL } from 'qrcode'
import { getDepositAddress, getRechargeRecords, type RechargeRecord } from '@/api/wallet'

// 阶段1 TRON 入账确认门槛（与后端 wallet.tron.min-confirmations 默认值一致，仅用于展示）
const MIN_CONF = 19

const address = ref('')
const qrDataUrl = ref('')
const loadingAddr = ref(true)

const records = ref<RechargeRecord[]>([])
const loadingRec = ref(false)
const refreshing = ref(false)

async function loadAddress() {
  loadingAddr.value = true
  try {
    const res = await getDepositAddress('TRON')
    address.value = res.address
    qrDataUrl.value = await toDataURL(res.address, { margin: 1, width: 220 }).catch(() => '')
  } finally {
    loadingAddr.value = false
  }
}

async function loadRecords() {
  loadingRec.value = true
  try {
    records.value = await getRechargeRecords()
  } finally {
    loadingRec.value = false
    refreshing.value = false
  }
}

async function copyAddress() {
  if (!address.value) return
  try {
    await navigator.clipboard.writeText(address.value)
    showSuccessToast('地址已复制')
  } catch {
    showFailToast('复制失败，请手动长按选择')
  }
}

// 状态展示：后端状态机 PENDING/CONFIRMING/CREDITED/EXPIRED
function statusText(s: string): string {
  return { PENDING: '待确认', CONFIRMING: '确认中', CREDITED: '已到账', EXPIRED: '已过期' }[s] ?? s
}
function statusClass(s: string): string {
  if (s === 'CREDITED') return 'ok'
  if (s === 'EXPIRED') return 'muted'
  return 'pending'
}

function fmtAmount(n: number): string {
  return parseFloat(Number(n).toFixed(6)).toString()
}

function shorten(hash: string): string {
  return hash.length > 16 ? `${hash.slice(0, 8)}…${hash.slice(-6)}` : hash
}

function formatTime(ms: number): string {
  if (!ms) return ''
  const d = new Date(ms)
  const p = (x: number) => String(x).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

onMounted(() => {
  loadAddress()
  loadRecords()
})
</script>

<style scoped>
.deposit-page {
  background: #f5f6fa;
  min-height: 100vh;
}

.chain-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 12px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 12px;
}
.chain-badge {
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: #e53935;
  padding: 2px 10px;
  border-radius: 6px;
}
.chain-text {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.addr-card {
  margin: 12px;
  padding: 20px 16px;
  background: #fff;
  border-radius: 12px;
  text-align: center;
}
.addr-loading {
  padding: 40px 0;
}
.qr-box {
  display: flex;
  justify-content: center;
  margin-bottom: 14px;
}
.qr-img {
  width: 200px;
  height: 200px;
  border: 1px solid #eee;
  border-radius: 8px;
}
.addr-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}
.addr-value {
  font-size: 14px;
  color: #1a237e;
  font-weight: 600;
  word-break: break-all;
  line-height: 1.5;
  margin-bottom: 14px;
}
.addr-tip {
  margin-top: 14px;
  font-size: 11px;
  color: #ff9800;
  line-height: 1.6;
  text-align: left;
  background: #fff8e1;
  padding: 10px 12px;
  border-radius: 8px;
}

.records {
  margin: 12px;
}
.records-head {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  padding: 4px 4px 10px;
}
.record-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.record-item {
  background: #fff;
  border-radius: 10px;
  padding: 12px 14px;
}
.rec-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.rec-amount {
  font-size: 16px;
  font-weight: 700;
  color: #4caf50;
}
.rec-status {
  font-size: 12px;
  font-weight: 600;
}
.rec-status.ok {
  color: #4caf50;
}
.rec-status.pending {
  color: #ff9800;
}
.rec-status.muted {
  color: #999;
}
.rec-sub {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #999;
  margin-top: 6px;
}
.rec-tx {
  font-size: 11px;
  color: #bbb;
  margin-top: 4px;
}
</style>