<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.report', 'menu.report.summary']" />
    <a-card :loading="loading" class="report-card">
      <template #title>综合报表</template>
      <template #extra>
        <a-space :size="12">
          <span class="snapshot-time">
            快照时间：{{ report.generatedAt ? parseTime(report.generatedAt) : '-' }}
          </span>
          <a-button type="primary" @click="loadData">
            <template #icon>
              <icon-refresh />
            </template>
            刷新
          </a-button>
        </a-space>
      </template>

      <div class="summary-grid">
        <div
          v-for="item in overviewCards"
          :key="item.label"
          class="summary-card"
        >
          <div class="summary-label">{{ item.label }}</div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-sub">{{ item.sub }}</div>
        </div>
      </div>

      <a-divider />

      <a-table
        :columns="columns"
        :data="businessRows"
        :pagination="false"
        :scroll="{ x: 1000 }"
      >
        <template #amount="{ record }">
          {{ record.amount }} USDT
        </template>
        <template #todayAmount="{ record }">
          {{ record.todayAmount }} USDT
        </template>
        <template #risk="{ record }">
          <a-tag :color="record.riskColor">{{ record.riskText }}</a-tag>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive, ref } from 'vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { parseTime } from '@/utils/dateUtils';
  import { summaryReport, SummaryReport } from '@/api/report';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'report-summary',
  });

  interface BusinessRow {
    scene: string;

    orders: number;

    amount: string;

    todayOrders: number;

    todayAmount: string;

    riskText: string;

    riskColor: string;
  }

  const emptyReport = (): SummaryReport => ({
    totalUsers: 0,
    todayNewUsers: 0,
    totalBalance: 0,
    availableBalance: 0,
    frozenBalance: 0,
    walletAccounts: 0,
    rechargeOrders: 0,
    rechargeAmount: 0,
    todayRechargeOrders: 0,
    todayRechargeAmount: 0,
    withdrawOrders: 0,
    withdrawAmount: 0,
    reviewingWithdrawOrders: 0,
    todayWithdrawOrders: 0,
    todayWithdrawAmount: 0,
    collectOrders: 0,
    collectedAmount: 0,
    activeCollectOrders: 0,
    todayCollectOrders: 0,
    todayCollectedAmount: 0,
    generatedAt: 0,
  });

  const report = reactive<SummaryReport>(emptyReport());
  const loading = ref(false);

  const columns: TableColumnData[] = [
    {
      title: '业务域',
      dataIndex: 'scene',
      align: 'center',
      width: 160,
    },
    {
      title: '累计订单',
      dataIndex: 'orders',
      align: 'center',
      width: 120,
    },
    {
      title: '累计金额',
      slotName: 'amount',
      align: 'center',
      width: 180,
    },
    {
      title: '今日订单',
      dataIndex: 'todayOrders',
      align: 'center',
      width: 120,
    },
    {
      title: '今日金额',
      slotName: 'todayAmount',
      align: 'center',
      width: 180,
    },
    {
      title: '当前状态',
      slotName: 'risk',
      align: 'center',
      width: 180,
    },
  ];

  const overviewCards = computed(() => [
    {
      label: '会员总数',
      value: intText(report.totalUsers),
      sub: `今日新增 ${intText(report.todayNewUsers)}`,
    },
    {
      label: '钱包账户',
      value: intText(report.walletAccounts),
      sub: '已创建 USDT 钱包账户',
    },
    {
      label: '平台总余额',
      value: `${moneyText(report.totalBalance)} USDT`,
      sub: `可用 ${moneyText(report.availableBalance)}`,
    },
    {
      label: '冻结资金',
      value: `${moneyText(report.frozenBalance)} USDT`,
      sub: `待审核提现 ${intText(report.reviewingWithdrawOrders)} 笔`,
    },
  ]);

  const businessRows = computed<BusinessRow[]>(() => [
    {
      scene: '充值入账',
      orders: report.rechargeOrders,
      amount: moneyText(report.rechargeAmount),
      todayOrders: report.todayRechargeOrders,
      todayAmount: moneyText(report.todayRechargeAmount),
      riskText: '链上确认后自动入账',
      riskColor: 'green',
    },
    {
      scene: '提现出款',
      orders: report.withdrawOrders,
      amount: moneyText(report.withdrawAmount),
      todayOrders: report.todayWithdrawOrders,
      todayAmount: moneyText(report.todayWithdrawAmount),
      riskText: report.reviewingWithdrawOrders
        ? `待审核 ${report.reviewingWithdrawOrders} 笔`
        : '无待审核',
      riskColor: report.reviewingWithdrawOrders ? 'orange' : 'green',
    },
    {
      scene: '平台归集',
      orders: report.collectOrders,
      amount: moneyText(report.collectedAmount),
      todayOrders: report.todayCollectOrders,
      todayAmount: moneyText(report.todayCollectedAmount),
      riskText: report.activeCollectOrders
        ? `进行中 ${report.activeCollectOrders} 笔`
        : '无进行中',
      riskColor: report.activeCollectOrders ? 'arcoblue' : 'green',
    },
  ]);

  const loadData = () => {
    loading.value = true;
    summaryReport()
      .then((rep) => {
        if (rep.success && rep.data) {
          Object.assign(report, rep.data);
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  loadData();

  function intText(value: number): string {
    return Number(value || 0).toLocaleString('en-US');
  }

  function moneyText(value: number): string {
    return Number(value || 0).toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 6,
    });
  }
</script>

<style scoped>
  .report-card {
    min-height: 520px;
  }

  .snapshot-time {
    color: var(--color-text-3);
    font-size: 13px;
  }

  .summary-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 16px;
  }

  .summary-card {
    padding: 18px;
    border: 1px solid var(--color-border-2);
    border-radius: 10px;
    background: linear-gradient(180deg, var(--color-bg-2), var(--color-fill-1));
  }

  .summary-label {
    color: var(--color-text-2);
    font-size: 14px;
  }

  .summary-value {
    margin-top: 10px;
    color: var(--color-text-1);
    font-size: 24px;
    font-weight: 600;
  }

  .summary-sub {
    margin-top: 8px;
    color: var(--color-text-3);
    font-size: 13px;
  }

  @media (max-width: 1200px) {
    .summary-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }
</style>