<template>
  <div class="earning-item">
    <div class="left">
      <div class="icon-box" :class="statusClass(record.status)">
        <van-icon :name="statusIcon(record.status)" />
      </div>
      <div class="info">
        <span class="title">{{ businessText(record.businessType) }}</span>
        <span class="source">来源用户：{{ record.sourceUserId }} / 订单：{{ shorten(record.sourceOrderNo) }}</span>
      </div>
    </div>
    <div class="right">
      <span class="amount">+{{ fmtAmount(record.commissionAmount) }} {{ record.currency }}</span>
      <span class="time">{{ formatTime(record.settledAt || record.createTime) }}</span>
      <span class="status" :class="statusClass(record.status)">{{ statusText(record.status) }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { InviteCommissionRecord } from '@/api/user'

defineProps<{
  record: InviteCommissionRecord
}>()

function businessText(type: string): string {
  const map: Record<string, string> = {
    VIP_UPGRADE: 'VIP升级返佣',
  }
  return map[type] || type
}

function statusText(status: string): string {
  const map: Record<string, string> = {
    PENDING: '待结算',
    SETTLED: '已入账',
    SETTLE_FAILED: '结算失败',
  }
  return map[status] || status
}

function statusClass(status: string): string {
  if (status === 'SETTLED') return 'settled'
  if (status === 'SETTLE_FAILED') return 'failed'
  return 'pending'
}

function statusIcon(status: string): string {
  if (status === 'SETTLED') return 'balance-o'
  if (status === 'SETTLE_FAILED') return 'warning-o'
  return 'underway-o'
}

function fmtAmount(n: number): string {
  return parseFloat(Number(n ?? 0).toFixed(6)).toString()
}

function shorten(v?: string): string {
  if (!v) return '-'
  return v.length > 18 ? `${v.slice(0, 8)}…${v.slice(-8)}` : v
}

function formatTime(ms?: number): string {
  if (!ms) return '-'
  const d = new Date(ms)
  const p = (x: number) => String(x).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
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
  flex-shrink: 0;
}
.icon-box.settled {
  background: #e8f5e9;
  color: #4caf50;
}
.icon-box.pending {
  background: #fff8e1;
  color: #ffab00;
}
.icon-box.failed {
  background: #ffebee;
  color: #f44336;
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
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
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
.status {
  display: inline-block;
  font-size: 10px;
  margin-top: 3px;
}
.status.settled {
  color: #4caf50;
}
.status.pending {
  color: #ffab00;
}
.status.failed {
  color: #f44336;
}
</style>