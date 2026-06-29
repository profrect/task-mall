<template>
  <div class="page-leaderboard">
    <van-nav-bar title="排行榜" left-arrow fixed placeholder @click-left="$router.back()" />

    <!-- 榜单切换：仅保留日榜/周榜，避免过多tab干扰 -->
    <van-tabs v-model="activeTab" sticky offset-top="46">
      <van-tab title="收益排行" name="daily" />
      <van-tab title="充值排行" name="weekly" />
      <van-tab title="任务排行" name="weekly" />
    </van-tabs>

    <!-- 榜单列表：纯文本+数字，无头像框/光效 -->
    <van-cell-group inset class="rank-list">
      <van-cell
        v-for="(item, index) in rankList"
        :key="item.userId"
        :title="`${index + 1}. ${item.nickname}`"
        :value="`$${item.earnings}`"
        value-class="earnings"
      >
        <template #icon>
          <!-- 前三名用品牌色高亮，4名及以后灰色 -->
          <span class="rank-badge" :class="{ top: index < 3 }">{{ index + 1 }}</span>
        </template>
      </van-cell>
    </van-cell-group>

    <!-- 自己的排名：固定底部，避免用户反复滑动查找 -->
    <div class="my-rank" v-if="isLoggedIn">
      <span>My Rank: {{ myRank.rank || 'Unranked' }}</span>
      <span class="earnings">${{ myRank.earnings || 0 }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const activeTab = ref('daily')
const isLoggedIn = ref(false)

// 模拟数据，后续替换为API
const rankList = ref([
  { userId: 1, nickname: 'Alex***', earnings: 580.5 },
  { userId: 2, nickname: 'Sam***', earnings: 420.0 },
  { userId: 3, nickname: 'Jack***', earnings: 310.2 },
  { userId: 4, nickname: 'Tom***', earnings: 280.0 },
  { userId: 5, nickname: 'Lily***', earnings: 210.5 },
])

const myRank = ref({ rank: 12, earnings: 85.0 })
</script>

<style scoped>
.rank-list {
  margin-top: 12px;
}
.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  color: #999;
  background: #f5f5f5;
  margin-right: 12px;
}
.rank-badge.top {
  background: #1989fa;
  color: #fff;
}
.earnings {
  font-weight: 600;
  color: #333;
}
.my-rank {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  z-index: 100;
}
</style>
