<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.sys-permission', 'menu.server-api-config']" />
    <a-card>
      <a-row style="margin-top: 10px">
        <a-col flex="auto" class="search_input">
          <a-input
            v-model="searchData.apiName"
            placeholder="请输入接口名称"
            allow-clear
          />
          <a-input
            v-model="searchData.apiUrl"
            placeholder="请输入接口地址"
            allow-clear
          />
          <a-select
            v-model="searchData.type"
            placeholder="请选择类型"
            allow-clear
          >
            <a-option
              v-for="code in typeMap.keys()"
              :key="code"
              :value="code"
              :label="typeMap.get(code)"
            />
          </a-select>
          <a-select
            v-model="searchData.status"
            placeholder="请选择状态"
            allow-clear
          >
            <a-option
              v-for="id in statusMap.keys()"
              :key="id"
              :value="id"
              :label="statusMap.get(id)"
            />
          </a-select>
        </a-col>
        <a-divider style="height: 35px" direction="vertical" />
        <a-col :flex="'50px'">
          <a-space :size="18">
            <a-button type="primary" @click="searchApiList">
              <template #icon>
                <icon-search />
              </template>
              {{ t('form.search') }}
            </a-button>
            <a-button @click="reset">
              <template #icon>
                <icon-refresh />
              </template>
              {{ t('form.reset') }}
            </a-button>
            <a-button status="warning" @click="openAddPage">
              <template #icon>
                <icon-user-add />
              </template>
              {{ t('form.add') }}
            </a-button>
            <ExcelTransfer
              file-name="接口资源"
              :im-method="uploadServerApi"
              :ex-method="downloadServerApi"
              @refresh-table="searchApiList"
            />
          </a-space>
        </a-col>
      </a-row>
      <a-divider style="margin-top: 20px" />
      <a-table
        :data="apiData"
        :columns="columns"
        :loading="loading"
        :scroll="{ y: '500px' }"
        :pagination="false"
      >
        <template #createTime="{ record }">
          {{ parseTime(record.createTime) }}
        </template>
        <template #updateTime="{ record }">
          {{ parseTime(record.updateTime) }}
        </template>
        <template #status="{ record }">
          {{ statusMap.get(record.status) }}
        </template>
        <template #type="{ record }">
          {{ typeMap.get(record.type) }}
        </template>
        <template #operations="{ record }">
          <a-button
            type="text"
            size="mini"
            status="warning"
            @click="openEditPage(record)"
          >
            {{ '编辑' }}
          </a-button>
          <a-popconfirm
            content="确定删除该接口吗？"
            @ok="submitDelete(record.id)"
          >
            <a-button type="text" size="mini" status="danger">
              {{ '删除' }}
            </a-button>
          </a-popconfirm>
        </template>
      </a-table>
      <a-pagination
        v-model:current="apiDataPage.pageNumber"
        v-model:page-size="apiDataPage.pageSize"
        :total="apiDataPage.totalRow"
        show-page-size
        class="pagination_style"
        @change="searchApiList"
        @page-size-change="searchApiList"
      />
    </a-card>

    <a-drawer v-model:visible="visible" :title="drawerFlag.title" :width="600">
      <a-form ref="formRef" layout="vertical" :model="editData" :rules="rules">
        <a-form-item field="apiName" label="接口名称">
          <a-input v-model="editData.apiName" placeholder="请输入接口名称" />
        </a-form-item>
        <a-form-item field="apiUrl" label="接口地址">
          <a-input v-model="editData.apiUrl" placeholder="请输入接口地址" />
        </a-form-item>
        <a-form-item field="method" label="请求方式">
          <a-select v-model="editData.method" placeholder="请选择请求方式">
            <a-option
              v-for="way in methodList"
              :key="way"
              :value="way"
              :label="way"
            />
          </a-select>
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-radio-group v-model="editData.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item field="type" label="类型">
          <a-select v-model="editData.type" placeholder="请选择类型">
            <a-option
              v-for="code in typeMap.keys()"
              :key="code"
              :value="code"
              :label="typeMap.get(code)"
            />
          </a-select>
        </a-form-item>
        <a-form-item field="remark" label="备注">
          <a-textarea
            v-model="editData.remark"
            placeholder="请输入备注"
            :auto-size="{ minRows: 3, maxRows: 5 }"
          />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-button status="success" @click="submitEdit">确定</a-button>
        <a-button @click="visible = false">关闭</a-button>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { computed, reactive, ref } from 'vue';
  import {
    addApi,
    allApiType,
    ApiSearch,
    deleteApi,
    downloadServerApi,
    selectApi,
    ServerApi,
    updateApi,
    uploadServerApi,
  } from '@/api/api';
  import PageData from '@/model/pageData';
  import useLoading from '@/hooks/loading';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { parseTime } from '@/utils/dateUtils';
  import { Message } from '@arco-design/web-vue';
  import ExcelTransfer from '@/components/excel-transfer/excelTransfer.vue';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'server-api-config',
  });

  const { t } = useI18n();
  const { loading, setLoading } = useLoading(true);
  const visible = ref(false);
  const drawerFlag = ref({
    title: '',
    type: 1,
  });
  const formRef = ref();
  const rules = {
    apiName: [{ required: true, message: '请填写接口名称' }],
    apiUrl: [{ required: true, message: '请填写接口地址' }],
    method: [{ required: true, message: '请选择请求方式' }],
    status: [{ required: true, message: '请选择状态' }],
    type: [{ required: true, message: '请选择类型' }],
  };

  const searchData = reactive(new ApiSearch({}));

  const apiData = reactive(new Array<ServerApi>());
  const apiDataPage = reactive(new PageData());

  const editData = reactive(new ServerApi({}));

  const columns = computed<TableColumnData[]>(() => [
    {
      title: t('server-api-config.columns.apiName'),
      dataIndex: 'apiName',
      slotName: 'apiName',
      align: 'center',
      width: 280,
    },
    {
      title: t('server-api-config.columns.apiUrl'),
      dataIndex: 'apiUrl',
      slotName: 'apiUrl',
      align: 'center',
      width: 380,
      ellipsis: true,
      tooltip: true,
    },
    {
      title: t('server-api-config.columns.method'),
      dataIndex: 'method',
      slotName: 'method',
      align: 'center',
      width: 100,
    },
    {
      title: t('server-api-config.columns.status'),
      dataIndex: 'status',
      slotName: 'status',
      align: 'center',
      width: 80,
    },
    {
      title: t('server-api-config.columns.type'),
      dataIndex: 'type',
      slotName: 'type',
      align: 'center',
      width: 150,
    },
    {
      title: t('server-api-config.columns.remark'),
      dataIndex: 'remark',
      slotName: 'remark',
      align: 'center',
      width: 200,
    },
    {
      title: t('server-api-config.columns.createTime'),
      dataIndex: 'createTime',
      slotName: 'createTime',
      align: 'center',
      width: 200,
    },
    {
      title: t('server-api-config.columns.updateTime'),
      dataIndex: 'updateTime',
      slotName: 'updateTime',
      align: 'center',
      width: 200,
    },
    {
      title: t('columns.operations'),
      dataIndex: 'operations',
      slotName: 'operations',
      align: 'center',
      width: 250,
      fixed: 'right',
    },
  ]);

  const statusMap = reactive(
    new Map<number, string>([
      [0, '禁用'],
      [1, '启用'],
    ])
  );
  const methodList = ['GET', 'POST', 'PUT', 'DELETE'];
  const typeMap = reactive(new Map<number, string>([]));

  function searchApiType() {
    allApiType().then((rep) => {
      if (rep.success) {
        Object.entries(rep.data).forEach(([k, v]) => {
          typeMap.set(Number(k), v);
        });
      }
    });
  }
  searchApiType();

  async function searchApiList() {
    apiData.splice(0);
    searchData.pageNumber = apiDataPage.pageNumber;
    searchData.pageSize = apiDataPage.pageSize;
    setLoading(true);
    await selectApi(searchData).then((rep) => {
      if (rep.success) {
        apiData.push(...(rep.data.records || []));
        apiDataPage.updatePage(rep.data);
      }
      setLoading(false);
    });
  }
  searchApiList();

  function reset() {
    searchData.update(new ApiSearch({}));
    apiDataPage.pageNumber = 1;
    apiDataPage.pageSize = 10;
    searchApiList();
  }

  function openAddPage() {
    editData.update(new ServerApi({}));
    drawerFlag.value.title = '新增接口';
    drawerFlag.value.type = 1;
    visible.value = true;
  }

  function openEditPage(row: ServerApi) {
    editData.update(row);
    drawerFlag.value.title = '编辑接口';
    drawerFlag.value.type = 2;
    visible.value = true;
  }

  function submitEdit() {
    formRef.value.validate((valid: any) => {
      if (valid !== undefined) {
        Message.warning('请按要求填写');
        return;
      }

      if (drawerFlag.value.type === 1) {
        addApi(editData).then(async (rep) => {
          if (rep.success) {
            Message.success('新增接口成功');
            await searchApiList();
            visible.value = false;
          }
        });
      } else {
        updateApi(editData).then(async (rep) => {
          if (rep.success) {
            Message.success('修改接口成功');
            await searchApiList();
            visible.value = false;
          }
        });
      }
    });
  }

  function submitDelete(id: number) {
    deleteApi(id).then(async (rep) => {
      if (rep.success) {
        Message.success('删除接口成功');
        await searchApiList();
        visible.value = false;
      }
    });
  }
</script>

<style scoped lang="scss"></style>
