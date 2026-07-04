<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.mission', 'menu.mission.approval']" />
    <a-card>
      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-input-number v-model="query.userId" placeholder="用户ID" style="width: 160px" />
        <a-input v-model="query.keyword" allow-clear placeholder="任务标题/编码" style="width: 220px" />
        <a-select v-model="query.taskType" allow-clear placeholder="任务类型" style="width: 160px">
          <a-option value="GENERAL">通用任务</a-option>
          <a-option value="TASK_CENTER">任务中心</a-option>
          <a-option value="SHARE">分享任务</a-option>
          <a-option value="VIDEO">视频任务</a-option>
          <a-option value="VA">VA任务</a-option>
        </a-select>
        <a-select v-model="query.status" allow-clear placeholder="状态" style="width: 160px">
          <a-option value="CLAIMED">已领取</a-option>
          <a-option value="SUBMITTED">待审核</a-option>
          <a-option value="APPROVED">已通过</a-option>
          <a-option value="REJECTED">已驳回</a-option>
          <a-option value="CANCELLED">已取消</a-option>
          <a-option value="EXPIRED">已过期</a-option>
        </a-select>
        <a-button type="primary" @click="loadData">查询</a-button>
        <a-button @click="resetQuery">重置</a-button>
      </a-space>

      <a-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1500 }"
        @page-change="onPageChange"
      >
        <template #rewardAmount="{ record }">
          {{ moneyText(record.rewardAmount) }} {{ record.currency }}
        </template>
        <template #status="{ record }">
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
        <template #submitContent="{ record }">
          <a-typography-paragraph :ellipsis="{ rows: 2, expandable: true }" class="submit-text">
            {{ record.submitContent || '-' }}
          </a-typography-paragraph>
        </template>
        <template #walletFlowNo="{ record }">
          {{ record.walletFlowNo || '-' }}
        </template>
        <template #submittedAt="{ record }">
          {{ record.submittedAt ? parseTime(record.submittedAt) : '-' }}
        </template>
        <template #finishedAt="{ record }">
          {{ record.finishedAt ? parseTime(record.finishedAt) : '-' }}
        </template>
        <template #operations="{ record }">
          <a-space v-if="record.status === 'SUBMITTED'" :size="4">
            <a-button type="text" size="mini" status="success" @click="openReview(record, 'approve')">
              通过并入账
            </a-button>
            <a-button type="text" size="mini" status="danger" @click="openReview(record, 'reject')">
              驳回
            </a-button>
          </a-space>
          <span v-else>-</span>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="reviewVisible"
      :title="reviewAction === 'approve' ? '审核通过任务' : '驳回任务'"
      :confirm-loading="reviewing"
      @ok="doReview"
    >
      <a-alert v-if="reviewAction === 'approve'" style="margin-bottom: 12px" type="warning">
        通过后会调用钱包结算 Provider 发放任务奖励，系统使用记录ID作为幂等键防止重复入账。
      </a-alert>
      <a-form :model="reviewForm" layout="vertical">
        <a-form-item label="任务记录">
          <a-input :model-value="reviewForm.recordId ? String(reviewForm.recordId) : ''" disabled />
        </a-form-item>
        <a-form-item field="reviewRemark" label="审核备注">
          <a-textarea v-model="reviewForm.reviewRemark" allow-clear :auto-size="{ minRows: 4, maxRows: 8 }" />
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
    MissionUserTask,
    approveMissionRecord,
    missionRecordPage,
    rejectMissionRecord,
  } from '@/api/mission';

  defineOptions({
    name: 'mission-approval',
  });

  const query = reactive({
    userId: undefined as number | undefined,
    keyword: '',
    taskType: undefined as string | undefined,
    status: 'SUBMITTED' as string | undefined,
    pageNumber: 1,
    pageSize: 10,
  });

  const columns: TableColumnData[] = [
    { title: '记录ID', dataIndex: 'id', align: 'center', width: 90 },
    { title: '用户ID', dataIndex: 'userId', align: 'center', width: 120 },
    { title: '任务编码', dataIndex: 'taskCode', align: 'center', width: 150 },
    { title: '任务标题', dataIndex: 'taskTitle', align: 'center', width: 180 },
    { title: '类型', dataIndex: 'taskType', align: 'center', width: 110 },
    { title: '奖励', slotName: 'rewardAmount', align: 'center', width: 140 },
    { title: '状态', slotName: 'status', align: 'center', width: 110 },
    { title: '提交内容', slotName: 'submitContent', align: 'center', width: 260 },
    { title: '钱包流水', slotName: 'walletFlowNo', align: 'center', width: 180 },
    { title: '提交时间', slotName: 'submittedAt', align: 'center', width: 170 },
    { title: '完成时间', slotName: 'finishedAt', align: 'center', width: 170 },
    { title: '操作', slotName: 'operations', align: 'center', fixed: 'right', width: 180 },
  ];

  const tableData = ref<MissionUserTask[]>([]);
  const loading = ref(false);
  const total = ref(0);
  const reviewVisible = ref(false);
  const reviewing = ref(false);
  const reviewAction = ref<'approve' | 'reject'>('approve');
  const reviewForm = reactive({
    recordId: undefined as number | undefined,
    reviewRemark: '',
  });

  const pagination = computed(() => ({
    current: query.pageNumber,
    pageSize: query.pageSize,
    total: total.value,
    showTotal: true,
  }));

  const loadData = () => {
    loading.value = true;
    missionRecordPage(query)
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
      keyword: '',
      taskType: undefined,
      status: 'SUBMITTED',
      pageNumber: 1,
    });
    loadData();
  };

  const onPageChange = (page: number) => {
    query.pageNumber = page;
    loadData();
  };

  const openReview = (record: MissionUserTask, action: 'approve' | 'reject') => {
    reviewAction.value = action;
    reviewForm.recordId = record.id;
    reviewForm.reviewRemark = '';
    reviewVisible.value = true;
  };

  const doReview = () => {
    if (!reviewForm.recordId) return;
    reviewing.value = true;
    const payload = {
      recordId: reviewForm.recordId,
      reviewRemark: reviewForm.reviewRemark,
    };
    const request = reviewAction.value === 'approve'
      ? approveMissionRecord(payload)
      : rejectMissionRecord(payload);
    request
      .then((rep) => {
        if (rep.success) {
          Message.success(reviewAction.value === 'approve' ? '审核通过，奖励已入账' : '已驳回');
          reviewVisible.value = false;
          loadData();
        }
      })
      .finally(() => {
        reviewing.value = false;
      });
  };

  const statusText = (status: string) => {
    const map: Record<string, string> = {
      CLAIMED: '已领取',
      SUBMITTED: '待审核',
      APPROVED: '已通过',
      REJECTED: '已驳回',
      CANCELLED: '已取消',
      EXPIRED: '已过期',
    };
    return map[status] || status;
  };

  const statusColor = (status: string) => {
    const map: Record<string, string> = {
      CLAIMED: 'arcoblue',
      SUBMITTED: 'orange',
      APPROVED: 'green',
      REJECTED: 'red',
      CANCELLED: 'gray',
      EXPIRED: 'gray',
    };
    return map[status] || 'gray';
  };

  const moneyText = (value?: number) => Number(value || 0).toFixed(6);

  loadData();
</script>

<style scoped>
  .submit-text {
    margin-bottom: 0;
  }
</style>