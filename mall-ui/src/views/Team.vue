<template>
  <div class="team-page">
    <van-nav-bar title="团队" left-arrow fixed placeholder @click-left="$router.back()" />
    <!-- 团队收益概览卡片 -->
    <div class="profit-card">
      <div class="profit-main">
        <span class="profit-label">Team Total Profit</span>
        <span class="profit-value">{{ teamStats.totalProfit }}</span>
      </div>
      <div class="profit-sub">
        <div class="sub-item">
          <span class="sub-val">{{ teamStats.directCount }}</span>
          <span class="sub-label">Direct Members</span>
        </div>
        <div class="sub-item">
          <span class="sub-val">{{ teamStats.todayProfit }}</span>
          <span class="sub-label">Today's Profit</span>
        </div>
      </div>
    </div>

    <!-- 双维度分页 Tabs -->
    <van-tabs v-model:active="activeTab" sticky offset-top="46" swipeable>
      <van-tab title="Members" name="members">
        <MemberList />
      </van-tab>
      <van-tab title="Earnings" name="earnings">
        <EarningList />
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import MemberList from '@/components/team/MemberList.vue'
import EarningList from '@/components/team/EarningList.vue'

const activeTab = ref('members')

// TODO: 替换为 /api/team/superior
const superior = ref({ nickname: 'Alex_Wang', level: 5 })

// TODO: 替换为 /api/team/stats
const teamStats = ref({
  totalProfit: '3,280.50',
  directCount: 42,
  todayProfit: '126.80',
})
</script>

<style scoped>
.team-page {
  background: #f5f6fa;
  min-height: 100vh;
}

.superior-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
}
.avatar-box {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e8eaf6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3949ab;
}
.superior-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.superior-info .label {
  font-size: 11px;
  color: #999;
}
.superior-info .name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.profit-card {
  margin: 12px;
  padding: 24px 20px;
  background: linear-gradient(135deg, #1a237e 0%, #283593 100%);
  border-radius: 12px;
  color: #fff;
}
.profit-main .profit-label {
  font-size: 12px;
  opacity: 0.8;
}
.profit-main .profit-value {
  display: block;
  font-size: 32px;
  font-weight: 700;
  margin-top: 6px;
}
.profit-sub {
  display: flex;
  gap: 32px;
  margin-top: 18px;
  padding-top: 14px;
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
