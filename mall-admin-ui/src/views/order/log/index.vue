<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.order', 'menu.order.log']" />
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
        :scroll="{ x: 1500 }"
      >
        <template #biz="{ record }">
          <a-tag color="arcoblue">{{ bizText(record.bizType) }}</a-tag>
        </template>
        <template #direction="{ record }">
          <a-tag :color="directionColor(record.direction)">
            {{ directionText(record.direction) }}
          </a-tag>
        </template>
        <template #amount="{ record }">
          <span :class="['amount', directionClass(record.direction)]">
            {{ amountPrefix(record.direction) }}{{ formatAmount(record.changeAmt) }}
          </span>
        </template>
        <template #balance="{ record }">
          {{ formatAmount(record.balanceBefore) }} → {{ formatAmount(record.balanceAfter) }}
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
  import { walletFlowList, WalletFlowRecord } from '@/api/walletFlow';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'wallet-flow-log',
  });

  const columns: TableColumnData[] = [
    {
      title: '流水号',
      dataIndex: 'flowNo',
      align: 'center',
      ellipsis: true,
      tooltip: true,
      width: 180,
    },
    {
      title: '用户ID',
      dataIndex: 'userId',
      align: 'center',
      width: 90,
    },
    {
      title: '业务类型',
      slotName: 'biz',
      align: 'center',
      width: 130,
    },
    {
      title: '业务单号',
      dataIndex: 'bizId',
      align: 'center',
      ellipsis: true,
      tooltip: true,
      width: 180,
    },
    {
      title: '方向',
      slotName: 'direction',
      align: 'center',
      width: 110,
    },
    {
      title: '变动金额',
      slotName: 'amount',
      align: 'center',
      width: 130,
    },
    {
      title: '可用余额变化',
      slotName: 'balance',
      align: 'center',
      width: 220,
    },
    {
      title: '备注',
      dataIndex: 'remark',
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
  ];

  const tableData = ref<WalletFlowRecord[]>([]);
  const loading = ref(false);
  const filterUserId = ref<number | undefined>();
  const filterLimit = ref(200);

  const loadData = () => {
    loading.value = true;
    walletFlowList({
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

  function directionClass(direction: string): string {
    if (direction === 'IN' || direction === 'UNFREEZE') {
      return 'in';
    }
    return 'out';
  }

  function amountPrefix(direction: string): string {
    return directionClass(direction) === 'in' ? '+' : '-';
  }

  function directionColor(direction: string): string {
    return directionClass(direction) === 'in' ? 'green' : 'orange';
  }

  function directionText(direction: string): string {
    const map: Record<string, string> = {
      IN: '入账',
      OUT: '出账',
      FREEZE: '冻结',
      UNFREEZE: '解冻',
    };
    return map[direction] || direction;
  }

  function bizText(bizType: string): string {
    const map: Record<string, string> = {
      RECHARGE: '充值入账',
      WITHDRAW_FREEZE: '提现冻结',
      WITHDRAW_REJECT: '提现退回',
      WITHDRAW_SETTLE: '提现出款',
      WITHDRAW_FEE: '提现手续费',
    };
    return map[bizType] || bizType;
  }

  function formatAmount(value: number): string {
    return parseFloat(Number(value ?? 0).toFixed(6)).toString();
  }
</script>

<style scoped>
  .amount {
    font-weight: 600;
  }

  .amount.in {
    color: #00b42a;
  }

  .amount.out {
    color: #ff7d00;
  }
</style>