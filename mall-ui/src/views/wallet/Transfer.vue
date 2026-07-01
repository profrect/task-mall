<template>
  <div class="transfer-page">
    <van-nav-bar title="站内转账" left-arrow fixed placeholder @click-left="$router.back()" />

    <div class="balance-card">
      <div class="balance-label">可用余额</div>
      <div class="balance-value">{{ fmtMoney(overview.availBalance) }} USDT</div>
      <div class="balance-sub">
        冻结 {{ fmtMoney(overview.frozenBalance) }} USDT · 总额 {{ fmtMoney(overview.totalBalance) }} USDT
      </div>
    </div>

    <van-form class="form-card" @submit="submitTransfer">
      <van-field
        v-model="form.toUserId"
        name="toUserId"
        label="收款用户ID"
        placeholder="请输入收款用户ID"
        type="digit"
        clearable
        :rules="[{ required: true, message: '请输入收款用户ID' }]"
      />
      <van-field
        v-model="form.amount"
        name="amount"
        label="转账金额"
        placeholder="请输入转账金额"
        type="number"
        clearable
        :rules="[{ required: true, message: '请输入转账金额' }]"
      />
      <van-field
        v-model="form.remark"
        name="remark"
        label="备注"
        placeholder="选填，最多 100 字"
        clearable
        maxlength="100"
      />
      <div class="tip-box">
        转账会立即完成：系统同事务扣减转出方、增加收款方，并生成双方钱包流水。请确认收款用户ID无误。
      </div>
      <div class="submit-wrap">
        <van-button round block type="primary" native-type="submit" :loading="submitting">
          确认转账
        </van-button>
      </div>
    </van-form>

    <div class="records">
      <div class="records-head">转账记录</div>
      <van-pull-refresh v-model="refreshing" @refresh="loadRecords">
        <van-empty v-if="!loadingRec && !records.length" description="暂无转账记录" image="search" />
        <div v-else class="record-list">
          <div v-for="r in records" :key="r.orderNo" class="record-item">
            <div class="rec-main">
              <span class="rec-amount" :class="directionClass(r)">
                {{ directionPrefix(r) }}{{ fmtAmount(r.amount) }} {{ r.coin }}
              </span>
              <span class="rec-status" :class="statusClass(r.status)">{{ statusText(r.status) }}</span>
            </div>
            <div class="rec-sub">
              <span>{{ directionText(r) }}</span>
              <span>{{ formatTime(r.createTime) }}</span>
            </div>
            <div class="rec-sub">
              <span>订单号 {{ shorten(r.orderNo) }}</span>
              <span v-if="r.finishedAt">完成 {{ formatTime(r.finishedAt) }}</span>
            </div>
            <div v-if="r.remark" class="rec-remark">{{ r.remark }}</div>
          </div>
        </div>
      </van-pull-refresh>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { showConfirmDialog, showFailToast, showSuccessToast } from 'vant'
import {
  applyTransfer,
  getOverview,
  getTransferRecords,
  type TransferRecord,
  type WalletOverview,
} from '@/api/wallet'

const overview = ref<WalletOverview>({
  userId: 0,
  currency: 'USDT',
  totalBalance: 0,
  availBalance: 0,
  frozenBalance: 0,
})

const form = reactive({
  toUserId: '',
  amount: '',
  remark: '',
})

const records = ref<TransferRecord[]>([])
const submitting = ref(false)
const loadingRec = ref(false)
const refreshing = ref(false)

const currentUserId = computed(() => overview.value.userId)

async function loadOverview() {
  overview.value = await getOverview()
}

async function loadRecords() {
  loadingRec.value = true
  try {
    records.value = await getTransferRecords()
  } finally {
    loadingRec.value = false
    refreshing.value = false
  }
}

async function submitTransfer() {
  const toUserId = Number(form.toUserId)
  const amount = Number(form.amount)
  if (!Number.isInteger(toUserId) || toUserId <= 0) {
    showFailToast('收款用户ID非法')
    return
  }
  if (toUserId === currentUserId.value) {
    showFailToast('不能向自己转账')
    return
  }
  if (!Number.isFinite(amount) || amount <= 0) {
    showFailToast('转账金额必须大于 0')
    return
  }
  if (amount > overview.value.availBalance) {
    showFailToast('可用余额不足')
    return
  }
  await showConfirmDialog({
    title: '确认转账',
    message: `确认向用户 ${toUserId} 转账 ${fmtAmount(amount)} USDT？`,
  })
  submitting.value = true
  try {
    await applyTransfer({
      toUserId,
      coin: 'USDT',
      amount,
      remark: form.remark.trim() || undefined,
    })
    showSuccessToast('转账成功')
    form.toUserId = ''
    form.amount = ''
    form.remark = ''
    await Promise.all([loadOverview(), loadRecords()])
  } finally {
    submitting.value = false
  }
}

function isIn(r: TransferRecord): boolean {
  return r.toUserId === currentUserId.value
}

function directionClass(r: TransferRecord): string {
  return isIn(r) ? 'in' : 'out'
}

function directionPrefix(r: TransferRecord): string {
  return isIn(r) ? '+' : '-'
}

function directionText(r: TransferRecord): string {
  return isIn(r) ? `来自用户 ${r.fromUserId}` : `转给用户 ${r.toUserId}`
}

function statusText(s: string): string {
  return { SUCCESS: '成功', FAILED: '失败' }[s] ?? s
}

function statusClass(s: string): string {
  if (s === 'SUCCESS') return 'ok'
  if (s === 'FAILED') return 'fail'
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

function formatTime(ms?: number): string {
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
.transfer-page {
  background: #f5f6fa;
  min-height: 100vh;
}

.balance-card {
  margin: 12px;
  padding: 20px;
  background: linear-gradient(135deg, #2196f3 0%, #1565c0 100%);
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
.tip-box {
  margin: 12px 16px 0;
  padding: 10px 12px;
  background: #e3f2fd;
  border-radius: 8px;
  color: #1565c0;
  font-size: 11px;
  line-height: 1.6;
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
.rec-main,
.rec-sub {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.rec-amount {
  font-size: 16px;
  font-weight: 700;
}
.rec-amount.in {
  color: #4caf50;
}
.rec-amount.out {
  color: #ff9800;
}
.rec-status {
  font-size: 12px;
  font-weight: 600;
}
.rec-status.ok {
  color: #4caf50;
}
.rec-status.fail {
  color: #f44336;
}
.rec-status.pending {
  color: #2196f3;
}
.rec-sub {
  margin-top: 6px;
  font-size: 11px;
  color: #999;
}
.rec-remark {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f2f2f2;
  font-size: 11px;
  color: #666;
  word-break: break-all;
}
</style>