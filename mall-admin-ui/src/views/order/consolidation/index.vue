<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.order', 'menu.order.consolidation']" />
    <a-card>
      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-select v-model="filterStatus" style="width: 160px" @change="loadData">
          <a-option value="ACTIVE">进行中</a-option>
          <a-option value="CREATED">待处理</a-option>
          <a-option value="GAS_FUNDING">喂 gas 中</a-option>
          <a-option value="SWEEPING">归集中</a-option>
          <a-option value="COMPLETED">已完成</a-option>
          <a-option value="FAILED">失败</a-option>
        </a-select>
        <a-button @click="loadData">
          <template #icon>
            <icon-refresh />
          </template>
          刷新
        </a-button>
        <a-divider direction="vertical" />
        <a-select
          v-model="scanChain"
          placeholder="扫描链（默认全部）"
          style="width: 170px"
          allow-clear
        >
          <a-option value="TRON">TRON</a-option>
          <a-option value="ETH">ETH</a-option>
          <a-option value="BSC">BSC</a-option>
          <a-option value="POLYGON">POLYGON</a-option>
        </a-select>
        <a-button status="warning" :loading="scanning" @click="doScan">
          扫描建单
        </a-button>
        <a-popconfirm
          content="推进所有在途归集订单一轮（喂 gas / 广播 / 确认）？"
          @ok="doAdvance"
        >
          <a-button status="danger" :loading="advancing">推进一轮</a-button>
        </a-popconfirm>
      </a-space>
      <a-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1600 }"
      >
        <template #status="{ record }">
          <a-tag :color="statusColor(record.status)">
            {{ statusText(record.status) }}
          </a-tag>
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
  import { Message } from '@arco-design/web-vue';
  import { parseTime } from '@/utils/dateUtils';
  import {
    activeCollectList,
    collectListByStatus,
    scanCollect,
    advanceCollect,
    CollectOrder,
  } from '@/api/collect';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'consolidation-order',
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
      title: '链',
      dataIndex: 'chainCode',
      align: 'center',
      width: 80,
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
      title: '快照金额',
      dataIndex: 'amount',
      align: 'center',
      width: 120,
    },
    {
      title: '已归集',
      dataIndex: 'sweptAmount',
      align: 'center',
      width: 120,
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
      width: 110,
    },
    {
      title: '归集交易',
      dataIndex: 'sweepTxHash',
      align: 'center',
      ellipsis: true,
      tooltip: true,
      width: 180,
    },
    {
      title: '创建时间',
      slotName: 'createTime',
      align: 'center',
      width: 170,
    },
    {
      title: '备注',
      dataIndex: 'remark',
      align: 'center',
      ellipsis: true,
      tooltip: true,
      width: 160,
    },
  ];

  const tableData = ref<CollectOrder[]>([]);
  const loading = ref(false);
  const scanning = ref(false);
  const advancing = ref(false);
  const filterStatus = ref('ACTIVE');
  const scanChain = ref('');

  const loadData = () => {
    loading.value = true;
    const req =
      filterStatus.value === 'ACTIVE'
        ? activeCollectList()
        : collectListByStatus(filterStatus.value);
    req
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

  function doScan() {
    scanning.value = true;
    scanCollect(scanChain.value || undefined)
      .then((rep) => {
        if (rep.success) {
          Message.success(`扫描完成，新建 ${rep.data} 笔归集单`);
          loadData();
        }
      })
      .finally(() => {
        scanning.value = false;
      });
  }

  function doAdvance() {
    advancing.value = true;
    advanceCollect()
      .then((rep) => {
        if (rep.success) {
          Message.success('已推进一轮');
          loadData();
        }
      })
      .finally(() => {
        advancing.value = false;
      });
  }

  function statusColor(status: string): string {
    switch (status) {
      case 'COMPLETED':
        return 'green';
      case 'FAILED':
        return 'red';
      case 'SWEEPING':
      case 'GAS_FUNDING':
        return 'arcoblue';
      default:
        return 'gray';
    }
  }

  function statusText(status: string): string {
    const map: Record<string, string> = {
      CREATED: '待处理',
      GAS_FUNDING: '喂 gas 中',
      SWEEPING: '归集中',
      COMPLETED: '已完成',
      FAILED: '失败',
    };
    return map[status] || status;
  }
</script>