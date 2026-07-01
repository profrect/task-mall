<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.order', 'menu.order.payment']" />
    <a-card>
      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-input-number
          v-model="query.userId"
          :min="1"
          :precision="0"
          allow-clear
          placeholder="用户ID"
          style="width: 160px"
          @press-enter="loadData"
        />
        <a-input
          v-model="query.orderNo"
          allow-clear
          placeholder="支付审计单号"
          style="width: 220px"
          @press-enter="loadData"
        />
        <a-select v-model="query.businessType" allow-clear placeholder="业务类型" style="width: 160px">
          <a-option value="RECHARGE">充值</a-option>
          <a-option value="VIP_UPGRADE">VIP升级</a-option>
          <a-option value="OTHER">其他</a-option>
        </a-select>
        <a-select v-model="query.channelCode" allow-clear placeholder="支付通道" style="width: 160px">
          <a-option value="TRON">TRON</a-option>
          <a-option value="ETH">ETH</a-option>
          <a-option value="BSC">BSC</a-option>
          <a-option value="POLYGON">POLYGON</a-option>
          <a-option value="MANUAL">MANUAL</a-option>
        </a-select>
        <a-select v-model="query.status" allow-clear placeholder="状态" style="width: 150px">
          <a-option value="CREATED">已创建</a-option>
          <a-option value="PENDING">待支付</a-option>
          <a-option value="PAID">已支付</a-option>
          <a-option value="CLOSED">已关闭</a-option>
          <a-option value="FAILED">失败</a-option>
        </a-select>
        <a-button type="primary" @click="loadData">查询</a-button>
        <a-button @click="resetQuery">重置</a-button>
      </a-space>

      <a-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1900 }"
        @page-change="onPageChange"
      >
        <template #businessType="{ record }">
          <a-tag color="arcoblue">{{ businessText(record.businessType) }}</a-tag>
        </template>
        <template #amount="{ record }">
          {{ moneyText(record.amount) }} {{ record.currency }}
        </template>
        <template #status="{ record }">
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
        <template #channel="{ record }">
          {{ record.channelCode }} / {{ record.channelOrderNo || '-' }}
        </template>
        <template #txHash="{ record }">
          {{ record.txHash || '-' }}
        </template>
        <template #paidAt="{ record }">
          {{ record.paidAt ? parseTime(record.paidAt) : '-' }}
        </template>
        <template #expiredAt="{ record }">
          {{ record.expiredAt ? parseTime(record.expiredAt) : '-' }}
        </template>
        <template #createTime="{ record }">
          {{ record.createTime ? parseTime(record.createTime) : '-' }}
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive, ref } from 'vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { parseTime } from '@/utils/dateUtils';
  import { PaymentOrderRecord, paymentOrderPage } from '@/api/payment';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'payment-order',
  });

  const query = reactive({
    userId: undefined as number | undefined,
    orderNo: '',
    businessType: undefined as string | undefined,
    channelCode: undefined as string | undefined,
    status: undefined as string | undefined,
    pageNumber: 1,
    pageSize: 10,
  });

  const columns: TableColumnData[] = [
    { title: '审计单号', dataIndex: 'orderNo', align: 'center', width: 200, ellipsis: true, tooltip: true },
    { title: '用户ID', dataIndex: 'userId', align: 'center', width: 110 },
    { title: '业务类型', slotName: 'businessType', align: 'center', width: 130 },
    { title: '业务单号', dataIndex: 'businessOrderNo', align: 'center', width: 180, ellipsis: true, tooltip: true },
    { title: '金额', slotName: 'amount', align: 'center', width: 150 },
    { title: '状态', slotName: 'status', align: 'center', width: 120 },
    { title: '通道/通道单号', slotName: 'channel', align: 'center', width: 220, ellipsis: true, tooltip: true },
    { title: '收款地址', dataIndex: 'payAddress', align: 'center', width: 220, ellipsis: true, tooltip: true },
    { title: '付款地址', dataIndex: 'payerAddress', align: 'center', width: 220, ellipsis: true, tooltip: true },
    { title: '交易哈希/凭证', slotName: 'txHash', align: 'center', width: 220, ellipsis: true, tooltip: true },
    { title: '审计备注', dataIndex: 'auditRemark', align: 'center', width: 220, ellipsis: true, tooltip: true },
    { title: '支付时间', slotName: 'paidAt', align: 'center', width: 170 },
    { title: '过期时间', slotName: 'expiredAt', align: 'center', width: 170 },
    { title: '创建时间', slotName: 'createTime', align: 'center', width: 170 },
  ];

  const tableData = ref<PaymentOrderRecord[]>([]);
  const loading = ref(false);
  const total = ref(0);

  const pagination = computed(() => ({
    current: query.pageNumber,
    pageSize: query.pageSize,
    total: total.value,
    showTotal: true,
  }));

  const loadData = () => {
    loading.value = true;
    paymentOrderPage({
      ...query,
      orderNo: query.orderNo || undefined,
    })
      .then((rep) => {
        if (rep.success) {
          tableData.value = rep.data?.records || [];
          total.value = rep.data?.totalRow || 0;
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  const resetQuery = () => {
    Object.assign(query, {
      userId: undefined,
      orderNo: '',
      businessType: undefined,
      channelCode: undefined,
      status: undefined,
      pageNumber: 1,
    });
    loadData();
  };

  const onPageChange = (page: number) => {
    query.pageNumber = page;
    loadData();
  };

  const businessText = (type: string) => {
    const map: Record<string, string> = {
      RECHARGE: '充值',
      VIP_UPGRADE: 'VIP升级',
      OTHER: '其他',
    };
    return map[type] || type;
  };

  const statusText = (status: string) => {
    const map: Record<string, string> = {
      CREATED: '已创建',
      PENDING: '待支付',
      PAID: '已支付',
      CLOSED: '已关闭',
      FAILED: '失败',
    };
    return map[status] || status;
  };

  const statusColor = (status: string) => {
    const map: Record<string, string> = {
      CREATED: 'gray',
      PENDING: 'orange',
      PAID: 'green',
      CLOSED: 'gray',
      FAILED: 'red',
    };
    return map[status] || 'gray';
  };

  const moneyText = (value?: number) => Number(value || 0).toFixed(6);

  loadData();
</script>

<style scoped></style>