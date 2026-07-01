<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.leaderboard', 'menu.leaderboard.index']" />
    <a-card>
      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-radio-group v-model="query.type" type="button" @change="loadData">
          <a-radio value="EARNING">收益榜</a-radio>
          <a-radio value="RECHARGE">充值榜</a-radio>
          <a-radio value="TASK">任务榜</a-radio>
        </a-radio-group>
        <a-input-number
          v-model="query.startTime"
          :min="0"
          :precision="0"
          placeholder="开始时间戳"
          style="width: 180px"
          allow-clear
        />
        <a-input-number
          v-model="query.endTime"
          :min="0"
          :precision="0"
          placeholder="结束时间戳"
          style="width: 180px"
          allow-clear
        />
        <a-select v-model="query.limit" style="width: 140px" @change="loadData">
          <a-option :value="10">Top 10</a-option>
          <a-option :value="20">Top 20</a-option>
          <a-option :value="50">Top 50</a-option>
          <a-option :value="100">Top 100</a-option>
        </a-select>
        <a-button @click="resetQuery">重置</a-button>
        <a-button type="primary" @click="loadData">
          <template #icon>
            <icon-refresh />
          </template>
          刷新
        </a-button>
      </a-space>

      <a-alert type="normal" class="scope-alert">
        当前榜单来自真实业务聚合：充值/收益基于钱包入账流水，任务基于已审核通过任务记录；无记录时展示空列表。
      </a-alert>

      <a-table
        :columns="columns"
        :data="rows"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1100 }"
      >
        <template #rank="{ record }">
          <a-tag :color="rankColor(record.rankNo)">#{{ record.rankNo }}</a-tag>
        </template>
        <template #user="{ record }">
          <div class="user-cell">
            <span>{{ record.displayName || `User ${record.userId}` }}</span>
            <span class="user-id">ID: {{ record.userId }}</span>
          </div>
        </template>
        <template #vip="{ record }">
          <a-tag color="arcoblue">VIP {{ record.vipLevel ?? 0 }}</a-tag>
        </template>
        <template #metric="{ record }">
          <span class="metric-value">{{ metricText(record) }}</span>
        </template>
        <template #recordCount="{ record }">
          {{ record.recordCount || 0 }}
        </template>
        <template #lastEventTime="{ record }">
          {{ record.lastEventTime ? parseTime(record.lastEventTime) : '-' }}
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { parseTime } from '@/utils/dateUtils';
  import {
    leaderboardList,
    LeaderboardItem,
    LeaderboardQuery,
  } from '@/api/leaderboard';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'leaderboard-index',
  });

  const query = ref<LeaderboardQuery>({
    type: 'EARNING',
    limit: 20,
  });
  const rows = ref<LeaderboardItem[]>([]);
  const loading = ref(false);

  const columns: TableColumnData[] = [
    { title: '排名', slotName: 'rank', align: 'center', width: 90 },
    { title: '用户', slotName: 'user', align: 'center', width: 220 },
    { title: 'VIP', slotName: 'vip', align: 'center', width: 100 },
    { title: '指标', dataIndex: 'metricLabel', align: 'center', width: 140 },
    { title: '累计值', slotName: 'metric', align: 'center', width: 170 },
    { title: '记录数', slotName: 'recordCount', align: 'center', width: 120 },
    { title: '最近发生时间', slotName: 'lastEventTime', align: 'center', width: 190 },
  ];

  const loadData = () => {
    loading.value = true;
    leaderboardList(query.value)
      .then((rep) => {
        if (rep.success) {
          rows.value = rep.data || [];
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  const resetQuery = () => {
    query.value = {
      type: 'EARNING',
      limit: 20,
    };
    loadData();
  };

  function metricText(record: LeaderboardItem): string {
    const value = Number(record.metricValue || 0);
    if (record.type === 'TASK') {
      return `${value.toLocaleString('en-US')} 次`;
    }
    return `${value.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 6,
    })} ${record.currency || 'USDT'}`;
  }

  function rankColor(rankNo: number): string {
    if (rankNo === 1) return 'gold';
    if (rankNo === 2) return 'gray';
    if (rankNo === 3) return 'orange';
    return 'blue';
  }

  loadData();
</script>

<style scoped>
  .scope-alert {
    margin-bottom: 16px;
  }

  .user-cell {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .user-id {
    color: var(--color-text-3);
    font-size: 12px;
  }

  .metric-value {
    color: #00b42a;
    font-weight: 600;
  }
</style>