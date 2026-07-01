<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.content-setting', 'menu.content-setting.notices']" />
    <a-card>
      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-select
          v-model="filterStatus"
          style="width: 140px"
          @change="loadData"
        >
          <a-option :value="undefined">全部</a-option>
          <a-option :value="1">启用</a-option>
          <a-option :value="0">停用</a-option>
        </a-select>
        <a-button @click="loadData">
          <template #icon>
            <icon-refresh />
          </template>
          刷新
        </a-button>
        <a-button type="primary" @click="openCreate">新增公告</a-button>
      </a-space>

      <a-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1100 }"
      >
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
            <a-popconfirm content="确认删除该公告？" @ok="doDelete(record.id)">
              <a-button type="text" size="mini" status="danger">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="modalVisible"
      :title="form.id ? '编辑公告' : '新增公告'"
      :confirm-loading="saving"
      width="760px"
      @ok="doSave"
    >
      <a-form :model="form" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="16">
            <a-form-item field="title" label="标题" required>
              <a-input v-model="form.title" allow-clear :max-length="128" />
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <a-form-item field="sortOrder" label="排序">
              <a-input-number v-model="form.sortOrder" :min="0" />
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <a-form-item field="status" label="状态">
              <a-select v-model="form.status">
                <a-option :value="1">启用</a-option>
                <a-option :value="0">停用</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item field="summary" label="摘要">
          <a-textarea
            v-model="form.summary"
            allow-clear
            :auto-size="{ minRows: 2, maxRows: 4 }"
            :max-length="255"
          />
        </a-form-item>
        <a-form-item field="content" label="正文" required>
          <a-textarea
            v-model="form.content"
            allow-clear
            :auto-size="{ minRows: 8, maxRows: 16 }"
          />
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
    ContentItem,
    ContentItemPayload,
    contentList,
    deleteContent,
    saveContent,
  } from '@/api/content';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'content-notices',
  });

  const columns: TableColumnData[] = [
    {
      title: '标题',
      dataIndex: 'title',
      align: 'center',
      ellipsis: true,
      tooltip: true,
      width: 220,
    },
    {
      title: '摘要',
      dataIndex: 'summary',
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

  const emptyForm = (): ContentItem => ({
    type: 'NOTICE',
    languageCode: 'zh-CN',
    title: '',
    summary: '',
    content: '',
    sortOrder: 0,
    status: 1,
  });

  const tableData = ref<ContentItem[]>([]);
  const loading = ref(false);
  const saving = ref(false);
  const modalVisible = ref(false);
  const filterStatus = ref<number | undefined>();
  const form = reactive<ContentItem>(emptyForm());

  const assignForm = (item?: ContentItem) => {
    Object.assign(form, emptyForm(), item || {});
  };

  const loadData = () => {
    loading.value = true;
    contentList({ type: 'NOTICE', status: filterStatus.value })
      .then((rep) => {
        if (rep.success) {
          tableData.value = rep.data || [];
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  const openCreate = () => {
    assignForm();
    modalVisible.value = true;
  };

  const openEdit = (item: ContentItem) => {
    assignForm(item);
    modalVisible.value = true;
  };

  const doSave = () => {
    if (!form.title.trim() || !form.content.trim()) {
      Message.warning('请填写标题和正文');
      return false;
    }
    saving.value = true;
    return saveContent(payload(form))
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

  const toggleStatus = (item: ContentItem) => {
    saveContent(payload({ ...item, status: item.status === 1 ? 0 : 1 }))
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
    deleteContent(id).then((rep) => {
      if (rep.success) {
        Message.success('删除成功');
        loadData();
      }
    });
  };

  const payload = (item: ContentItem): ContentItemPayload => ({
    id: item.id,
    type: 'NOTICE',
    languageCode: item.languageCode || 'zh-CN',
    title: item.title,
    summary: item.summary || '',
    content: item.content,
    sortOrder: item.sortOrder || 0,
    status: item.status,
  });

  loadData();
</script>

<style scoped></style>