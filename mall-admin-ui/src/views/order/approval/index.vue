<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.order', 'menu.order.approval']" />
    <a-card>
      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-select v-model="sourceFilter" allow-clear placeholder="审核来源" style="width: 180px">
          <a-option value="WITHDRAW">提现审核</a-option>
          <a-option value="MISSION_TASK">任务提交审核</a-option>
        </a-select>
        <a-input-number
          v-model="userFilter"
          :min="1"
          :precision="0"
          allow-clear
          placeholder="用户ID"
          style="width: 180px"
          @press-enter="loadData"
        />
        <a-input
          v-model="keyword"
          allow-clear
          placeholder="标题/单号/详情"
          style="width: 240px"
          @press-enter="loadData"
        />
        <a-button @click="resetQuery">重置</a-button>
        <a-button type="primary" @click="loadData">
          <template #icon>
            <icon-refresh />
          </template>
          刷新
        </a-button>
        <a-tag color="orange">待审核 {{ filteredData.length }} 笔</a-tag>
      </a-space>

      <a-table
        :columns="columns"
        :data="filteredData"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1450 }"
      >
        <template #sourceType="{ record }">
          <a-tag :color="sourceColor(record.sourceType)">{{ sourceText(record.sourceType) }}</a-tag>
        </template>
        <template #amount="{ record }">
          {{ moneyText(record.amount) }} {{ record.currency }}
        </template>
        <template #status="{ record }">
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
        <template #detail="{ record }">
          <a-typography-paragraph :ellipsis="{ rows: 2, expandable: true }" class="detail-text">
            {{ record.detail || '-' }}
          </a-typography-paragraph>
        </template>
        <template #submittedAt="{ record }">
          {{ record.submittedAt ? parseTime(record.submittedAt) : '-' }}
        </template>
        <template #operations="{ record }">
          <a-space :size="4">
            <a-button type="text" size="mini" status="success" @click="openReview(record, 'approve')">
              通过
            </a-button>
            <a-button type="text" size="mini" status="danger" @click="openReview(record, 'reject')">
              驳回
            </a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="reviewVisible"
      :title="reviewAction === 'approve' ? '审核通过' : '驳回审核'"
      :confirm-loading="reviewing"
      @ok="doReview"
      @cancel="reviewVisible = false"
    >
      <a-alert v-if="reviewTarget" style="margin-bottom: 12px" type="warning">
        {{ reviewHint(reviewTarget, reviewAction) }}
      </a-alert>
      <a-form :model="reviewForm" layout="vertical">
        <a-form-item label="来源">
          <a-input :model-value="reviewTarget ? sourceText(reviewTarget.sourceType) : ''" disabled />
        </a-form-item>
        <a-form-item label="业务单号/记录ID">
          <a-input v-model="reviewForm.sourceId" disabled />
        </a-form-item>
        <a-form-item field="remark" label="审核备注">
          <a-textarea v-model="reviewForm.remark" allow-clear :auto-size="{ minRows: 4, maxRows: 8 }" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { parseTime } from '@/utils/dateUtils';
  import {
    OrderApprovalItem,
    approveOrderApproval,
    pendingApprovalList,
    rejectOrderApproval,
  } from '@/api/orderApproval';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'order-approval',
  });

  const columns: TableColumnData[] = [
    { title: '来源', slotName: 'sourceType', align: 'center', width: 130 },
    { title: '业务单号/记录ID', dataIndex: 'sourceId', align: 'center', width: 170, ellipsis: true, tooltip: true },
    { title: '标题', dataIndex: 'title', align: 'center', width: 180, ellipsis: true, tooltip: true },
    { title: '用户ID', dataIndex: 'userId', align: 'center', width: 110 },
    { title: '金额', slotName: 'amount', align: 'center', width: 140 },
    { title: '状态', slotName: 'status', align: 'center', width: 120 },
    { title: '详情', slotName: 'detail', align: 'center', width: 320 },
    { title: '提交时间', slotName: 'submittedAt', align: 'center', width: 170 },
    { title: '操作', slotName: 'operations', align: 'center', fixed: 'right', width: 140 },
  ];

  const tableData = ref<OrderApprovalItem[]>([]);
  const loading = ref(false);
  const sourceFilter = ref<string | undefined>();
  const userFilter = ref<number | undefined>();
  const keyword = ref('');
  const reviewVisible = ref(false);
  const reviewing = ref(false);
  const reviewAction = ref<'approve' | 'reject'>('approve');
  const reviewTarget = ref<OrderApprovalItem>();
  const reviewForm = reactive({
    sourceType: '',
    sourceId: '',
    remark: '',
  });

  const filteredData = computed(() => {
    const kw = keyword.value.trim().toLowerCase();
    return tableData.value.filter((item) => {
      const sourceMatched = !sourceFilter.value || item.sourceType === sourceFilter.value;
      const userMatched = !userFilter.value || item.userId === userFilter.value;
      const keywordMatched = !kw || [item.sourceId, item.title, item.detail]
        .filter(Boolean)
        .some((text) => String(text).toLowerCase().includes(kw));
      return sourceMatched && userMatched && keywordMatched;
    });
  });

  const loadData = () => {
    loading.value = true;
    pendingApprovalList()
      .then((rep) => {
        if (rep.success) {
          tableData.value = rep.data || [];
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  const resetQuery = () => {
    sourceFilter.value = undefined;
    userFilter.value = undefined;
    keyword.value = '';
    loadData();
  };

  const openReview = (record: OrderApprovalItem, action: 'approve' | 'reject') => {
    reviewAction.value = action;
    reviewTarget.value = record;
    reviewForm.sourceType = record.sourceType;
    reviewForm.sourceId = record.sourceId;
    reviewForm.remark = '';
    reviewVisible.value = true;
  };

  const doReview = () => {
    reviewing.value = true;
    const payload = {
      sourceType: reviewForm.sourceType,
      sourceId: reviewForm.sourceId,
      remark: reviewForm.remark,
    };
    const request = reviewAction.value === 'approve'
      ? approveOrderApproval(payload)
      : rejectOrderApproval(payload);
    request
      .then((rep) => {
        if (rep.success) {
          Message.success(reviewAction.value === 'approve' ? '审核已通过' : '审核已驳回');
          reviewVisible.value = false;
          loadData();
        }
      })
      .finally(() => {
        reviewing.value = false;
      });
  };

  const sourceText = (source: string) => {
    const map: Record<string, string> = {
      WITHDRAW: '提现审核',
      MISSION_TASK: '任务提交审核',
    };
    return map[source] || source;
  };

  const sourceColor = (source: string) => {
    const map: Record<string, string> = {
      WITHDRAW: 'purple',
      MISSION_TASK: 'arcoblue',
    };
    return map[source] || 'gray';
  };

  const statusText = (status: string) => {
    const map: Record<string, string> = {
      REVIEWING: '待审核',
      SUBMITTED: '待审核',
      APPROVED: '已通过',
      REJECTED: '已驳回',
      BROADCASTING: '出款中',
      CONFIRMED: '已确认',
      FAILED: '失败',
    };
    return map[status] || status;
  };

  const statusColor = (status: string) => {
    const map: Record<string, string> = {
      REVIEWING: 'orange',
      SUBMITTED: 'orange',
      APPROVED: 'green',
      REJECTED: 'red',
      BROADCASTING: 'arcoblue',
      CONFIRMED: 'green',
      FAILED: 'red',
    };
    return map[status] || 'gray';
  };

  const reviewHint = (record: OrderApprovalItem, action: 'approve' | 'reject') => {
    if (record.sourceType === 'WITHDRAW') {
      return action === 'approve'
        ? '通过后会进入提现出款状态机，钱包侧负责广播与冻结资金结算。'
        : '驳回后钱包侧会按提现订单解冻退款，并记录审核备注。';
    }
    if (record.sourceType === 'MISSION_TASK') {
      return action === 'approve'
        ? '通过后会调用任务奖励结算，奖励只通过钱包账务服务入账。'
        : '驳回后任务记录进入终态，不产生钱包流水。';
    }
    return '当前操作会路由到来源业务自己的真实状态机。';
  };

  const moneyText = (value?: number) => Number(value || 0).toFixed(6);

  loadData();
</script>

<style scoped>
  .detail-text {
    margin-bottom: 0;
  }
</style>