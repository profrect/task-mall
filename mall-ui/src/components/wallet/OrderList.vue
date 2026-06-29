<template>
  <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
    <van-list
      v-model:loading="loading"
      :finished="finished"
      finished-text="No more records"
      @load="onLoad"
    >
      <div class="list-wrapper">
        <OrderItem v-for="item in list" :key="item.id" :order="item" :type="type" />
      </div>
      <van-empty v-if="!loading && !list.length" description="No records" image="search" />
    </van-list>
  </van-pull-refresh>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import OrderItem from './OrderItem.vue';

const props = defineProps<{
  type: 'flow' | 'deposit' | 'withdraw' | 'transfer' | 'payment'
}>();

const list = ref<any[]>([]);
const loading = ref(false);
const finished = ref(false);
const refreshing = ref(false);
const pageNum = ref(1);

const onLoad = async () => {
  try {
    // TODO: 替换为 /api/wallet/orders?type=x&page=x&size=15
    // await new Promise(r => setTimeout(r, 600));
  //   const statuses = ['success', 'processing', 'failed'];
  //   const mockData = Array.from({ length: 15 }, (_, i) => ({
  //     id: props.type-pageNum.value-{i},
  //     amount: +(Math.random() * 300 + 10).toFixed(2),
  //     status: statuses[i % 3],
  //     time: '2026-06-24 14:30',
  //     // flow 和 payment 需要方向字段，其他类型由 type 本身决定方向
  //     direction: (props.type === 'flow' || props.type === 'payment')
  //       ? (i % 2 === 0 ? 'in' : 'out')
  //       : undefined,
  //     counterparty: props.type === 'transfer' ? User_{1000 + i} : undefined,
  //     payMethod: props.type === 'payment' ? ['USDT-TRC20', 'Bank Wire'][i % 2] : undefined
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
</script>

<style scoped>
.list-wrapper {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
