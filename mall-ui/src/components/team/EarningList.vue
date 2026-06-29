<template>
  <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="No more earnings"
      @load="onLoad"
    >
      <div class="list-wrapper">
        <EarningItem v-for="e in list" :key="e.id" :earning="e" />
      </div>
      <van-empty v-if="!loading && !list.length" description="No earnings yet" image="search" />
    </van-list>
  </van-pull-refresh>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import EarningItem from './EarningItem.vue';

const list = ref<any[]>([]);
const loading = ref(false);
const finished = ref(false);
const refreshing = ref(false);
const pageNum = ref(1);

const onLoad = async () => {
  try {
    // TODO: 替换为 /api/team/earnings?page=x&size=15
  //   await new Promise(r => setTimeout(r, 600));
  //   const types = ['direct_reward', 'level_bonus', 'team_dividend'];
  //   const mockData = Array.from({ length: 15 }, (_, i) => ({
  //     id: e-{pageNum.value}-{i},
  //     type: types[i % 3],
  //     amount: +(Math.random() * 50 + 1).toFixed(2),
  //     source: User_{2000 + i},
  //   time: '2026-06-24 14:30'
  // }));
    if (refreshing.value) { list.value = []; refreshing.value = false; }
    // list.value.push(...mockData);
    pageNum.value++;
    if (pageNum.value > 5) finished.value = true;
  } finally { loading.value = false; }
};

const onRefresh = () => { pageNum.value = 1; finished.value = false; loading.value = true; onLoad(); };
</script>

<style scoped>
.list-wrapper {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
