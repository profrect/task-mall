<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.log', 'menu.user-log']" />
    <a-card>
      <a-row style="margin-top: 10px">
        <a-col flex="auto" class="search_input">
          <a-select
            v-model="searchUserLogParam.type"
            placeholder="请选择操作类型"
            allow-clear
          >
            <a-option
              v-for="d in userLogType.keys()"
              :key="d"
              :value="d"
              :label="userLogType.get(d)"
            />
          </a-select>
        </a-col>
        <a-divider style="height: 35px" direction="vertical" />
        <a-col :flex="'50px'">
          <a-space :size="18">
            <a-button type="primary" @click="searchUserLogMethod">
              <template #icon>
                <icon-search />
              </template>
              {{ t('user-log.form.search') }}
            </a-button>
            <a-button @click="reset">
              <template #icon>
                <icon-refresh />
              </template>
              {{ t('user-log.form.reset') }}
            </a-button>
          </a-space>
        </a-col>
      </a-row>
      <a-divider style="margin-top: 20px" />
      <a-table
        :data="userLogTableData"
        :columns="columns"
        :pagination="false"
        :scroll="{ y: '500px' }"
      >
        <template #type="{ record }">
          {{ userLogType.get(record.type) }}
        </template>
        <template #result="{ record }">
          <status-font :status="record.result" />
        </template>
      </a-table>
      <a-pagination
        v-model:current="userLogTablePageData.pageNumber"
        v-model:page-size="userLogTablePageData.pageSize"
        :total="userLogTablePageData.totalRow"
        show-page-size
        class="pagination_style"
        @change="searchUserLogMethod"
        @page-size-change="searchUserLogMethod"
      />
    </a-card>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { computed, reactive } from 'vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import {
    searchUserLog,
    searchUserLogType,
    UserLog,
    UserLogType,
  } from '@/api/userLog';
  import PageData from '@/model/pageData';
  import StatusFont from '@/components/status-font/statusFont.vue';

  const { t } = useI18n();

  const searchUserLogParam = reactive({
    type: null,
  });

  const userLogTableData = reactive(new Array<UserLog>());
  const userLogTablePageData = reactive(new PageData());
  const userLogType = reactive(new Map<number, string>());

  const columns = computed<TableColumnData[]>(() => [
    {
      title: t('user-log.columns.name'),
      dataIndex: 'name',
      slotName: 'name',
      align: 'center',
    },
    {
      title: t('user-log.columns.type'),
      dataIndex: 'type',
      slotName: 'type',
      align: 'center',
    },
    {
      title: t('user-log.columns.result'),
      dataIndex: 'result',
      slotName: 'result',
      align: 'center',
    },
    {
      title: t('user-log.columns.ip'),
      dataIndex: 'ip',
      slotName: 'ip',
      align: 'center',
    },
  ]);

  function searchUserLogTypeMethod() {
    searchUserLogType().then((rep) => {
      if (!rep.success) {
        return;
      }

      userLogType.clear();
      rep.data.forEach((item: UserLogType) => {
        userLogType.set(item.type, item.message);
      });
    });
  }
  searchUserLogTypeMethod();

  function searchUserLogMethod() {
    const searchUserLogParamMap = new Map<string, any>();
    searchUserLogParamMap.set('type', searchUserLogParam.type);
    searchUserLogParamMap.set(
      'pageSize',
      userLogTablePageData.pageSize.toString()
    );
    searchUserLogParamMap.set(
      'pageNum',
      userLogTablePageData.pageNumber.toString()
    );
    searchUserLog(searchUserLogParamMap).then((rep) => {
      if (!rep.success) {
        return;
      }

      userLogTableData.splice(0);
      rep.data.records?.forEach((item: UserLog) => {
        userLogTableData.push(item);
      });

      userLogTablePageData.updatePageData(rep.data.pageData);
    });
  }
  searchUserLogMethod();

  function reset() {
    searchUserLogParam.type = null;
    userLogTablePageData.pageNumber = 1;
    userLogTablePageData.pageSize = 10;
    searchUserLogMethod();
  }
</script>

<style scoped lang="scss"></style>
