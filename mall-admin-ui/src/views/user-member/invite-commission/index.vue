<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.user-member', 'menu.user-member.invite-commission']" />
    <a-card>
      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-input-number v-model="query.inviterUserId" placeholder="邀请人ID" style="width: 160px" />
        <a-input-number v-model="query.sourceUserId" placeholder="来源用户ID" style="width: 160px" />
        <a-input v-model="query.keyword" allow-clear placeholder="记录号/订单号/钱包流水" style="width: 240px" />
        <a-select v-model="query.status" allow-clear placeholder="状态" style="width: 150px">
          <a-option value="PENDING">待结算</a-option>
          <a-option value="SETTLED">已结算</a-option>
          <a-option value="SETTLE_FAILED">结算失败</a-option>
        </a-select>
        <a-button type="primary" @click="loadData">查询</a-button>
        <a-button @click="resetQuery">重置</a-button>
      </a-space>

      <a-table
        :columns="columns"
        :data="rows"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1600 }"
        @page-change="onPageChange"
      >
        <template #sourceAmount="{ record }">
          {{ moneyText(record.sourceAmount) }} {{ record.currency }}
        </template>
        <template #commissionRate="{ record }">
          {{ rateText(record.commissionRate) }}
        </template>
        <template #commissionAmount="{ record }">
          <span class="amount">+{{ moneyText(record.commissionAmount) }} {{ record.currency }}</span>
        </template>
        <template #status="{ record }">
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
        <template #walletFlowNo="{ record }">
          {{ record.walletFlowNo || '-' }}
        </template>
        <template #createTime="{ record }">
          {{ record.createTime ? parseTime(record.createTime) : '-' }}
        </template>
        <template #settledAt="{ record }">
          {{ record.settledAt ? parseTime(record.settledAt) : '-' }}
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive, ref } from 'vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { parseTime } from '@/utils/dateUtils';
  import {
    InviteCommissionRecord,
    inviteCommissionPage,
  } from '@/api/inviteCommission';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'invite-commission',
  });

  const query = reactive({
    inviterUserId: undefined as number | undefined,
    sourceUserId: undefined as number | undefined,
    keyword: '',
    status: undefined as string | undefined,
    pageNumber: 1,
    pageSize: 10,
  });

  const rows = ref<InviteCommissionRecord[]>([]);
  const loading = ref(false);
  const total = ref(0);

  const columns: TableColumnData[] = [
    { title: '记录号', dataIndex: 'recordNo', align: 'center', width: 210, ellipsis: true, tooltip: true },
    { title: '邀请人ID', dataIndex: 'inviterUserId', align: 'center', width: 120 },
    { title: '来源用户ID', dataIndex: 'sourceUserId', align: 'center', width: 120 },
    { title: '来源订单', dataIndex: 'sourceOrderNo', align: 'center', width: 210, ellipsis: true, tooltip: true },
    { title: '触发业务', dataIndex: 'businessType', align: 'center', width: 130 },
    { title: '订单金额', slotName: 'sourceAmount', align: 'center', width: 150 },
    { title: '返佣比例', slotName: 'commissionRate', align: 'center', width: 120 },
    { title: '返佣金额', slotName: 'commissionAmount', align: 'center', width: 160 },
    { title: '状态', slotName: 'status', align: 'center', width: 120 },
    { title: '钱包流水', slotName: 'walletFlowNo', align: 'center', width: 190, ellipsis: true, tooltip: true },
    { title: '失败原因', dataIndex: 'failReason', align: 'center', width: 220, ellipsis: true, tooltip: true },
    { title: '创建时间', slotName: 'createTime', align: 'center', width: 170 },
    { title: '结算时间', slotName: 'settledAt', align: 'center', width: 170 },
  ];

  const pagination = computed(() => ({
    current: query.pageNumber,
    pageSize: query.pageSize,
    total: total.value,
    showTotal: true,
  }));

  const loadData = () => {
    loading.value = true;
    inviteCommissionPage(query)
      .then((rep) => {
        if (rep.success) {
          rows.value = rep.data?.records || [];
          total.value = rep.data?.totalRow || 0;
          query.pageNumber = rep.data?.pageNumber || query.pageNumber;
          query.pageSize = rep.data?.pageSize || query.pageSize;
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  const resetQuery = () => {
    query.inviterUserId = undefined;
    query.sourceUserId = undefined;
    query.keyword = '';
    query.status = undefined;
    query.pageNumber = 1;
    loadData();
  };

  const onPageChange = (pageNumber: number) => {
    query.pageNumber = pageNumber;
    loadData();
  };

  function statusText(status: string): string {
    const map: Record<string, string> = {
      PENDING: '待结算',
      SETTLED: '已结算',
      SETTLE_FAILED: '结算失败',
    };
    return map[status] || status;
  }

  function statusColor(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'orange',
      SETTLED: 'green',
      SETTLE_FAILED: 'red',
    };
    return map[status] || 'gray';
  }

  function moneyText(amount: number): string {
    return parseFloat(Number(amount || 0).toFixed(6)).toString();
  }

  function rateText(rate: number): string {
    return `${parseFloat((Number(rate || 0) * 100).toFixed(4))}%`;
  }

  loadData();
</script>

<style scoped>
  .amount {
    color: #00b42a;
    font-weight: 600;
  }
</style>