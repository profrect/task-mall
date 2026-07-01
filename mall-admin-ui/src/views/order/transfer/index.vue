<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.order', 'menu.order.transfer']" />
    <a-card>
      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-input-number
          v-model="filterUserId"
          :min="1"
          :precision="0"
          placeholder="用户ID"
          style="width: 180px"
          allow-clear
          @press-enter="loadData"
        />
        <a-select v-model="filterLimit" style="width: 140px" @change="loadData">
          <a-option :value="100">最近 100 条</a-option>
          <a-option :value="200">最近 200 条</a-option>
          <a-option :value="500">最近 500 条</a-option>
          <a-option :value="1000">最近 1000 条</a-option>
        </a-select>
        <a-button @click="resetFilters">重置</a-button>
        <a-button type="primary" @click="loadData">
          <template #icon>
            <icon-search />
          </template>
          查询
        </a-button>
      </a-space>
      <a-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1300 }"
      >
        <template #amount="{ record }">
          {{ formatAmount(record.amount) }} {{ record.coin }}
        </template>
        <template #status="{ record }">
          <a-tag :color="statusColor(record.status)">
            {{ statusText(record.status) }}
          </a-tag>
        </template>
        <template #finishedAt="{ record }">
          {{ record.finishedAt ? parseTime(record.finishedAt) : '-' }}
        </template>
        <template #createTime="{ record }">
          {{ parseTime(record.createTime) }}
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { parseTime } from '@/utils/dateUtils';
  import { transferOrderList, TransferOrderRecord } from '@/api/transfer';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'transfer-order',
  });

  const columns: TableColumnData[] = [
    {
      title: '订单号',
      dataIndex: 'orderNo',
      align: 'center',
      ellipsis: true,
      tooltip: true,
      width: 190,
    },
    {
      title: '转出用户ID',
      dataIndex: 'fromUserId',
      align: 'center',
      width: 110,
    },
    {
      title: '转入用户ID',
      dataIndex: 'toUserId',
      align: 'center',
      width: 110,
    },
    {
      title: '金额',
      slotName: 'amount',
      align: 'center',
      width: 150,
    },
    {
      title: '状态',
      slotName: 'status',
      align: 'center',
      width: 110,
    },
    {
      title: '备注',
      dataIndex: 'remark',
      align: 'center',
      ellipsis: true,
      tooltip: true,
      width: 220,
    },
    {
      title: '完成时间',
      slotName: 'finishedAt',
      align: 'center',
      width: 170,
    },
    {
      title: '创建时间',
      slotName: 'createTime',
      align: 'center',
      width: 170,
    },
  ];

  const tableData = ref<TransferOrderRecord[]>([]);
  const loading = ref(false);
  const filterUserId = ref<number | undefined>();
  const filterLimit = ref(200);

  const loadData = () => {
    loading.value = true;
    transferOrderList({
      userId: filterUserId.value,
      limit: filterLimit.value,
    })
      .then((rep) => {
        if (rep.success) {
          tableData.value = rep.data;
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  const resetFilters = () => {
    filterUserId.value = undefined;
    filterLimit.value = 200;
    loadData();
  };

  loadData();

  function statusColor(status: string): string {
    if (status === 'SUCCESS') {
      return 'green';
    }
    if (status === 'FAILED') {
      return 'red';
    }
    return 'gray';
  }

  function statusText(status: string): string {
    const map: Record<string, string> = {
      SUCCESS: '成功',
      FAILED: '失败',
    };
    return map[status] || status;
  }

  function formatAmount(value: number): string {
    return parseFloat(Number(value ?? 0).toFixed(6)).toString();
  }
</script>

<style scoped></style>