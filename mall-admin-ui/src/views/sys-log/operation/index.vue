<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.sys-log', 'menu.sys-log.operation']" />
    <a-card>
      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-input
          v-model="filterAdminAccount"
          allow-clear
          placeholder="管理员账号"
          style="width: 180px"
          @press-enter="loadData"
        />
        <a-input
          v-model="filterAction"
          allow-clear
          placeholder="动作关键字"
          style="width: 180px"
          @press-enter="loadData"
        />
        <a-select v-model="filterSuccess" style="width: 140px" @change="loadData">
          <a-option :value="undefined">全部结果</a-option>
          <a-option :value="1">成功</a-option>
          <a-option :value="0">失败</a-option>
        </a-select>
        <a-select v-model="filterLimit" style="width: 140px" @change="loadData">
          <a-option :value="100">最近 100 条</a-option>
          <a-option :value="200">最近 200 条</a-option>
          <a-option :value="500">最近 500 条</a-option>
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
        :scroll="{ x: 1600 }"
      >
        <template #action="{ record }">
          <a-tag color="arcoblue">{{ actionText(record.action) }}</a-tag>
        </template>
        <template #success="{ record }">
          <a-tag :color="record.success === 1 ? 'green' : 'red'">
            {{ record.success === 1 ? '成功' : '失败' }}
          </a-tag>
        </template>
        <template #path="{ record }">
          <a-typography-paragraph
            :copyable="Boolean(record.path)"
            :ellipsis="{ rows: 1, expandable: true }"
            class="path-text"
          >
            {{ fullPath(record) }}
          </a-typography-paragraph>
        </template>
        <template #duration="{ record }">
          {{ record.durationMs }} ms
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
  import {
    OperationLogRecord,
    operationLogList,
  } from '@/api/operationLog';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'operation-log',
  });

  const columns: TableColumnData[] = [
    {
      title: '管理员',
      dataIndex: 'adminAccount',
      align: 'center',
      width: 120,
    },
    {
      title: '动作',
      slotName: 'action',
      align: 'center',
      width: 170,
    },
    {
      title: '方法',
      dataIndex: 'method',
      align: 'center',
      width: 90,
    },
    {
      title: '路径',
      slotName: 'path',
      align: 'center',
      width: 420,
    },
    {
      title: '结果',
      slotName: 'success',
      align: 'center',
      width: 90,
    },
    {
      title: '状态码',
      dataIndex: 'statusCode',
      align: 'center',
      width: 90,
    },
    {
      title: '耗时',
      slotName: 'duration',
      align: 'center',
      width: 110,
    },
    {
      title: 'IP',
      dataIndex: 'ipAddress',
      align: 'center',
      ellipsis: true,
      tooltip: true,
      width: 140,
    },
    {
      title: '浏览器标识',
      dataIndex: 'userAgent',
      align: 'center',
      ellipsis: true,
      tooltip: true,
      width: 220,
    },
    {
      title: '时间',
      slotName: 'createTime',
      align: 'center',
      fixed: 'right',
      width: 170,
    },
  ];

  const tableData = ref<OperationLogRecord[]>([]);
  const loading = ref(false);
  const filterAdminAccount = ref('');
  const filterAction = ref('');
  const filterSuccess = ref<number | undefined>();
  const filterLimit = ref(200);

  const loadData = () => {
    loading.value = true;
    operationLogList({
      adminAccount: filterAdminAccount.value || undefined,
      action: filterAction.value || undefined,
      success: filterSuccess.value,
      limit: filterLimit.value,
    })
      .then((rep) => {
        if (rep.success) {
          tableData.value = rep.data || [];
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  const resetFilters = () => {
    filterAdminAccount.value = '';
    filterAction.value = '';
    filterSuccess.value = undefined;
    filterLimit.value = 200;
    loadData();
  };

  const fullPath = (record: OperationLogRecord): string => {
    return record.queryString ? `${record.path}?${record.queryString}` : record.path;
  };

  function actionText(action: string): string {
    const map: Record<string, string> = {
      GET_CONTENT_CONFIG: '查看内容配置',
      POST_CONTENT_CONFIG: '新增内容配置',
      PUT_CONTENT_CONFIG: '更新内容配置',
      DELETE_CONTENT_CONFIG: '删除内容配置',
      GET_SYSTEM_PARAM: '查看系统参数',
      POST_SYSTEM_PARAM: '新增系统参数',
      PUT_SYSTEM_PARAM: '更新系统参数',
      DELETE_SYSTEM_PARAM: '删除系统参数',
      POST_MEMBER_STATUS: '调整会员状态',
      GET_MEMBER_VIEW: '查看会员',
      GET_WITHDRAW_REVIEW: '查看提现审核',
      POST_WITHDRAW_REVIEW: '处理提现审核',
      GET_RECHARGE_VIEW: '查看充值订单',
      GET_COLLECT_VIEW: '查看归集订单',
      GET_WALLET_FLOW_VIEW: '查看账务流水',
      GET_REPORT_VIEW: '查看报表',
      GET_OPERATION_LOG_VIEW: '查看操作日志',
      POST_ADMIN_LOGOUT: '退出登录',
    };
    return map[action] || action;
  }

  loadData();
</script>

<style scoped>
  .path-text {
    margin-bottom: 0;
    text-align: left;
  }
</style>