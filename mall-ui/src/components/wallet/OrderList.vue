<template>
  <van-pull-refresh v-model="refreshing" @refresh="loadData">
    <van-empty v-if="!loading && !items.length" :description="emptyText" image="search" />
    <div v-else class="list-wrapper">
      <div v-for="item in items" :key="item.id" class="order-item">
        <div class="order-main">
          <div class="order-left">
            <div class="order-icon" :class="item.tone">
              <van-icon :name="item.icon" />
            </div>
            <div class="order-info">
              <span class="order-title">{{ item.title }}</span>
              <span class="order-desc">{{ item.desc }}</span>
            </div>
          </div>
          <div class="order-right">
            <span class="order-amount" :class="item.tone">{{ item.amount }}</span>
            <span class="order-time">{{ item.time }}</span>
          </div>
        </div>
        <div v-if="item.meta" class="meta-row">{{ item.meta }}</div>
        <div v-if="item.extra" class="extra-row">{{ item.extra }}</div>
      </div>
    </div>
  </van-pull-refresh>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import {
  getOverview,
  getRechargeRecords,
  getTransferRecords,
  getWalletFlows,
  getWithdrawRecords,
  type RechargeRecord,
  type TransferRecord,
  type WalletFlowRecord,
  type WithdrawRecord,
} from '@/api/wallet'

type TabType = 'flow' | 'deposit' | 'withdraw' | 'transfer'
type Tone = 'in' | 'out' | 'pending' | 'muted'

interface DisplayItem {
  id: string
  title: string
  desc: string
  amount: string
  time: string
  icon: string
  tone: Tone
  meta?: string
  extra?: string
}

const props = defineProps<{
  type: TabType
}>()

const items = ref<DisplayItem[]>([])
const loading = ref(false)
const refreshing = ref(false)
const currentUserId = ref(0)

const emptyText = computed(() => {
  if (props.type === 'flow') return '暂无钱包流水'
  if (props.type === 'deposit') return '暂无充值记录'
  if (props.type === 'withdraw') return '暂无提现记录'
  return '暂无转账记录'
})

async function loadData() {
  loading.value = true
  try {
    if (props.type === 'flow') {
      items.value = (await getWalletFlows()).map(flowItem)
      return
    }
    if (props.type === 'deposit') {
      items.value = (await getRechargeRecords()).map(depositItem)
      return
    }
    if (props.type === 'transfer') {
      if (!currentUserId.value) {
        currentUserId.value = (await getOverview()).userId
      }
      items.value = (await getTransferRecords()).map(transferItem)
      return
    }
    items.value = (await getWithdrawRecords()).map(withdrawItem)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function flowItem(r: WalletFlowRecord): DisplayItem {
  const tone = directionTone(r.direction)
  return {
    id: r.flowNo,
    title: bizText(r.bizType),
    desc: r.remark || r.bizId,
    amount: `${amountPrefix(r.direction)}${fmtAmount(r.changeAmt)}`,
    time: formatTime(r.createTime),
    icon: tone === 'in' ? 'plus' : 'minus',
    tone,
    meta: `可用余额：${fmtAmount(r.balanceBefore)} → ${fmtAmount(r.balanceAfter)}`,
  }
}

function depositItem(r: RechargeRecord): DisplayItem {
  return {
    id: r.orderNo,
    title: depositStatusText(r.status),
    desc: `${r.chainCode} / ${r.coin}`,
    amount: `+${fmtAmount(r.amount)} ${r.coin}`,
    time: formatTime(r.createTime),
    icon: 'plus',
    tone: r.status === 'CREDITED' ? 'in' : 'pending',
    meta: r.status === 'CREDITED' ? creditedText(r.creditedAt) : `确认数：${r.confirmations}`,
    extra: r.txHash ? `Tx: ${shorten(r.txHash)}` : `To: ${shorten(r.toAddress)}`,
  }
}

function withdrawItem(r: WithdrawRecord): DisplayItem {
  return {
    id: r.orderNo,
    title: withdrawStatusText(r.status),
    desc: `${r.chainCode} / ${r.coin}`,
    amount: `-${fmtAmount(r.amount)} ${r.coin}`,
    time: formatTime(r.createTime),
    icon: 'minus',
    tone: withdrawTone(r.status),
    meta: `到账 ${fmtAmount(r.receiveAmount)} ${r.coin} · 手续费 ${fmtAmount(r.fee)}`,
    extra: r.txHash ? `Tx: ${shorten(r.txHash)}` : `To: ${shorten(r.toAddress)}`,
  }
}

function transferItem(r: TransferRecord): DisplayItem {
  const isIncoming = r.toUserId === currentUserId.value
  return {
    id: r.orderNo,
    title: transferStatusText(r.status),
    desc: isIncoming ? `来自用户 ${r.fromUserId}` : `转给用户 ${r.toUserId}`,
    amount: `${isIncoming ? '+' : '-'}${fmtAmount(r.amount)} ${r.coin}`,
    time: formatTime(r.createTime),
    icon: isIncoming ? 'plus' : 'minus',
    tone: isIncoming ? 'in' : 'out',
    meta: r.finishedAt ? `完成时间：${formatTime(r.finishedAt)}` : undefined,
    extra: r.remark || `订单号：${shorten(r.orderNo)}`,
  }
}

function directionTone(direction: string): Tone {
  if (direction === 'IN' || direction === 'UNFREEZE') return 'in'
  return 'out'
}

function amountPrefix(direction: string): string {
  return directionTone(direction) === 'in' ? '+' : '-'
}

function withdrawTone(status: string): Tone {
  if (status === 'ON_CHAIN_CONFIRMED') return 'out'
  if (status === 'REJECTED' || status === 'FAILED') return 'muted'
  return 'pending'
}

function bizText(bizType: string): string {
  const map: Record<string, string> = {
    RECHARGE: '充值入账',
    WITHDRAW_FREEZE: '提现冻结',
    WITHDRAW_UNFREEZE: '提现退回',
    WITHDRAW_SETTLE: '提现出款',
    TRANSFER_IN: '站内转入',
    TRANSFER_OUT: '站内转出',
    TASK_REWARD: '任务收益',
    TASK_CENTER_REWARD: '任务中心收益',
    SHARE_TASK_REWARD: '分享任务奖励',
    VIDEO_TASK_REWARD: '视频任务奖励',
    VA_TASK_REWARD: 'VA任务奖励',
    INVITE_COMMISSION: '邀请返佣',
    CHECKIN_REWARD: '签到奖励',
    LOTTERY_REWARD: '抽奖奖励',
    VIP_UPGRADE: 'VIP升级扣款',
  }
  return map[bizType] || bizType
}

function depositStatusText(status: string): string {
  const map: Record<string, string> = {
    PENDING: '充值待确认',
    CONFIRMING: '充值确认中',
    CREDITED: '充值已到账',
    EXPIRED: '充值已过期',
  }
  return map[status] || status
}

function withdrawStatusText(status: string): string {
  const map: Record<string, string> = {
    REVIEWING: '提现待审核',
    REJECTED: '提现已驳回',
    BROADCASTING: '链上广播中',
    ON_CHAIN_CONFIRMED: '提现已到账',
    FAILED: '提现失败',
  }
  return map[status] || status
}

function transferStatusText(status: string): string {
  const map: Record<string, string> = {
    SUCCESS: '转账成功',
    FAILED: '转账失败',
  }
  return map[status] || status
}

function creditedText(ms?: number): string {
  return ms ? `入账时间：${formatTime(ms)}` : '已完成入账'
}

function fmtAmount(n: number): string {
  return parseFloat(Number(n ?? 0).toFixed(6)).toString()
}

function shorten(v?: string): string {
  if (!v) return '-'
  return v.length > 18 ? `${v.slice(0, 8)}…${v.slice(-8)}` : v
}

function formatTime(ms: number): string {
  if (!ms) return ''
  const d = new Date(ms)
  const p = (x: number) => String(x).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

watch(() => props.type, loadData)
onMounted(loadData)
</script>

<style scoped>
.list-wrapper {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.order-item {
  background: #fff;
  border-radius: 10px;
  padding: 12px 14px;
}
.order-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.order-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.order-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.order-icon.in {
  background: #e8f5e9;
  color: #4caf50;
}
.order-icon.out {
  background: #fff3e0;
  color: #ff9800;
}
.order-icon.pending {
  background: #e3f2fd;
  color: #2196f3;
}
.order-icon.muted {
  background: #f5f5f5;
  color: #999;
}
.order-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}
.order-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}
.order-desc {
  font-size: 11px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
}
.order-right {
  text-align: right;
  flex-shrink: 0;
}
.order-amount {
  display: block;
  font-size: 16px;
  font-weight: 700;
}
.order-amount.in {
  color: #4caf50;
}
.order-amount.out,
.order-amount.pending {
  color: #ff9800;
}
.order-amount.muted {
  color: #999;
}
.order-time {
  display: block;
  font-size: 10px;
  color: #bbb;
  margin-top: 4px;
}
.meta-row,
.extra-row {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f2f2f2;
  font-size: 11px;
  color: #999;
}
.extra-row {
  border-top: 0;
  padding-top: 0;
  word-break: break-all;
}
</style>