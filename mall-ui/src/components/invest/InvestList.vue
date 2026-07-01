<template>
  <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
    <van-list :loading="loading" :finished="true" finished-text="No more records">
      <div class="list-wrapper">
        <InvestItem v-for="item in projects" :key="item.id" :plan="item" />
      </div>

      <van-empty v-if="!loading && !projects.length" description="No data available" image="search" />
    </van-list>
  </van-pull-refresh>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { MissionInvestProject } from '@/api/mission';
import InvestItem from './InvestItem.vue';

const props = defineProps<{
  projects: MissionInvestProject[];
  loading: boolean;
}>();

const emit = defineEmits<{ refresh: [] }>();
const refreshing = ref(false);

const onRefresh = () => {
  emit('refresh');
  refreshing.value = false;
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