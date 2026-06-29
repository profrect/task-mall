<template>
  <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="No more members"
      @load="onLoad"
    >
      <div class="list-wrapper">
        <MemberItem v-for="m in list" :key="m.id" :member="m" />
      </div>
      <van-empty v-if="!loading && !list.length" description="No subordinates yet" image="search" />
    </van-list>
  </van-pull-refresh>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import MemberItem from './MemberItem.vue';

const list = ref<any[]>([]);
const loading = ref(false);
const finished = ref(false);
const refreshing = ref(false);
const pageNum = ref(1);

const onLoad = async () => {
  try {
    // TODO: 替换为 /api/team/subordinates?page=x&size=15
  //   await new Promise(r => setTimeout(r, 600));
  //   const mockData = Array.from({ length: 15 }, (_, i) => ({
  //     id: m-{pageNum.value}-{i},
  //     nickname: User_{1000 + pageNum.value * 15 + i},
  //   level: Math.floor(Math.random() * 5) + 1,
  //     joinTime: '2026-06-20',
  //     contribution: +(Math.random() * 200 + 10).toFixed(2),
  //     status: i % 7 === 0 ? 'inactive' : 'active'
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
