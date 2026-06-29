<template>
  <div class="tasks-page">
    <van-nav-bar title="任务" left-arrow fixed placeholder @click-left="$router.back()" />
    <!-- 顶部四维数据总览 (固定不随Tab切换而销毁) -->
    <div class="task-header">
      <div class="data-item">
        <span class="value">{{ stats.completedCount }}</span>
        <span class="label">Completed</span>
      </div>
      <div class="data-item">
        <span class="value">{{ stats.inProgressCount }}</span>
        <span class="label">In Progress</span>
      </div>
      <div class="data-item highlight">
        <span class="value">{{ stats.totalEarnings }}</span>
        <span class="label">My Earnings</span>
      </div>
      <div class="data-item">
        <span class="value">{{ stats.teamEarnings }}</span>
        <span class="label">Team Earnings</span>
      </div>
    </div>

    <!-- Tabs 分类列表 -->
    <van-tabs v-model:active="activeTab" sticky offset-top="46" shrink>
      <van-tab title="Available" name="available">
        <TaskList status="available" />
      </van-tab>
      <van-tab title="In Progress" name="in_progress">
        <TaskList status="in_progress" />
      </van-tab>
      <van-tab title="Completed" name="completed">
        <TaskList status="completed" />
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import TaskList from '@/components/task/TaskList.vue'

const activeTab = ref('available')

// TODO: 统计数据建议单独调用 /api/tasks/stats 接口，避免依赖全量列表数据
const stats = ref({
  completedCount: 128,
  inProgressCount: 3,
  totalEarnings: '45.50',
  teamEarnings: '12.50',
})
</script>

<style scoped>
.tasks-page {
  background: #f7f8fa;
  min-height: 100vh;
}

.task-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 12px;
  padding: 16px 8px;
  background: #fff;
  border-radius: 8px;
}

.data-item {
  text-align: center;
  flex: 1;
}

.data-item .value {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #333;
  line-height: 1.3;
}

.data-item.highlight .value {
  color: #ff976a;
}

.data-item .label {
  display: block;
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

/* 覆盖 Vant Tabs 默认样式，使其更贴合卡片风格 */
:deep(.van-tabs__nav) {
  background: transparent;
  margin: 0 12px;
  width: calc(100% - 24px);
}
:deep(.van-tabs__content) {
  min-height: 60vh; /* 防止列表过短时底部导航栏遮挡 */
}
</style>
