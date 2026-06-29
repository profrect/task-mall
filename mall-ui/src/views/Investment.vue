<template>
  <div class="invest-page">
    <van-nav-bar title="投资" left-arrow fixed placeholder @click-left="$router.back()" />
    <!-- 顶部五维数据总览 (可用余额 + 4项统计) -->
    <div class="asset-card">
      <div class="balance-section">
        <span class="balance-label">Available Balance</span>
        <span class="balance-value">{{ stats.availableBalance }}</span>
      </div>

      <div class="stats-row">
        <div class="stat-item">
          <span class="stat-val">{{ stats.currentProjects }}</span>
          <span class="stat-label">Current</span>
        </div>
        <div class="stat-item">
          <span class="stat-val">{{ stats.completedProjects }}</span>
          <span class="stat-label">Completed</span>
        </div>
        <div class="stat-item highlight">
          <span class="stat-val">{{ stats.totalProfit }}</span>
          <span class="stat-label">Total Profit</span>
        </div>
        <!-- 新增：昨日收益 -->
        <div class="stat-item yesterday">
          <span class="stat-val">{{ stats.yesterdayProfit }}</span>
          <span class="stat-label">Yesterday</span>
        </div>
      </div>
    </div>

    <!-- Tabs 分类列表 (保持不变) -->
    <van-tabs v-model:active="activeTab" sticky offset-top="46" shrink>
      <van-tab title="Available" name="available">
        <InvestList status="available" />
      </van-tab>
      <van-tab title="In Progress" name="in_progress">
        <InvestList status="in_progress" />
      </van-tab>
      <van-tab title="History" name="history">
        <InvestList status="history" />
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import InvestList from '@/components/invest/InvestList.vue'

const activeTab = ref('available')

// TODO: 替换为 /api/invest/stats 接口
const stats = ref({
  availableBalance: '450.00',
  currentProjects: 3,
  completedProjects: 28,
  totalProfit: '125.50',
  yesterdayProfit: '8.20', // 新增昨日收益字段
})
</script>

<style scoped>
.invest-page {
  background: #f5f6fa;
  min-height: 100vh;
}

.asset-card {
  margin: 12px;
  padding: 20px 16px;
  background: linear-gradient(135deg, #1a237e 0%, #283593 100%);
  border-radius: 12px;
  color: #fff;
}

.balance-section {
  margin-bottom: 18px;
}
.balance-label {
  display: block;
  font-size: 12px;
  opacity: 0.8;
}
.balance-value {
  display: block;
  font-size: 32px;
  font-weight: 700;
  margin-top: 6px;
  letter-spacing: -0.5px;
}

.stats-row {
  display: flex;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}

/* 改为等宽分布，防止4项数据在小屏下挤压 */
.stat-item {
  text-align: center;
  flex: 1;
  min-width: 0; /* 允许文本截断，防止撑破布局 */
}

.stat-val {
  display: block;
  font-size: 16px; /* 4项数据时适当缩小字号 */
  font-weight: 700;
  line-height: 1.3;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-label {
  display: block;
  font-size: 10px;
  opacity: 0.7;
  margin-top: 4px;
}

.stat-item.highlight .stat-val {
  color: #ffab00;
}
.stat-item.yesterday .stat-val {
  color: #4caf50;
} /* 昨日收益使用绿色区分 */

/* Tabs 样式微调 */
:deep(.van-tabs__nav) {
  background: transparent;
  margin: 0 12px;
  width: calc(100% - 24px);
}
:deep(.van-tabs__content) {
  min-height: 60vh;
}
</style>
