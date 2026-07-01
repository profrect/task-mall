<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.order', 'menu.order.withdraw']" />
    <a-card>
      <a-space :size="18" style="margin-bottom: 16px">
        <a-button type="primary" @click="loadData">
          <template #icon>
            <icon-refresh />
          </template>
          刷新
        </a-button>
        <a-tag color="arcoblue">待审核 {{ tableData.length }} 笔</a-tag>
      </a-space>
      <a-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1300 }"
      >
        <template #chain="{ record }">
          {{ record.chainCode }} / {{ record.coin }}
        </template>
        <template #createTime="{ record }">
          {{ parseTime(record.createTime) }}
        </template>
        <template #operations="{ record }">
          <a-popconfirm content="确认通过并立即出款？" @ok="doApprove(record)">
            <a-button type="text" size="mini" status="success">通过</a-button>
          </a-popconfirm>
          <a-button
            type="text"
            size="mini"
            status="danger"
            @click="openReject(record)"
          >
            驳回
          </a-button>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="rejectVisible"
      title="驳回提现"
      @ok="doReject"
      @cancel="rejectVisible = false"
    >
      <a-form :model="rejectForm" layout="vertical">
        <a-form-item label="订单号">
          <a-input v-model="rejectForm.orderNo" disabled />
        </a-form-item>
        <a-form-item label="驳回原因">
          <a-textarea
            v-model="rejectForm.remark"
            placeholder="请输入驳回原因（将随退款一并记录）"
            :auto-size="{ minRows: 3 }"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { Message } from '@arco-design/web-vue';
  import { parseTime } from '@/utils/dateUtils';
  import {
    reviewingList,
    approveWithdraw,
    rejectWithdraw,
    WithdrawOrder,
  } from '@/api/withdraw';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'withdraw-order',
  });

  const columns: TableColumnData[] = [
    {
      title: '订单号',
      dataIndex: 'orderNo',
      align: 'center',
      width: 190,
    },
    {
      title: '用户ID',
      dataIndex: 'userId',
      align: 'center',
      width: 90,
    },
    {
      title: '链/币种',
      slotName: 'chain',
      align: 'center',
      width: 120,
    },
    {
      title: '提现金额',
      dataIndex: 'amount',
      align: 'center',
      width: 110,
    },
    {
      title: '手续费',
      dataIndex: 'fee',
      align: 'center',
      width: 90,
    },
    {
      title: '到账金额',
      dataIndex: 'receiveAmount',
      align: 'center',
      width: 110,
    },
    {
      title: '收款地址',
      dataIndex: 'toAddress',
      align: 'center',
      ellipsis: true,
      tooltip: true,
      width: 200,
    },
    {
      title: '申请时间',
      slotName: 'createTime',
      align: 'center',
      width: 170,
    },
    {
      title: '操作',
      slotName: 'operations',
      align: 'center',
      width: 150,
      fixed: 'right',
    },
  ];

  const tableData = ref<WithdrawOrder[]>([]);
  const loading = ref(false);
  const rejectVisible = ref(false);
  const rejectForm = ref({ orderNo: '', remark: '' });

  const loadData = () => {
    loading.value = true;
    reviewingList()
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

  function doApprove(record: WithdrawOrder) {
    approveWithdraw({ orderNo: record.orderNo }).then((rep) => {
      if (rep.success) {
        Message.success('已通过并出款');
        loadData();
      }
    });
  }

  function openReject(record: WithdrawOrder) {
    rejectForm.value = { orderNo: record.orderNo, remark: '' };
    rejectVisible.value = true;
  }

  function doReject() {
    rejectWithdraw(rejectForm.value).then((rep) => {
      if (rep.success) {
        Message.success('已驳回并退款');
        rejectVisible.value = false;
        loadData();
      }
    });
  }
</script>