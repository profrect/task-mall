<template>
  <div class="tasks-page">
    <van-nav-bar title="任务" left-arrow fixed placeholder @click-left="$router.back()" />
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
        <span class="value">{{ moneyText(stats.totalEarnings) }}</span>
        <span class="label">My Earnings</span>
      </div>
    </div>

    <van-tabs v-model:active="activeTab" sticky offset-top="46" shrink>
      <van-tab title="Available" name="available">
        <TaskList status="available" @changed="loadStats" />
      </van-tab>
      <van-tab title="In Progress" name="in_progress">
        <TaskList status="in_progress" @changed="loadStats" />
      </van-tab>
      <van-tab title="Completed" name="completed">
        <TaskList status="completed" @changed="loadStats" />
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import TaskList from '@/components/task/TaskList.vue';
import { getMissionStats, MissionTaskStats } from '@/api/mission';

const activeTab = ref('available');
const stats = ref<MissionTaskStats>({
  completedCount: 0,
  inProgressCount: 0,
  totalEarnings: 0,
});

const moneyText = (value?: number) => Number(value || 0).toFixed(6);

const loadStats = async () => {
  stats.value = await getMissionStats();
};

onMounted(loadStats);
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

:deep(.van-tabs__nav) {
  background: transparent;
  margin: 0 12px;
  width: calc(100% - 24px);
}
:deep(.van-tabs__content) {
  min-height: 60vh;
}
</style>