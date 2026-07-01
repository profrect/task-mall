<template>
  <div class="withdraw-page">
    <van-nav-bar title="提现" left-arrow fixed placeholder @click-left="$router.back()" />

    <div class="balance-card">
      <div class="balance-label">可用余额</div>
      <div class="balance-value">{{ fmtMoney(overview.availBalance) }} USDT</div>
      <div class="balance-sub">
        冻结 {{ fmtMoney(overview.frozenBalance) }} USDT · 总额 {{ fmtMoney(overview.totalBalance) }} USDT
      </div>
    </div>

    <van-form class="form-card" @submit="submitWithdraw">
      <div class="chain-row">
        <span class="chain-badge">TRON</span>
        <span class="chain-text">USDT · TRC20</span>
      </div>
      <van-field
        v-model="form.toAddress"
        name="toAddress"
        label="提现地址"
        placeholder="请输入 USDT-TRC20 收款地址"
        clearable
        :rules="[{ required: true, message: '请输入提现地址' }]"
      />
      <van-field
        v-model="form.amount"
        name="amount"
        label="提现金额"
        placeholder="请输入提现金额"
        type="number"
        clearable
        :rules="[{ required: true, message: '请输入提现金额' }]"
      />
      <div class="fee-box">
        <div>提现手续费、实际到账金额以审核后的提现记录为准。</div>
        <div>提交后先冻结余额，审核通过后链上广播；审核驳回会自动解冻。</div>
      </div>
      <div class="submit-wrap">
        <van-button round block type="primary" native-type="submit" :loading="submitting">
          提交提现申请
        </van-button>
      </div>
    </van-form>

    <div class="records">
      <div class="records-head">提现记录</div>
      <van-pull-refresh v-model="refreshing" @refresh="loadRecords">
        <van-empty v-if="!loadingRec && !records.length" description="暂无提现记录" image="search" />
        <div v-else class="record-list">
          <div v-for="r in records" :key="r.orderNo" class="record-item">
            <div class="rec-main">
              <span class="rec-amount">-{{ fmtAmount(r.amount) }} {{ r.coin }}</span>
              <span class="rec-status" :class="statusClass(r.status)">{{ statusText(r.status) }}</span>
            </div>
            <div class="rec-sub">
              <span>到账 {{ fmtAmount(r.receiveAmount) }} {{ r.coin }}</span>
              <span>手续费 {{ fmtAmount(r.fee) }}</span>
            </div>
            <div class="rec-sub">
              <span>{{ formatTime(r.createTime) }}</span>
              <span v-if="r.confirmations">确认 {{ r.confirmations }}</span>
            </div>
            <div class="rec-addr">To: {{ shorten(r.toAddress) }}</div>
            <div v-if="r.txHash" class="rec-addr">Tx: {{ shorten(r.txHash) }}</div>
            <div v-if="r.reviewRemark" class="rec-remark">{{ r.reviewRemark }}</div>
          </div>
        </div>
      </van-pull-refresh>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { showSuccessToast, showFailToast, showConfirmDialog } from 'vant'
import {
  applyWithdraw,
  getOverview,
  getWithdrawRecords,
  type WalletOverview,
  type WithdrawRecord,
} from '@/api/wallet'

const overview = ref<WalletOverview>({
  userId: 0,
  currency: 'USDT',
  totalBalance: 0,
  availBalance: 0,
  frozenBalance: 0,
})

const form = reactive({
  toAddress: '',
  amount: '',
})

const records = ref<WithdrawRecord[]>([])
const submitting = ref(false)
const loadingRec = ref(false)
const refreshing = ref(false)

async function loadOverview() {
  overview.value = await getOverview()
}

async function loadRecords() {
  loadingRec.value = true
  try {
    records.value = await getWithdrawRecords()
  } finally {
    loadingRec.value = false
    refreshing.value = false
  }
}

async function submitWithdraw() {
  const amount = Number(form.amount)
  if (!Number.isFinite(amount) || amount <= 0) {
    showFailToast('提现金额必须大于 0')
    return
  }
  if (amount > overview.value.availBalance) {
    showFailToast('可用余额不足')
    return
  }
  await showConfirmDialog({
    title: '确认提现',
    message: `确认提现 ${fmtAmount(amount)} USDT 到该 TRC20 地址？手续费和实际到账以审核后的提现记录为准。`,
  })
  submitting.value = true
  try {
    await applyWithdraw({
      chain: 'TRON',
      coin: 'USDT',
      toAddress: form.toAddress.trim(),
      amount,
    })
    showSuccessToast('提现申请已提交')
    form.amount = ''
    form.toAddress = ''
    await Promise.all([loadOverview(), loadRecords()])
  } finally {
    submitting.value = false
  }
}

function statusText(s: string): string {
  return {
    REVIEWING: '待审核',
    REJECTED: '已驳回',
    BROADCASTING: '广播中',
    ON_CHAIN_CONFIRMED: '已到账',
    FAILED: '失败',
  }[s] ?? s
}

function statusClass(s: string): string {
  if (s === 'ON_CHAIN_CONFIRMED') return 'ok'
  if (s === 'REJECTED' || s === 'FAILED') return 'fail'
  return 'pending'
}

function fmtMoney(n: number): string {
  return Number(n ?? 0).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

function fmtAmount(n: number): string {
  return parseFloat(Number(n ?? 0).toFixed(6)).toString()
}

function shorten(v: string): string {
  if (!v) return '-'
  return v.length > 18 ? `${v.slice(0, 8)}…${v.slice(-8)}` : v
}

function formatTime(ms: number): string {
  if (!ms) return ''
  const d = new Date(ms)
  const p = (x: number) => String(x).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

onMounted(() => {
  loadOverview()
  loadRecords()
})
</script>

<style scoped>
.withdraw-page {
  background: #f5f6fa;
  min-height: 100vh;
}

.balance-card {
  margin: 12px;
  padding: 20px;
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
  border-radius: 12px;
  color: #fff;
}
.balance-label {
  font-size: 12px;
  opacity: 0.85;
}
.balance-value {
  font-size: 30px;
  font-weight: 700;
  margin-top: 8px;
}
.balance-sub {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 10px;
}

.form-card {
  margin: 12px;
  padding: 14px 0 16px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}
.chain-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px 12px;
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
.fee-box {
  margin: 12px 16px 0;
  padding: 10px 12px;
  background: #fff8e1;
  border-radius: 8px;
  color: #ff9800;
  font-size: 11px;
  line-height: 1.7;
}
.submit-wrap {
  padding: 16px 16px 0;
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
  color: #ff9800;
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
.rec-status.fail {
  color: #f44336;
}
.rec-sub {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #999;
  margin-top: 6px;
}
.rec-addr {
  font-size: 11px;
  color: #bbb;
  margin-top: 5px;
}
.rec-remark {
  margin-top: 8px;
  padding: 8px 10px;
  background: #fff3f3;
  border-radius: 6px;
  color: #f44336;
  font-size: 11px;
  line-height: 1.5;
}
</style>