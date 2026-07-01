<template>
  <div class="member-item">
    <div class="avatar">
      <van-icon name="user-o" />
    </div>
    <div class="info">
      <div class="name-row">
        <span class="name">{{ member.nickname || '未设置昵称' }}</span>
        <van-tag type="primary" plain>VIP{{ member.vipLevel || 0 }}</van-tag>
      </div>
      <span class="join-time">加入时间：{{ formatTime(member.invitateTime) }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { TeamMemberRecord } from '@/api/user'

defineProps<{
  member: TeamMemberRecord
}>()

function formatTime(ms: number): string {
  if (!ms) return '-'
  const d = new Date(ms)
  const p = (x: number) => String(x).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}
</script>

<style scoped>
.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border-radius: 10px;
  padding: 14px 16px;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e8eaf6;
  color: #3949ab;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.info {
  flex: 1;
  min-width: 0;
}
.name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.name {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.join-time {
  font-size: 11px;
  color: #999;
}
</style>