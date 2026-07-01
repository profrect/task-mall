<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.mission', 'menu.mission.list']" />
    <a-card>
      <a-tabs v-model:active-key="activeTab" @change="loadData">
        <a-tab-pane key="tasks" title="任务配置">
          <a-space :size="12" wrap style="margin-bottom: 16px">
            <a-input v-model="taskQuery.keyword" allow-clear placeholder="任务标题/编码" style="width: 220px" />
            <a-select v-model="taskQuery.status" allow-clear placeholder="状态" style="width: 120px">
              <a-option :value="1">启用</a-option>
              <a-option :value="0">停用</a-option>
            </a-select>
            <a-button type="primary" @click="loadTasks">查询</a-button>
            <a-button @click="resetTasks">重置</a-button>
            <a-button type="primary" @click="openTaskCreate">新增任务</a-button>
          </a-space>

          <a-table
            :columns="taskColumns"
            :data="taskRows"
            :loading="loading"
            :pagination="taskPagination"
            :scroll="{ x: 1400 }"
            @page-change="onTaskPageChange"
          >
            <template #rewardAmount="{ record }">
              {{ moneyText(record.rewardAmount) }} {{ record.currency }}
            </template>
            <template #status="{ record }">
              <a-tag :color="record.status === 1 ? 'green' : 'gray'">
                {{ record.status === 1 ? '启用' : '停用' }}
              </a-tag>
            </template>
            <template #timeRange="{ record }">
              {{ record.startAt ? parseTime(record.startAt) : '-' }} / {{ record.endAt ? parseTime(record.endAt) : '-' }}
            </template>
            <template #operations="{ record }">
              <a-space :size="4">
                <a-button type="text" size="mini" @click="openTaskEdit(record)">编辑</a-button>
                <a-button
                  type="text"
                  size="mini"
                  :status="record.status === 1 ? 'warning' : 'success'"
                  @click="toggleTaskStatus(record)"
                >
                  {{ record.status === 1 ? '停用' : '启用' }}
                </a-button>
                <a-popconfirm content="确认删除该任务配置？" @ok="doDeleteTask(record.id)">
                  <a-button type="text" size="mini" status="danger">删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table>
        </a-tab-pane>

        <a-tab-pane key="goods" title="商品/投资展示">
          <a-space :size="12" wrap style="margin-bottom: 16px">
            <a-input v-model="goodsQuery.keyword" allow-clear placeholder="标题/编码" style="width: 220px" />
            <a-select v-model="goodsQuery.goodsType" allow-clear placeholder="类型" style="width: 170px">
              <a-option value="TASK_PRODUCT">普通商品</a-option>
              <a-option value="INVEST_PROJECT">投资项目展示</a-option>
            </a-select>
            <a-select v-model="goodsQuery.status" allow-clear placeholder="状态" style="width: 120px">
              <a-option :value="1">启用</a-option>
              <a-option :value="0">停用</a-option>
            </a-select>
            <a-button type="primary" @click="loadGoods">查询</a-button>
            <a-button @click="resetGoods">重置</a-button>
            <a-button type="primary" @click="openGoodsCreate">新增展示项</a-button>
          </a-space>

          <a-alert style="margin-bottom: 12px" type="info">
            投资项目在当前阶段只作为任务中心商品/计划展示口径，不产生独立投资扣款、派息、分红或到期结算。
          </a-alert>

          <a-table
            :columns="goodsColumns"
            :data="goodsRows"
            :loading="loading"
            :pagination="goodsPagination"
            :scroll="{ x: 1400 }"
            @page-change="onGoodsPageChange"
          >
            <template #goodsType="{ record }">
              <a-tag :color="record.goodsType === 'INVEST_PROJECT' ? 'orange' : 'arcoblue'">
                {{ record.goodsType === 'INVEST_PROJECT' ? '投资展示' : '普通商品' }}
              </a-tag>
            </template>
            <template #amountRange="{ record }">
              {{ moneyText(record.minAmount) }} - {{ moneyText(record.maxAmount) }} {{ record.currency }}
            </template>
            <template #displayRate="{ record }">
              {{ record.displayRate || 0 }}%
            </template>
            <template #status="{ record }">
              <a-tag :color="record.status === 1 ? 'green' : 'gray'">
                {{ record.status === 1 ? '启用' : '停用' }}
              </a-tag>
            </template>
            <template #operations="{ record }">
              <a-space :size="4">
                <a-button type="text" size="mini" @click="openGoodsEdit(record)">编辑</a-button>
                <a-button
                  type="text"
                  size="mini"
                  :status="record.status === 1 ? 'warning' : 'success'"
                  @click="toggleGoodsStatus(record)"
                >
                  {{ record.status === 1 ? '停用' : '启用' }}
                </a-button>
                <a-popconfirm content="确认删除该展示配置？" @ok="doDeleteGoods(record.id)">
                  <a-button type="text" size="mini" status="danger">删除</a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <a-modal
      v-model:visible="taskModalVisible"
      :title="taskForm.id ? '编辑任务' : '新增任务'"
      :confirm-loading="saving"
      width="760px"
      @ok="doSaveTask"
    >
      <a-form :model="taskForm" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item field="taskCode" label="任务编码">
              <a-input v-model="taskForm.taskCode" allow-clear placeholder="留空自动生成" />
            </a-form-item>
          </a-col>
          <a-col :span="10">
            <a-form-item field="title" label="任务标题" required>
              <a-input v-model="taskForm.title" allow-clear :max-length="100" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="taskType" label="任务类型">
              <a-input v-model="taskForm.taskType" allow-clear />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item field="rewardAmount" label="奖励金额" required>
              <a-input-number v-model="taskForm.rewardAmount" :min="0" :precision="6" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="currency" label="币种">
              <a-input v-model="taskForm.currency" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="requiredVipLevel" label="所需 VIP">
              <a-input-number v-model="taskForm.requiredVipLevel" :min="0" :precision="0" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="dailyLimit" label="每日限制">
              <a-input-number v-model="taskForm.dailyLimit" :min="0" :precision="0" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item field="sortOrder" label="排序">
              <a-input-number v-model="taskForm.sortOrder" :min="0" :precision="0" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="status" label="状态">
              <a-select v-model="taskForm.status">
                <a-option :value="1">启用</a-option>
                <a-option :value="0">停用</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item field="description" label="任务说明">
          <a-textarea v-model="taskForm.description" allow-clear :auto-size="{ minRows: 4, maxRows: 8 }" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:visible="goodsModalVisible"
      :title="goodsForm.id ? '编辑展示项' : '新增展示项'"
      :confirm-loading="saving"
      width="760px"
      @ok="doSaveGoods"
    >
      <a-form :model="goodsForm" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item field="goodsType" label="类型" required>
              <a-select v-model="goodsForm.goodsType">
                <a-option value="TASK_PRODUCT">普通商品</a-option>
                <a-option value="INVEST_PROJECT">投资项目展示</a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="goodsCode" label="编码">
              <a-input v-model="goodsForm.goodsCode" allow-clear placeholder="留空自动生成" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="title" label="标题" required>
              <a-input v-model="goodsForm.title" allow-clear />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item field="minAmount" label="最小展示金额">
              <a-input-number v-model="goodsForm.minAmount" :min="0" :precision="6" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="maxAmount" label="最大展示金额">
              <a-input-number v-model="goodsForm.maxAmount" :min="0" :precision="6" />
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <a-form-item field="currency" label="币种">
              <a-input v-model="goodsForm.currency" />
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <a-form-item field="displayRate" label="展示比例%">
              <a-input-number v-model="goodsForm.displayRate" :min="0" :precision="4" />
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <a-form-item field="cycleDays" label="周期天">
              <a-input-number v-model="goodsForm.cycleDays" :min="0" :precision="0" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item field="riskLevel" label="风险等级">
              <a-input v-model="goodsForm.riskLevel" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="sortOrder" label="排序">
              <a-input-number v-model="goodsForm.sortOrder" :min="0" :precision="0" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="status" label="状态">
              <a-select v-model="goodsForm.status">
                <a-option :value="1">启用</a-option>
                <a-option :value="0">停用</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item field="description" label="说明">
          <a-textarea v-model="goodsForm.description" allow-clear :auto-size="{ minRows: 4, maxRows: 8 }" />
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
    MissionGoods,
    MissionTask,
    deleteMissionGoods,
    deleteMissionTask,
    missionGoodsPage,
    missionTaskPage,
    saveMissionGoods,
    saveMissionTask,
  } from '@/api/mission';

  defineOptions({
    name: 'mission-list',
  });

  const activeTab = ref('tasks');
  const loading = ref(false);
  const saving = ref(false);

  const taskRows = ref<MissionTask[]>([]);
  const goodsRows = ref<MissionGoods[]>([]);
  const taskTotal = ref(0);
  const goodsTotal = ref(0);

  const taskQuery = reactive({
    keyword: '',
    status: undefined as number | undefined,
    pageNumber: 1,
    pageSize: 10,
  });
  const goodsQuery = reactive({
    keyword: '',
    goodsType: undefined as string | undefined,
    status: undefined as number | undefined,
    pageNumber: 1,
    pageSize: 10,
  });

  const emptyTask = (): MissionTask => ({
    taskCode: '',
    title: '',
    description: '',
    taskType: 'GENERAL',
    currency: 'USDT',
    rewardAmount: 0,
    requiredVipLevel: 0,
    dailyLimit: 0,
    sortOrder: 0,
    status: 1,
  });

  const emptyGoods = (): MissionGoods => ({
    goodsType: 'INVEST_PROJECT',
    goodsCode: '',
    title: '',
    description: '',
    currency: 'USDT',
    minAmount: 0,
    maxAmount: 0,
    displayRate: 0,
    cycleDays: 0,
    riskLevel: 'LOW',
    sortOrder: 0,
    status: 1,
  });

  const taskForm = reactive<MissionTask>(emptyTask());
  const goodsForm = reactive<MissionGoods>(emptyGoods());
  const taskModalVisible = ref(false);
  const goodsModalVisible = ref(false);

  const taskColumns: TableColumnData[] = [
    { title: 'ID', dataIndex: 'id', align: 'center', width: 80 },
    { title: '任务编码', dataIndex: 'taskCode', align: 'center', width: 150 },
    { title: '标题', dataIndex: 'title', align: 'center', width: 180 },
    { title: '类型', dataIndex: 'taskType', align: 'center', width: 120 },
    { title: '奖励', slotName: 'rewardAmount', align: 'center', width: 140 },
    { title: '所需VIP', dataIndex: 'requiredVipLevel', align: 'center', width: 100 },
    { title: '时间范围', slotName: 'timeRange', align: 'center', width: 260 },
    { title: '状态', slotName: 'status', align: 'center', width: 100 },
    { title: '操作', slotName: 'operations', align: 'center', fixed: 'right', width: 180 },
  ];

  const goodsColumns: TableColumnData[] = [
    { title: 'ID', dataIndex: 'id', align: 'center', width: 80 },
    { title: '类型', slotName: 'goodsType', align: 'center', width: 130 },
    { title: '编码', dataIndex: 'goodsCode', align: 'center', width: 170 },
    { title: '标题', dataIndex: 'title', align: 'center', width: 180 },
    { title: '金额范围', slotName: 'amountRange', align: 'center', width: 220 },
    { title: '展示比例', slotName: 'displayRate', align: 'center', width: 110 },
    { title: '周期天', dataIndex: 'cycleDays', align: 'center', width: 100 },
    { title: '风险', dataIndex: 'riskLevel', align: 'center', width: 100 },
    { title: '状态', slotName: 'status', align: 'center', width: 100 },
    { title: '操作', slotName: 'operations', align: 'center', fixed: 'right', width: 180 },
  ];

  const taskPagination = computed(() => ({
    current: taskQuery.pageNumber,
    pageSize: taskQuery.pageSize,
    total: taskTotal.value,
    showTotal: true,
  }));

  const goodsPagination = computed(() => ({
    current: goodsQuery.pageNumber,
    pageSize: goodsQuery.pageSize,
    total: goodsTotal.value,
    showTotal: true,
  }));

  const loadTasks = () => {
    loading.value = true;
    missionTaskPage(taskQuery)
      .then((rep) => {
        if (rep.success) {
          taskRows.value = rep.data?.records || [];
          taskTotal.value = rep.data?.totalRow || 0;
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  const loadGoods = () => {
    loading.value = true;
    missionGoodsPage(goodsQuery)
      .then((rep) => {
        if (rep.success) {
          goodsRows.value = rep.data?.records || [];
          goodsTotal.value = rep.data?.totalRow || 0;
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  const loadData = () => {
    if (activeTab.value === 'tasks') {
      loadTasks();
    } else {
      loadGoods();
    }
  };

  const resetTasks = () => {
    Object.assign(taskQuery, { keyword: '', status: undefined, pageNumber: 1 });
    loadTasks();
  };

  const resetGoods = () => {
    Object.assign(goodsQuery, { keyword: '', goodsType: undefined, status: undefined, pageNumber: 1 });
    loadGoods();
  };

  const onTaskPageChange = (page: number) => {
    taskQuery.pageNumber = page;
    loadTasks();
  };

  const onGoodsPageChange = (page: number) => {
    goodsQuery.pageNumber = page;
    loadGoods();
  };

  const openTaskCreate = () => {
    Object.assign(taskForm, emptyTask());
    taskModalVisible.value = true;
  };

  const openTaskEdit = (record: MissionTask) => {
    Object.assign(taskForm, emptyTask(), record);
    taskModalVisible.value = true;
  };

  const openGoodsCreate = () => {
    Object.assign(goodsForm, emptyGoods());
    goodsModalVisible.value = true;
  };

  const openGoodsEdit = (record: MissionGoods) => {
    Object.assign(goodsForm, emptyGoods(), record);
    goodsModalVisible.value = true;
  };

  const doSaveTask = () => {
    if (!taskForm.title || !taskForm.rewardAmount || taskForm.rewardAmount <= 0) {
      Message.warning('请填写任务标题和正数奖励金额');
      return;
    }
    saving.value = true;
    saveMissionTask(taskForm)
      .then((rep) => {
        if (rep.success) {
          Message.success('保存成功');
          taskModalVisible.value = false;
          loadTasks();
        }
      })
      .finally(() => {
        saving.value = false;
      });
  };

  const doSaveGoods = () => {
    if (!goodsForm.title || !goodsForm.goodsType) {
      Message.warning('请填写类型和标题');
      return;
    }
    saving.value = true;
    saveMissionGoods(goodsForm)
      .then((rep) => {
        if (rep.success) {
          Message.success('保存成功');
          goodsModalVisible.value = false;
          loadGoods();
        }
      })
      .finally(() => {
        saving.value = false;
      });
  };

  const toggleTaskStatus = (record: MissionTask) => {
    saveMissionTask({ ...record, status: record.status === 1 ? 0 : 1 }).then((rep) => {
      if (rep.success) {
        Message.success('状态已更新');
        loadTasks();
      }
    });
  };

  const toggleGoodsStatus = (record: MissionGoods) => {
    saveMissionGoods({ ...record, status: record.status === 1 ? 0 : 1 }).then((rep) => {
      if (rep.success) {
        Message.success('状态已更新');
        loadGoods();
      }
    });
  };

  const doDeleteTask = (id?: number) => {
    if (!id) return;
    deleteMissionTask(id).then((rep) => {
      if (rep.success) {
        Message.success('删除成功');
        loadTasks();
      }
    });
  };

  const doDeleteGoods = (id?: number) => {
    if (!id) return;
    deleteMissionGoods(id).then((rep) => {
      if (rep.success) {
        Message.success('删除成功');
        loadGoods();
      }
    });
  };

  const moneyText = (value?: number) => Number(value || 0).toFixed(6);

  loadTasks();
</script>