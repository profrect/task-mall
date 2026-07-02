<template>
  <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
    <van-list v-model:loading="loading" :finished="finished" finished-text="No more tasks" @load="onLoad">
      <div class="list-wrapper">
        <TaskItem v-for="task in list" :key="itemKey(task)" :task="task" @action="handleAction(task)" />
      </div>
      <van-empty v-if="!loading && !list.length" description="No tasks here" image="search" />
    </van-list>
  </van-pull-refresh>

  <van-dialog
    v-model:show="submitVisible"
    title="Submit task proof"
    show-cancel-button
    :before-close="beforeSubmitClose"
  >
    <van-field
      v-model="submitContent"
      rows="4"
      autosize
      type="textarea"
      maxlength="1000"
      show-word-limit
      placeholder="Describe your task proof"
    />
  </van-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { showSuccessToast, showToast } from 'vant';
import {
  MissionTaskItem,
  claimMissionTask,
  getMissionTasks,
  submitMissionTask,
} from '@/api/mission';
import { rejectIfImpersonated } from '@/utils/impersonation';
import TaskItem from './TaskItem.vue';

const props = defineProps<{ status: 'available' | 'in_progress' | 'completed' }>();
const emit = defineEmits<{ changed: [] }>();

const list = ref<MissionTaskItem[]>([]);
const loading = ref(false);
const finished = ref(false);
const refreshing = ref(false);
const submitVisible = ref(false);
const submitContent = ref('');
const activeTask = ref<MissionTaskItem | null>(null);

const itemKey = (task: MissionTaskItem) => `${task.recordId || 0}-${task.taskId}`;

const loadList = async () => {
  const data = await getMissionTasks(props.status, 50);
  list.value = data || [];
  finished.value = true;
};

const onLoad = async () => {
  try {
    if (refreshing.value) {
      list.value = [];
      refreshing.value = false;
    }
    await loadList();
  } finally {
    loading.value = false;
  }
};

const onRefresh = () => {
  finished.value = false;
  loading.value = true;
  onLoad();
};

const handleAction = async (task: MissionTaskItem) => {
  if (props.status === 'available') {
    if (rejectIfImpersonated()) {
      return;
    }
    await claimMissionTask(task.taskId);
    showSuccessToast('Task claimed');
    await loadList();
    emit('changed');
    return;
  }
  if (props.status === 'in_progress') {
    activeTask.value = task;
    submitContent.value = task.submitContent || '';
    submitVisible.value = true;
  }
};

const beforeSubmitClose = async (action: string) => {
  if (action !== 'confirm') return true;
  if (rejectIfImpersonated()) return false;
  if (!activeTask.value?.recordId) return true;
  if (!submitContent.value.trim()) {
    showToast('Please enter proof');
    return false;
  }
  await submitMissionTask(activeTask.value.recordId, submitContent.value.trim());
  showSuccessToast('Submitted for review');
  await loadList();
  emit('changed');
  return true;
};

watch(
  () => props.status,
  () => {
    onRefresh();
  }
);
</script>

<style scoped>
.list-wrapper {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>