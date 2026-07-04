<template>
  <div class="tasks-page">
    <van-nav-bar :title="pageTitle" left-arrow fixed placeholder @click-left="$router.back()" />
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
      <van-tab title="可领取" name="available">
        <TaskList status="available" :task-type="taskType" @changed="loadStats" />
      </van-tab>
      <van-tab title="进行中" name="in_progress">
        <TaskList status="in_progress" :task-type="taskType" @changed="loadStats" />
      </van-tab>
      <van-tab title="审核中" name="submitted">
        <TaskList status="submitted" :task-type="taskType" @changed="loadStats" />
      </van-tab>
      <van-tab title="已完成" name="completed">
        <TaskList status="completed" :task-type="taskType" @changed="loadStats" />
      </van-tab>
      <van-tab title="已驳回" name="rejected">
        <TaskList status="rejected" :task-type="taskType" @changed="loadStats" />
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import TaskList from '@/components/task/TaskList.vue';
import { getMissionStats } from '@/api/mission';
import type { MissionTaskStats, MissionTaskTabStatus } from '@/api/mission';

const route = useRoute();
const defaultStatus = computed<MissionTaskTabStatus>(() =>
  (route.meta.defaultStatus as MissionTaskTabStatus | undefined) || 'available'
);
const activeTab = ref<MissionTaskTabStatus>(defaultStatus.value);
const taskType = computed(() => route.meta.taskType as string | undefined);
const pageTitle = computed(() => (route.meta.title as string) || '任务');
const stats = ref<MissionTaskStats>({
  completedCount: 0,
  inProgressCount: 0,
  totalEarnings: 0,
});

const moneyText = (value?: number) => Number(value || 0).toFixed(6);

const loadStats = async () => {
  stats.value = await getMissionStats(taskType.value);
};

watch(
  () => route.fullPath,
  () => {
    activeTab.value = defaultStatus.value;
    loadStats();
  }
);

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