<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.i18n-lang', 'menu.i18n-lang.lang-config']" />
    <a-alert type="info" style="margin-bottom: 16px">
      语言配置持久化在系统参数表：语言项使用 i18n.lang.*，默认语言使用 i18n.default。
    </a-alert>

    <a-card title="默认语言" style="margin-bottom: 16px">
      <a-space :size="12" wrap>
        <a-select
          v-model="defaultLanguage"
          placeholder="请选择默认语言"
          style="width: 260px"
          allow-clear
        >
          <a-option
            v-for="item in activeLanguageOptions"
            :key="item.value"
            :value="item.value"
          >
            {{ item.label }}
          </a-option>
        </a-select>
        <a-button
          type="primary"
          :loading="savingDefault"
          :disabled="!defaultLanguage"
          @click="saveDefaultLanguage"
        >
          保存默认语言
        </a-button>
        <a-typography-text type="secondary">
          仅启用语言可设为默认语言。
        </a-typography-text>
      </a-space>
    </a-card>

    <a-card>
      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-input
          v-model="keyword"
          allow-clear
          placeholder="语言编码/名称/说明"
          style="width: 240px"
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
        <a-button type="primary" @click="openCreate">新增语言</a-button>
      </a-space>

      <a-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1100 }"
      >
        <template #languageCode="{ record }">
          {{ languageCodeOf(record) }}
        </template>
        <template #paramKey="{ record }">
          <a-typography-paragraph
            :copyable="true"
            :ellipsis="{ rows: 1 }"
            class="param-value"
          >
            {{ record.paramKey }}
          </a-typography-paragraph>
        </template>
        <template #defaultFlag="{ record }">
          <a-tag v-if="languageCodeOf(record) === defaultLanguage" color="blue">
            默认
          </a-tag>
          <span v-else>-</span>
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
            <a-popconfirm content="确认删除该语言？" @ok="doDelete(record)">
              <a-button type="text" size="mini" status="danger">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="modalVisible"
      :title="form.id ? '编辑语言' : '新增语言'"
      :confirm-loading="saving"
      width="720px"
      @ok="doSave"
    >
      <a-form :model="form" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item field="languageCode" label="语言编码" required>
              <a-input
                v-model="form.languageCode"
                allow-clear
                :disabled="Boolean(form.id)"
                :max-length="32"
                placeholder="例如 zh-CN"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="displayName" label="展示名称" required>
              <a-input
                v-model="form.displayName"
                allow-clear
                :max-length="128"
                placeholder="例如 简体中文"
              />
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
        <a-form-item field="description" label="说明">
          <a-input v-model="form.description" allow-clear :max-length="255" />
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
    SystemParam,
    SystemParamPayload,
    deleteSystemParam,
    saveSystemParam,
    systemParamList,
  } from '@/api/systemSetting';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'lang-config',
  });

  interface LanguageForm {
    id?: number;
    languageCode: string;
    displayName: string;
    description: string;
    sortOrder: number;
    status: number;
  }

  const LANG_PREFIX = 'i18n.lang.';
  const DEFAULT_PARAM_KEY = 'i18n.default';
  const LANGUAGE_CODE_PATTERN = /^[A-Za-z0-9][A-Za-z0-9_-]{1,31}$/;

  const columns: TableColumnData[] = [
    {
      title: '语言编码',
      slotName: 'languageCode',
      align: 'center',
      width: 140,
    },
    {
      title: '参数键',
      slotName: 'paramKey',
      align: 'center',
      width: 230,
    },
    {
      title: '展示名称',
      dataIndex: 'paramValue',
      align: 'center',
      ellipsis: true,
      tooltip: true,
      width: 180,
    },
    {
      title: '默认',
      slotName: 'defaultFlag',
      align: 'center',
      width: 90,
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

  const emptyForm = (): LanguageForm => ({
    languageCode: '',
    displayName: '',
    description: '',
    sortOrder: 0,
    status: 1,
  });

  const rawParams = ref<SystemParam[]>([]);
  const loading = ref(false);
  const saving = ref(false);
  const savingDefault = ref(false);
  const modalVisible = ref(false);
  const keyword = ref('');
  const filterStatus = ref<number | undefined>();
  const defaultLanguage = ref('');
  const form = reactive<LanguageForm>(emptyForm());

  const languageCodeOf = (item: SystemParam) => item.paramKey.slice(LANG_PREFIX.length);
  const isLanguageParam = (item: SystemParam) => item.paramKey.startsWith(LANG_PREFIX);
  const toParamKey = (languageCode: string) => `${LANG_PREFIX}${languageCode}`;

  const languageParams = computed(() => rawParams.value.filter(isLanguageParam));
  const defaultParam = computed(() =>
    rawParams.value.find((item) => item.paramKey === DEFAULT_PARAM_KEY)
  );

  const tableData = computed(() => {
    const searchText = keyword.value.trim().toLowerCase();
    return languageParams.value.filter((item) => {
      const statusMatched = filterStatus.value === undefined || item.status === filterStatus.value;
      if (!statusMatched) {
        return false;
      }
      if (!searchText) {
        return true;
      }
      return [
        languageCodeOf(item),
        item.paramKey,
        item.paramValue,
        item.description,
      ].some((value) => (value || '').toLowerCase().includes(searchText));
    });
  });

  const activeLanguageOptions = computed(() =>
    languageParams.value
      .filter((item) => item.status === 1)
      .map((item) => {
        const value = languageCodeOf(item);
        return {
          value,
          label: `${value} - ${item.paramValue || value}`,
        };
      })
  );

  const assignForm = (item?: SystemParam) => {
    Object.assign(form, emptyForm());
    if (item) {
      Object.assign(form, {
        id: item.id,
        languageCode: languageCodeOf(item),
        displayName: item.paramValue || '',
        description: item.description || '',
        sortOrder: item.sortOrder || 0,
        status: item.status,
      });
    }
  };

  const syncDefaultLanguage = () => {
    defaultLanguage.value = defaultParam.value?.paramValue || '';
  };

  const loadData = () => {
    loading.value = true;
    systemParamList({ keyword: 'i18n.' })
      .then((rep) => {
        if (rep.success) {
          rawParams.value = (rep.data || []).filter(
            (item) => isLanguageParam(item) || item.paramKey === DEFAULT_PARAM_KEY
          );
          syncDefaultLanguage();
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

  const validateForm = () => {
    const languageCode = form.languageCode.trim();
    if (!LANGUAGE_CODE_PATTERN.test(languageCode)) {
      Message.warning('语言编码仅支持 2-32 位字母、数字、下划线和横线');
      return false;
    }
    if (!form.displayName.trim()) {
      Message.warning('请填写展示名称');
      return false;
    }
    if (languageCode === defaultLanguage.value && form.status !== 1) {
      Message.warning('默认语言不能停用');
      return false;
    }
    return true;
  };

  const languagePayload = (item: LanguageForm): SystemParamPayload => {
    const languageCode = item.languageCode.trim();
    return {
      id: item.id,
      paramKey: toParamKey(languageCode),
      paramValue: item.displayName.trim(),
      description: item.description.trim(),
      sortOrder: item.sortOrder || 0,
      status: item.status,
    };
  };

  const doSave = () => {
    if (!validateForm()) {
      return false;
    }
    saving.value = true;
    return saveSystemParam(languagePayload(form))
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
    if (languageCodeOf(item) === defaultLanguage.value && item.status === 1) {
      Message.warning('默认语言不能停用');
      return;
    }
    saveSystemParam({
      id: item.id,
      paramKey: item.paramKey,
      paramValue: item.paramValue || '',
      description: item.description || '',
      sortOrder: item.sortOrder || 0,
      status: item.status === 1 ? 0 : 1,
    }).then((rep) => {
      if (rep.success) {
        Message.success('状态已更新');
        loadData();
      }
    });
  };

  const doDelete = (item: SystemParam) => {
    if (!item.id) {
      return;
    }
    if (languageCodeOf(item) === defaultLanguage.value) {
      Message.warning('默认语言不能删除');
      return;
    }
    deleteSystemParam(item.id).then((rep) => {
      if (rep.success) {
        Message.success('删除成功');
        loadData();
      }
    });
  };

  const saveDefaultLanguage = () => {
    const selected = languageParams.value.find(
      (item) => languageCodeOf(item) === defaultLanguage.value && item.status === 1
    );
    if (!selected) {
      Message.warning('请选择已启用语言');
      return;
    }
    savingDefault.value = true;
    saveSystemParam({
      id: defaultParam.value?.id,
      paramKey: DEFAULT_PARAM_KEY,
      paramValue: defaultLanguage.value,
      description: '后台默认语言编码',
      sortOrder: 0,
      status: 1,
    })
      .then((rep) => {
        if (rep.success) {
          Message.success('默认语言已保存');
          loadData();
        }
      })
      .finally(() => {
        savingDefault.value = false;
      });
  };

  loadData();
</script>

<style scoped>
  .param-value {
    margin-bottom: 0;
    text-align: left;
  }
</style>