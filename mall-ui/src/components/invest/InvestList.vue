<template>
  <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="No more records"
      @load="onLoad"
    >
      <div class="list-wrapper">
        <InvestItem v-for="item in list" :key="item.id" :plan="item" @action="handleAction(item)" />
      </div>

      <van-empty v-if="!loading && !list.length" description="No data available" image="search" />
    </van-list>
  </van-pull-refresh>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import InvestItem from './InvestItem.vue';

const props = defineProps<{ status: 'available' | 'in_progress' | 'history' }>();

const list = ref<any[]>([]);
const loading = ref(false);
const finished = ref(false);
const refreshing = ref(false);
const pageNum = ref(1);
const pageSize = 10;

const onLoad = async () => {
  try {
    // TODO: 替换为真实API /api/invest/list?status=x&page=x&size=10
    await new Promise(r => setTimeout(r, 600));

    // const mockData = Array.from({ length: pageSize }, (_, i) => ({
    //   id: {props.status}-{pageNum.value}-{i},
    //   name: {props.status} Plan {String.fromCharCode(65 + i)},
    //   dailyRate: +(Math.random() * 2 + 0.5).toFixed(2),
    //   cycle: [7, 15, 30, 90][i % 4],
    //   amount: +(Math.random() * 1000 + 100).toFixed(0),
    //   status: props.status,
    //   progress: props.status === 'in_progress' ? Math.floor(Math.random() * 100) : undefined,
    //   profit: props.status === 'history' ? +(Math.random() * 50).toFixed(2) : undefined,
    //   riskLevel: ['Low', 'Medium'][i % 2]
    // }));

    if (refreshing.value) {
      list.value = [];
      refreshing.value = false;
    }

    // list.value.push(...mockData);
    pageNum.value++;
    if (pageNum.value > 5) finished.value = true;
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

const handleAction = (item: any) => {
  console.log('Invest action:', props.status, item.id);
  // TODO: available -> 弹出认购弹窗; in_progress -> 查看详情; history -> 无操作或查看凭证
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
