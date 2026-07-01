<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.sys-setting', 'menu.sys-setting.sys-param']" />
    <a-card>
      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-input
          v-model="keyword"
          allow-clear
          placeholder="参数键"
          style="width: 220px"
          @press-enter="loadData"
        />
        <a-select
          v-model="filterStatus"
          style="width: 140px"
          @change="loadData"
        >
          <a-option :value="undefined">全部</a-option>
          <a-option :value="1">启用</a-option>
          <a-option :value="0">停用</a-option>
        </a-select>
        <a-button type="primary" @click="loadData">查询</a-button>
        <a-button @click="resetQuery">重置</a-button>
        <a-button type="primary" @click="openCreate">新增参数</a-button>
      </a-space>

      <a-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1100 }"
      >
        <template #paramValue="{ record }">
          <a-typography-paragraph
            :copyable="Boolean(record.paramValue)"
            :ellipsis="{ rows: 2, expandable: true }"
            class="param-value"
          >
            {{ record.paramValue || '-' }}
          </a-typography-paragraph>
        </template>
        <template #status="{ record }">
          <a-tag :color="record.status === 1 ? 'green' : 'gray'">
            {{ record.status === 1 ? '启用' : '停用' }}
          </a-tag>
        </template>
        <template #updateTime="{ record }">
          {{ record.updateTime ? parseTime(record.updateTime) : '-' }}
        </template>
        <template #operations="{ record }">
          <a-space :size="4">
            <a-button type="text" size="mini" @click="openEdit(record)">
              编辑
            </a-button>
            <a-button
              type="text"
              size="mini"
              :status="record.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(record)"
            >
              {{ record.status === 1 ? '停用' : '启用' }}
            </a-button>
            <a-popconfirm content="确认删除该参数？" @ok="doDelete(record.id)">
              <a-button type="text" size="mini" status="danger">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="modalVisible"
      :title="form.id ? '编辑参数' : '新增参数'"
      :confirm-loading="saving"
      width="720px"
      @ok="doSave"
    >
      <a-form :model="form" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="14">
            <a-form-item field="paramKey" label="参数键" required>
              <a-input
                v-model="form.paramKey"
                allow-clear
                :max-length="96"
                placeholder="例如 site_name"
              />
            </a-form-item>
          </a-col>
          <a-col :span="5">
            <a-form-item field="sortOrder" label="排序">
              <a-input-number v-model="form.sortOrder" :min="0" />
            </a-form-item>
          </a-col>
          <a-col :span="5">
            <a-form-item field="status" label="状态">
              <a-select v-model="form.status">
                <a-option :value="1">启用</a-option>
                <a-option :value="0">停用</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item field="paramValue" label="参数值">
          <a-textarea
            v-model="form.paramValue"
            allow-clear
            :auto-size="{ minRows: 4, maxRows: 10 }"
            :max-length="2000"
          />
        </a-form-item>
        <a-form-item field="description" label="说明">
          <a-input v-model="form.description" allow-clear :max-length="255" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { reactive, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { parseTime } from '@/utils/dateUtils';
  import {
    SystemParam,
    SystemParamPayload,
    deleteSystemParam,
    saveSystemParam,
    systemParamList,
  } from '@/api/systemSetting';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'sys-param',
  });

  const columns: TableColumnData[] = [
    {
      title: '参数键',
      dataIndex: 'paramKey',
      align: 'center',
      ellipsis: true,
      tooltip: true,
      width: 220,
    },
    {
      title: '参数值',
      slotName: 'paramValue',
      align: 'center',
      width: 320,
    },
    {
      title: '说明',
      dataIndex: 'description',
      align: 'center',
      ellipsis: true,
      tooltip: true,
      width: 260,
    },
    {
      title: '排序',
      dataIndex: 'sortOrder',
      align: 'center',
      width: 90,
    },
    {
      title: '状态',
      slotName: 'status',
      align: 'center',
      width: 100,
    },
    {
      title: '更新时间',
      slotName: 'updateTime',
      align: 'center',
      width: 170,
    },
    {
      title: '操作',
      slotName: 'operations',
      align: 'center',
      fixed: 'right',
      width: 190,
    },
  ];

  const emptyForm = (): SystemParam => ({
    paramKey: '',
    paramValue: '',
    description: '',
    sortOrder: 0,
    status: 1,
  });

  const tableData = ref<SystemParam[]>([]);
  const loading = ref(false);
  const saving = ref(false);
  const modalVisible = ref(false);
  const keyword = ref('');
  const filterStatus = ref<number | undefined>();
  const form = reactive<SystemParam>(emptyForm());

  const assignForm = (item?: SystemParam) => {
    Object.assign(form, emptyForm(), item || {});
  };

  const loadData = () => {
    loading.value = true;
    systemParamList({ keyword: keyword.value, status: filterStatus.value })
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
    keyword.value = '';
    filterStatus.value = undefined;
    loadData();
  };

  const openCreate = () => {
    assignForm();
    modalVisible.value = true;
  };

  const openEdit = (item: SystemParam) => {
    assignForm(item);
    modalVisible.value = true;
  };

  const doSave = () => {
    if (!form.paramKey.trim()) {
      Message.warning('请填写参数键');
      return false;
    }
    saving.value = true;
    return saveSystemParam(payload(form))
      .then((rep) => {
        if (rep.success) {
          Message.success('保存成功');
          modalVisible.value = false;
          loadData();
        }
      })
      .finally(() => {
        saving.value = false;
      });
  };

  const toggleStatus = (item: SystemParam) => {
    saveSystemParam(payload({ ...item, status: item.status === 1 ? 0 : 1 }))
      .then((rep) => {
        if (rep.success) {
          Message.success('状态已更新');
          loadData();
        }
      });
  };

  const doDelete = (id?: number) => {
    if (!id) {
      return;
    }
    deleteSystemParam(id).then((rep) => {
      if (rep.success) {
        Message.success('删除成功');
        loadData();
      }
    });
  };

  const payload = (item: SystemParam): SystemParamPayload => ({
    id: item.id,
    paramKey: item.paramKey,
    paramValue: item.paramValue || '',
    description: item.description || '',
    sortOrder: item.sortOrder || 0,
    status: item.status,
  });

  loadData();
</script>

<style scoped>
  .param-value {
    margin-bottom: 0;
    text-align: left;
  }
</style>