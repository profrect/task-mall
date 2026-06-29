<template>
  <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="No more tasks"
      @load="onLoad"
    >
      <div class="list-wrapper">
        <TaskItem v-for="task in list" :key="task.id" :task="task" @action="handleAction(task)" />
      </div>

      <!-- 列表为空时的兜底 -->
      <van-empty v-if="!loading && !list.length" description="No tasks here" image="search" />
    </van-list>
  </van-pull-refresh>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import TaskItem from './TaskItem.vue';
// TODO: import { getTaskList } from '@/api/task';

const props = defineProps<{ status: 'available' | 'in_progress' | 'completed' }>();

const list = ref<any[]>([]);
const loading = ref(false);
const finished = ref(false);
const refreshing = ref(false);
const pageNum = ref(1);

const onLoad = async () => {
  try {
    // TODO: 替换为真实API调用
    // const res = await getTaskList({ status: props.status, page: pageNum.value, size: pageSize });

    // --- 模拟数据开始 (实际开发请删除) ---
  //   await new Promise(r => setTimeout(r, 800));
  //   const mockData = Array.from({ length: pageSize }, (_, i) => ({
  //     id: {props.status}-{pageNum.value}-{i},
  //     title: {props.status} Task Mock {pageNum.value}-{i},
  //     reward: +(Math.random() * 5).toFixed(2),
  //     status: props.status,
  //     progress: props.status === 'in_progress' ? Math.floor(Math.random() * 100) : undefined,
  //     icon: 'todo-list-o',
  //     iconBg: '#999'
  // }));
    // --- 模拟数据结束 ---

    if (refreshing.value) {
      list.value = [];
      refreshing.value = false;
    }

    // list.value.push(...mockData);
    pageNum.value++;

    // 模拟最后一页
    if (pageNum.value > 3) {
      finished.value = true;
    }
  } catch (e) {
    finished.value = true; // 异常时停止加载防止死循环
  } finally {
    loading.value = false;
  }
};

const onRefresh = () => {
  pageNum.value = 1;
  finished.value = false;
  loading.value = true;
  onLoad();
};

const handleAction = (task: any) => {
  console.log('Action:', task.status, task.id);
  // TODO: 根据 task.status 执行领取、跳转或提示已完成
};
</script>

<style scoped>
.list-wrapper {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>
