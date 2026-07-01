<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.promotion', 'menu.promotion.lucky-draw']" />
    <a-card>
      <a-tabs v-model:active-key="activeTab" @change="loadActiveTab">
        <a-tab-pane key="activities" title="抽奖活动">
          <a-space :size="12" wrap style="margin-bottom: 16px">
            <a-input v-model="activityQuery.keyword" allow-clear placeholder="活动标题/编码" style="width: 220px" />
            <a-select v-model="activityQuery.status" allow-clear placeholder="状态" style="width: 120px">
              <a-option :value="1">启用</a-option>
              <a-option :value="0">停用</a-option>
            </a-select>
            <a-button type="primary" @click="loadActivities">查询</a-button>
            <a-button @click="resetActivities">重置</a-button>
            <a-button type="primary" @click="openActivityCreate">新增活动</a-button>
          </a-space>

          <a-table
            :columns="activityColumns"
            :data="activityRows"
            :loading="loading"
            :pagination="activityPagination"
            :scroll="{ x: 1300 }"
            @page-change="onActivityPageChange"
          >
            <template #dailyLimit="{ record }">
              {{ record.dailyLimit === 0 ? '不限制' : `${record.dailyLimit} 次/日` }}
            </template>
            <template #timeRange="{ record }">
              {{ record.startAt ? parseTime(record.startAt) : '-' }} / {{ record.endAt ? parseTime(record.endAt) : '-' }}
            </template>
            <template #status="{ record }">
              <a-tag :color="record.status === 1 ? 'green' : 'gray'">
                {{ record.status === 1 ? '启用' : '停用' }}
              </a-tag>
            </template>
            <template #operations="{ record }">
              <a-space :size="4">
                <a-button type="text" size="mini" @click="openPrizePool(record)">奖池</a-button>
                <a-button type="text" size="mini" @click="openActivityEdit(record)">编辑</a-button>
                <a-button
                  type="text"
                  size="mini"
                  :status="record.status === 1 ? 'warning' : 'success'"
                  @click="toggleActivityStatus(record)"
                >
                  {{ record.status === 1 ? '停用' : '启用' }}
                </a-button>
                <a-popconfirm content="确认删除该活动？" @ok="doDeleteActivity(record.id)">
                  <a-button type="text" size="mini" status="danger">删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table>
        </a-tab-pane>

        <a-tab-pane key="records" title="抽奖记录">
          <a-space :size="12" wrap style="margin-bottom: 16px">
            <a-input-number v-model="recordQuery.userId" placeholder="用户ID" style="width: 160px" />
            <a-input-number v-model="recordQuery.activityId" placeholder="活动ID" style="width: 160px" />
            <a-input v-model="recordQuery.keyword" allow-clear placeholder="记录号/奖品名" style="width: 220px" />
            <a-select v-model="recordQuery.status" allow-clear placeholder="状态" style="width: 150px">
              <a-option value="DRAWN">已抽中</a-option>
              <a-option value="SETTLED">已结算</a-option>
              <a-option value="SETTLE_FAILED">结算失败</a-option>
            </a-select>
            <a-button type="primary" @click="loadRecords">查询</a-button>
            <a-button @click="resetRecords">重置</a-button>
          </a-space>

          <a-table
            :columns="recordColumns"
            :data="recordRows"
            :loading="loading"
            :pagination="recordPagination"
            :scroll="{ x: 1600 }"
            @page-change="onRecordPageChange"
          >
            <template #amount="{ record }">
              {{ moneyText(record.amount) }} {{ record.currency }}
            </template>
            <template #status="{ record }">
              <a-tag :color="recordStatusColor(record.status)">{{ recordStatusText(record.status) }}</a-tag>
            </template>
            <template #walletFlowNo="{ record }">
              {{ record.walletFlowNo || '-' }}
            </template>
            <template #drawnAt="{ record }">
              {{ record.drawnAt ? parseTime(record.drawnAt) : '-' }}
            </template>
            <template #settledAt="{ record }">
              {{ record.settledAt ? parseTime(record.settledAt) : '-' }}
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <a-modal
      v-model:visible="activityModalVisible"
      :title="activityForm.id ? '编辑抽奖活动' : '新增抽奖活动'"
      :confirm-loading="saving"
      width="760px"
      @ok="doSaveActivity"
    >
      <a-form :model="activityForm" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item field="activityCode" label="活动编码">
              <a-input v-model="activityForm.activityCode" allow-clear placeholder="留空自动生成" />
            </a-form-item>
          </a-col>
          <a-col :span="10">
            <a-form-item field="title" label="活动标题" required>
              <a-input v-model="activityForm.title" allow-clear />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="dailyLimit" label="每日次数">
              <a-input-number v-model="activityForm.dailyLimit" :min="0" :precision="0" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item field="sortOrder" label="排序">
              <a-input-number v-model="activityForm.sortOrder" :min="0" :precision="0" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="status" label="状态">
              <a-select v-model="activityForm.status">
                <a-option :value="1">启用</a-option>
                <a-option :value="0">停用</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item field="description" label="活动说明">
          <a-textarea v-model="activityForm.description" allow-clear :auto-size="{ minRows: 4, maxRows: 8 }" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:visible="poolVisible" title="活动奖池" width="900px" :footer="false" @cancel="closePool">
      <a-alert style="margin-bottom: 12px" type="info">
        实际概率 = 单个奖项权重 / 当前活动启用奖项总权重；非现金奖只记录中奖事实，不触发钱包入账。
      </a-alert>
      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-select v-model="poolForm.prizeId" placeholder="选择奖品" style="width: 260px">
          <a-option v-for="item in prizeOptions" :key="item.id" :value="item.id">
            {{ item.prizeName }} / {{ item.prizeCode }}
          </a-option>
        </a-select>
        <a-input-number v-model="poolForm.weight" placeholder="权重" :min="1" :precision="0" style="width: 120px" />
        <a-input-number
          v-model="poolForm.dailyLimit"
          placeholder="每日限制"
          :min="0"
          :precision="0"
          style="width: 140px"
        />
        <a-button type="primary" @click="doSaveActivityPrize">加入/保存</a-button>
      </a-space>
      <a-table :columns="poolColumns" :data="poolRows" :pagination="false" :scroll="{ x: 1000 }">
        <template #amount="{ record }">
          {{ moneyText(record.amount) }} {{ record.currency }}
        </template>
        <template #status="{ record }">
          <a-tag :color="record.status === 1 ? 'green' : 'gray'">
            {{ record.status === 1 ? '启用' : '停用' }}
          </a-tag>
        </template>
        <template #operations="{ record }">
          <a-space :size="4">
            <a-button type="text" size="mini" @click="editActivityPrize(record)">编辑</a-button>
            <a-button type="text" size="mini" :status="record.status === 1 ? 'warning' : 'success'" @click="togglePoolStatus(record)">
              {{ record.status === 1 ? '停用' : '启用' }}
            </a-button>
            <a-popconfirm content="确认移除该奖池项？" @ok="doDeleteActivityPrize(record.id)">
              <a-button type="text" size="mini" status="danger">移除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { parseTime } from '@/utils/dateUtils';
  import {
    PromotionLotteryActivity,
    PromotionLotteryPrize,
    PromotionLotteryRecord,
    PromotionPrize,
    deletePromotionActivity,
    deletePromotionActivityPrize,
    promotionActivityPage,
    promotionActivityPrizes,
    promotionLotteryRecordPage,
    promotionPrizePage,
    savePromotionActivity,
    savePromotionActivityPrize,
  } from '@/api/promotion';

  defineOptions({
    name: 'promotion-lucky-draw',
  });

  const activeTab = ref('activities');
  const loading = ref(false);
  const saving = ref(false);

  const activityQuery = reactive({
    keyword: '',
    status: undefined as number | undefined,
    pageNumber: 1,
    pageSize: 10,
  });
  const recordQuery = reactive({
    userId: undefined as number | undefined,
    activityId: undefined as number | undefined,
    status: undefined as string | undefined,
    keyword: '',
    pageNumber: 1,
    pageSize: 10,
  });

  const activityRows = ref<PromotionLotteryActivity[]>([]);
  const recordRows = ref<PromotionLotteryRecord[]>([]);
  const poolRows = ref<PromotionLotteryPrize[]>([]);
  const prizeOptions = ref<PromotionPrize[]>([]);
  const activityTotal = ref(0);
  const recordTotal = ref(0);
  const currentActivity = ref<PromotionLotteryActivity>();
  const activityModalVisible = ref(false);
  const poolVisible = ref(false);

  const defaultActivityForm = (): PromotionLotteryActivity => ({
    title: '',
    description: '',
    dailyLimit: 1,
    sortOrder: 0,
    status: 1,
  });

  const activityForm = reactive<PromotionLotteryActivity>(defaultActivityForm());
  const poolForm = reactive<PromotionLotteryPrize>({
    activityId: 0,
    prizeId: 0,
    weight: 1,
    dailyLimit: 0,
    sortOrder: 0,
    status: 1,
  });

  const activityColumns: TableColumnData[] = [
    { title: 'ID', dataIndex: 'id', align: 'center', width: 80 },
    { title: '活动编码', dataIndex: 'activityCode', align: 'center', width: 170 },
    { title: '活动标题', dataIndex: 'title', align: 'center', width: 180 },
    { title: '每日次数', slotName: 'dailyLimit', align: 'center', width: 120 },
    { title: '时间范围', slotName: 'timeRange', align: 'center', width: 260 },
    { title: '排序', dataIndex: 'sortOrder', align: 'center', width: 90 },
    { title: '状态', slotName: 'status', align: 'center', width: 100 },
    { title: '操作', slotName: 'operations', align: 'center', fixed: 'right', width: 230 },
  ];

  const recordColumns: TableColumnData[] = [
    { title: '记录号', dataIndex: 'recordNo', align: 'center', width: 210 },
    { title: '用户ID', dataIndex: 'userId', align: 'center', width: 110 },
    { title: '活动', dataIndex: 'activityTitle', align: 'center', width: 180 },
    { title: '奖品', dataIndex: 'prizeName', align: 'center', width: 180 },
    { title: '金额', slotName: 'amount', align: 'center', width: 150 },
    { title: '状态', slotName: 'status', align: 'center', width: 120 },
    { title: '钱包流水', slotName: 'walletFlowNo', align: 'center', width: 190 },
    { title: '失败原因', dataIndex: 'failReason', align: 'center', width: 220 },
    { title: '抽奖时间', slotName: 'drawnAt', align: 'center', width: 170 },
    { title: '结算时间', slotName: 'settledAt', align: 'center', width: 170 },
  ];

  const poolColumns: TableColumnData[] = [
    { title: '奖品编码', dataIndex: 'prizeCode', align: 'center', width: 170 },
    { title: '奖品名称', dataIndex: 'prizeName', align: 'center', width: 180 },
    { title: '类型', dataIndex: 'prizeType', align: 'center', width: 100 },
    { title: '金额', slotName: 'amount', align: 'center', width: 150 },
    { title: '权重', dataIndex: 'weight', align: 'center', width: 90 },
    { title: '每日限制', dataIndex: 'dailyLimit', align: 'center', width: 110 },
    { title: '状态', slotName: 'status', align: 'center', width: 100 },
    { title: '操作', slotName: 'operations', align: 'center', fixed: 'right', width: 200 },
  ];

  const activityPagination = computed(() => ({
    current: activityQuery.pageNumber,
    pageSize: activityQuery.pageSize,
    total: activityTotal.value,
    showTotal: true,
  }));

  const recordPagination = computed(() => ({
    current: recordQuery.pageNumber,
    pageSize: recordQuery.pageSize,
    total: recordTotal.value,
    showTotal: true,
  }));

  const loadActiveTab = () => {
    if (activeTab.value === 'records') loadRecords();
    else loadActivities();
  };

  const loadActivities = () => {
    loading.value = true;
    promotionActivityPage(activityQuery)
      .then((rep) => {
        if (rep.success) {
          activityRows.value = rep.data?.records || [];
          activityTotal.value = rep.data?.totalRow || 0;
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  const loadRecords = () => {
    loading.value = true;
    promotionLotteryRecordPage(recordQuery)
      .then((rep) => {
        if (rep.success) {
          recordRows.value = rep.data?.records || [];
          recordTotal.value = rep.data?.totalRow || 0;
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  const resetActivities = () => {
    Object.assign(activityQuery, { keyword: '', status: undefined, pageNumber: 1 });
    loadActivities();
  };

  const resetRecords = () => {
    Object.assign(recordQuery, {
      userId: undefined,
      activityId: undefined,
      status: undefined,
      keyword: '',
      pageNumber: 1,
    });
    loadRecords();
  };

  const onActivityPageChange = (page: number) => {
    activityQuery.pageNumber = page;
    loadActivities();
  };

  const onRecordPageChange = (page: number) => {
    recordQuery.pageNumber = page;
    loadRecords();
  };

  const openActivityCreate = () => {
    Object.assign(activityForm, defaultActivityForm());
    activityModalVisible.value = true;
  };

  const openActivityEdit = (record: PromotionLotteryActivity) => {
    Object.assign(activityForm, { ...record });
    activityModalVisible.value = true;
  };

  const doSaveActivity = () => {
    if (!activityForm.title) {
      Message.warning('请填写活动标题');
      return;
    }
    saving.value = true;
    savePromotionActivity(activityForm)
      .then((rep) => {
        if (rep.success) {
          Message.success('保存成功');
          activityModalVisible.value = false;
          loadActivities();
        }
      })
      .finally(() => {
        saving.value = false;
      });
  };

  const doDeleteActivity = (id?: number) => {
    if (!id) return;
    deletePromotionActivity(id).then((rep) => {
      if (rep.success) {
        Message.success('删除成功');
        loadActivities();
      }
    });
  };

  const toggleActivityStatus = (record: PromotionLotteryActivity) => {
    savePromotionActivity({ ...record, status: record.status === 1 ? 0 : 1 }).then((rep) => {
      if (rep.success) {
        Message.success('状态已更新');
        loadActivities();
      }
    });
  };

  const openPrizePool = (record: PromotionLotteryActivity) => {
    currentActivity.value = record;
    Object.assign(poolForm, {
      id: undefined,
      activityId: record.id || 0,
      prizeId: 0,
      weight: 1,
      dailyLimit: 0,
      sortOrder: 0,
      status: 1,
    });
    poolVisible.value = true;
    loadPrizeOptions();
    loadPool();
  };

  const closePool = () => {
    currentActivity.value = undefined;
  };

  const loadPrizeOptions = () => {
    promotionPrizePage({ status: 1, pageNumber: 1, pageSize: 200 }).then((rep) => {
      if (rep.success) prizeOptions.value = rep.data?.records || [];
    });
  };

  const loadPool = () => {
    if (!currentActivity.value?.id) return;
    promotionActivityPrizes(currentActivity.value.id).then((rep) => {
      if (rep.success) poolRows.value = rep.data || [];
    });
  };

  const editActivityPrize = (record: PromotionLotteryPrize) => {
    Object.assign(poolForm, { ...record });
  };

  const doSaveActivityPrize = () => {
    if (!poolForm.activityId || !poolForm.prizeId) {
      Message.warning('请选择活动和奖品');
      return;
    }
    savePromotionActivityPrize(poolForm).then((rep) => {
      if (rep.success) {
        Message.success('奖池已保存');
        Object.assign(poolForm, {
          id: undefined,
          activityId: currentActivity.value?.id || 0,
          prizeId: 0,
          weight: 1,
          dailyLimit: 0,
          sortOrder: 0,
          status: 1,
        });
        loadPool();
      }
    });
  };

  const doDeleteActivityPrize = (id?: number) => {
    if (!id) return;
    deletePromotionActivityPrize(id).then((rep) => {
      if (rep.success) {
        Message.success('已移除');
        loadPool();
      }
    });
  };

  const togglePoolStatus = (record: PromotionLotteryPrize) => {
    savePromotionActivityPrize({ ...record, status: record.status === 1 ? 0 : 1 }).then((rep) => {
      if (rep.success) {
        Message.success('状态已更新');
        loadPool();
      }
    });
  };

  const recordStatusText = (status: string) => {
    const map: Record<string, string> = {
      DRAWN: '已抽中',
      SETTLED: '已结算',
      SETTLE_FAILED: '结算失败',
    };
    return map[status] || status;
  };

  const recordStatusColor = (status: string) => {
    const map: Record<string, string> = {
      DRAWN: 'arcoblue',
      SETTLED: 'green',
      SETTLE_FAILED: 'red',
    };
    return map[status] || 'gray';
  };

  const moneyText = (value?: number) => Number(value || 0).toFixed(6);

  loadActivities();
</script>