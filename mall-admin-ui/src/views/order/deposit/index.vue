<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.order', 'menu.order.deposit']" />
    <a-card>
      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-select
          v-model="filterStatus"
          style="width: 160px"
          @change="loadData"
        >
          <a-option value="">全部</a-option>
          <a-option value="CONFIRMING">确认中</a-option>
          <a-option value="CREDITED">已入账</a-option>
        </a-select>
        <a-button @click="loadData">
          <template #icon>
            <icon-refresh />
          </template>
          刷新
        </a-button>
      </a-space>
      <a-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1700 }"
      >
        <template #amount="{ record }">
          {{ record.amount }} {{ record.coin }}
        </template>
        <template #status="{ record }">
          <a-tag :color="statusColor(record.status)">
            {{ statusText(record.status) }}
          </a-tag>
        </template>
        <template #creditedAt="{ record }">
          {{ record.creditedAt ? parseTime(record.creditedAt) : '-' }}
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
  import { rechargeList, RechargeOrder } from '@/api/recharge';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'deposit-order',
  });

  const columns: TableColumnData[] = [
    {
      title: '订单号',
      dataIndex: 'orderNo',
      align: 'center',
      width: 200,
    },
    {
      title: '用户ID',
      dataIndex: 'userId',
      align: 'center',
      width: 90,
    },
    {
      title: '链',
      dataIndex: 'chainCode',
      align: 'center',
      width: 80,
    },
    {
      title: '金额',
      slotName: 'amount',
      align: 'center',
      width: 150,
    },
    {
      title: '来源地址',
      dataIndex: 'fromAddress',
      align: 'center',
      ellipsis: true,
      tooltip: true,
      width: 180,
    },
    {
      title: '收款地址',
      dataIndex: 'toAddress',
      align: 'center',
      ellipsis: true,
      tooltip: true,
      width: 180,
    },
    {
      title: '交易哈希',
      dataIndex: 'txHash',
      align: 'center',
      ellipsis: true,
      tooltip: true,
      width: 180,
    },
    {
      title: '确认数',
      dataIndex: 'confirmations',
      align: 'center',
      width: 90,
    },
    {
      title: '状态',
      slotName: 'status',
      align: 'center',
      width: 100,
    },
    {
      title: '入账时间',
      slotName: 'creditedAt',
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

  const tableData = ref<RechargeOrder[]>([]);
  const loading = ref(false);
  const filterStatus = ref('');

  const loadData = () => {
    loading.value = true;
    rechargeList(filterStatus.value || undefined)
      .then((rep) => {
        if (rep.success) {
          tableData.value = rep.data;
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  loadData();

  function statusColor(status: string): string {
    switch (status) {
      case 'CREDITED':
        return 'green';
      case 'CONFIRMING':
        return 'arcoblue';
      default:
        return 'gray';
    }
  }

  function statusText(status: string): string {
    const map: Record<string, string> = {
      CONFIRMING: '确认中',
      CREDITED: '已入账',
    };
    return map[status] || status;
  }
</script>

<style scoped></style>