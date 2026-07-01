<template>
  <a-card :loading="loading" class="content-editor">
    <template #title>{{ title }}</template>
    <template #extra>
      <a-space :size="12">
        <span class="content-meta">
          更新时间：{{ form.updateTime ? parseTime(form.updateTime) : '-' }}
        </span>
        <a-button @click="loadData">
          <template #icon>
            <icon-refresh />
          </template>
          刷新
        </a-button>
        <a-button type="primary" :loading="saving" @click="doSave">
          保存
        </a-button>
      </a-space>
    </template>

    <a-form :model="form" layout="vertical">
      <a-row :gutter="16">
        <a-col :span="16">
          <a-form-item field="title" label="标题" required>
            <a-input v-model="form.title" allow-clear :max-length="128" />
          </a-form-item>
        </a-col>
        <a-col :span="4">
          <a-form-item field="languageCode" label="语言">
            <a-input v-model="form.languageCode" allow-clear />
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
          :auto-size="{ minRows: 14, maxRows: 24 }"
        />
      </a-form-item>
    </a-form>
  </a-card>
</template>

<script setup lang="ts">
  import { reactive, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { parseTime } from '@/utils/dateUtils';
  import {
    ContentItem,
    ContentItemPayload,
    ContentItemType,
    contentList,
    saveContent,
  } from '@/api/content';

  const props = defineProps<{
    type: ContentItemType;
    title: string;
    defaultTitle: string;
  }>();

  const emptyForm = (): ContentItem => ({
    type: props.type,
    languageCode: 'zh-CN',
    title: props.defaultTitle,
    summary: '',
    content: '',
    sortOrder: 0,
    status: 1,
  });

  const form = reactive<ContentItem>(emptyForm());
  const loading = ref(false);
  const saving = ref(false);

  const assignForm = (data?: ContentItem) => {
    Object.assign(form, emptyForm(), data || {});
  };

  const loadData = () => {
    loading.value = true;
    contentList({ type: props.type, languageCode: form.languageCode })
      .then((rep) => {
        if (rep.success) {
          assignForm(rep.data?.[0]);
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  const doSave = () => {
    if (!form.title.trim() || !form.content.trim()) {
      Message.warning('请填写标题和正文');
      return;
    }
    const payload: ContentItemPayload = {
      id: form.id,
      type: props.type,
      languageCode: form.languageCode || 'zh-CN',
      title: form.title,
      summary: form.summary || '',
      content: form.content,
      sortOrder: form.sortOrder || 0,
      status: form.status,
    };
    saving.value = true;
    saveContent(payload)
      .then((rep) => {
        if (rep.success && rep.data) {
          assignForm(rep.data);
          Message.success('保存成功');
        }
      })
      .finally(() => {
        saving.value = false;
      });
  };

  loadData();
</script>

<style scoped>
  .content-editor {
    min-height: 520px;
  }

  .content-meta {
    color: var(--color-text-3);
    font-size: 13px;
  }
</style>