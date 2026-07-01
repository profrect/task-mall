<template>
  <div class="page-leaderboard">
    <van-nav-bar title="排行榜" left-arrow fixed placeholder @click-left="$router.back()" />

    <van-tabs v-model:active="activeType" sticky offset-top="46" @change="loadData">
      <van-tab title="收益排行" name="EARNING" />
      <van-tab title="充值排行" name="RECHARGE" />
      <van-tab title="任务排行" name="TASK" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div class="rank-scope">{{ scopeText }}</div>
      <div v-if="rankList.length" class="rank-list">
        <div v-for="item in rankList" :key="`${item.type}-${item.userId}`" class="rank-card">
          <span class="rank-badge" :class="{ top: item.rankNo <= 3 }">{{ item.rankNo }}</span>
          <div class="rank-user">
            <span class="name">{{ item.displayName || `User ${item.userId}` }}</span>
            <span class="meta">ID: {{ item.userId }} · VIP {{ item.vipLevel ?? 0 }}</span>
          </div>
          <div class="rank-metric">
            <span class="value">{{ metricText(item) }}</span>
            <span class="label">{{ item.metricLabel }}</span>
          </div>
        </div>
      </div>
      <van-empty v-else-if="!loading" image="search" description="暂无真实榜单记录" />
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  getLeaderboard,
  type LeaderboardItem,
  type LeaderboardType,
} from '@/api/leaderboard'

const activeType = ref<LeaderboardType>('EARNING')
const rankList = ref<LeaderboardItem[]>([])
const loading = ref(false)
const refreshing = ref(false)

const scopeText = computed(() => {
  const map: Record<LeaderboardType, string> = {
    EARNING: '按真实奖励、返佣、抽奖入账流水聚合',
    RECHARGE: '按真实充值入账流水聚合',
    TASK: '按已审核通过任务记录聚合',
  }
  return map[activeType.value]
})

async function loadData() {
  loading.value = true
  try {
    rankList.value = await getLeaderboard({ type: activeType.value, limit: 20 })
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function onRefresh() {
  loadData()
}

function metricText(item: LeaderboardItem): string {
  const value = Number(item.metricValue || 0)
  if (item.type === 'TASK') return `${value.toLocaleString('en-US')} 次`
  return `${value.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 6,
  })} ${item.currency || 'USDT'}`
}

onMounted(loadData)
</script>

<style scoped>
.page-leaderboard {
  min-height: 100vh;
  background: #f5f6fa;
}
.rank-scope {
  margin: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fff;
  color: #666;
  font-size: 12px;
}
.rank-list {
  padding: 0 12px 16px;
}
.rank-card {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
  padding: 14px 12px;
  border-radius: 12px;
  background: #fff;
}
.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
  color: #777;
  background: #f0f2f5;
}
.rank-badge.top {
  background: #1989fa;
  color: #fff;
}
.rank-user {
  flex: 1;
  min-width: 0;
}
.name {
  display: block;
  overflow: hidden;
  color: #222;
  font-size: 14px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.meta {
  display: block;
  margin-top: 4px;
  color: #999;
  font-size: 11px;
}
.rank-metric {
  max-width: 140px;
  text-align: right;
}
.value {
  display: block;
  color: #1989fa;
  font-size: 14px;
  font-weight: 700;
}
.label {
  display: block;
  margin-top: 4px;
  color: #999;
  font-size: 11px;
}
</style>