<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.user-member', 'menu.member-list']" />
    <a-card>
      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-button type="primary" @click="fetchData">
          <template #icon>
            <icon-refresh />
          </template>
          刷新
        </a-button>
      </a-space>
      <a-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1260 }"
      >
        <template #vipLevel="{ record }">
          <a-tag color="arcoblue">VIP {{ record.vipLevel ?? 0 }}</a-tag>
        </template>
        <template #status="{ record }">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ statusText(record.status) }}
          </a-tag>
        </template>
        <template #availableAmt="{ record }">
          {{ moneyText(record.availableAmt) }}
        </template>
        <template #freezeAmt="{ record }">
          {{ moneyText(record.freezeAmt) }}
        </template>
        <template #registerTime="{ record }">
          {{ record.registerTime ? parseTime(record.registerTime) : '-' }}
        </template>
        <template #operations="{ record }">
          <a-space :size="4">
            <a-button
              type="text"
              size="mini"
              status="warning"
              :disabled="record.status !== 1"
              :loading="impersonatingUserId === record.userId"
              @click="openImpersonation(record)"
            >
              模拟登录
            </a-button>
            <a-popconfirm
              v-if="record.status === 1"
              content="确认冻结该会员？冻结后该会员当前登录态会失效。"
              @ok="changeStatus(record, 2)"
            >
              <a-button type="text" size="mini" status="danger">冻结</a-button>
            </a-popconfirm>
            <a-popconfirm
              v-else
              content="确认恢复该会员为正常状态？"
              @ok="changeStatus(record, 1)"
            >
              <a-button type="text" size="mini" status="success">解冻</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
      <a-pagination
        v-model:current="page.pageNumber"
        v-model:page-size="page.pageSize"
        :total="page.totalRow"
        show-page-size
        class="pagination_style"
        @change="fetchData"
        @page-size-change="fetchData"
      />
    </a-card>
  </div>
</template>

<script setup lang="ts">
  import { reactive, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { parseTime } from '@/utils/dateUtils';
  import { openWindow } from '@/utils';
  import PageData from '@/model/pageData';
  import {
    createMemberImpersonationTicket,
    queryMemberList,
    updateMemberStatus,
    MemberUser,
  } from '@/api/member';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'member-list',
  });

  const columns: TableColumnData[] = [
    {
      title: '用户ID',
      dataIndex: 'userId',
      align: 'center',
      width: 110,
    },
    {
      title: '账号',
      dataIndex: 'userName',
      align: 'center',
      width: 140,
    },
    {
      title: 'VIP等级',
      slotName: 'vipLevel',
      align: 'center',
      width: 100,
    },
    {
      title: '邀请码',
      dataIndex: 'inviteCode',
      align: 'center',
      width: 120,
    },
    {
      title: '上级账号',
      dataIndex: 'parentUserName',
      align: 'center',
      width: 140,
    },
    {
      title: '可用余额',
      slotName: 'availableAmt',
      align: 'center',
      width: 120,
    },
    {
      title: '冻结金额',
      slotName: 'freezeAmt',
      align: 'center',
      width: 120,
    },
    {
      title: '状态',
      slotName: 'status',
      align: 'center',
      width: 90,
    },
    {
      title: '注册时间',
      slotName: 'registerTime',
      align: 'center',
      width: 170,
    },
    {
      title: '操作',
      slotName: 'operations',
      align: 'center',
      fixed: 'right',
      width: 180,
    },
  ];

  const tableData = ref<MemberUser[]>([]);
  const loading = ref(false);
  const statusUpdating = ref(false);
  const impersonatingUserId = ref<number | null>(null);
  const page = reactive(new PageData());
  const h5BaseUrl = (import.meta.env.VITE_H5_BASE_URL || 'http://127.0.0.1:5173').replace(/\/$/, '');

  const fetchData = () => {
    loading.value = true;
    const params = new Map<string, string>();
    params.set('pageNumber', String(page.pageNumber));
    params.set('pageSize', String(page.pageSize));
    queryMemberList(params)
      .then((rep) => {
        if (rep.success) {
          tableData.value = rep.data.records || [];
          page.updatePage(rep.data);
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  const changeStatus = (record: MemberUser, status: number) => {
    if (statusUpdating.value) {
      return;
    }
    statusUpdating.value = true;
    updateMemberStatus({
      userId: record.userId,
      status,
    })
      .then((rep) => {
        if (rep.success) {
          fetchData();
        }
      })
      .finally(() => {
        statusUpdating.value = false;
      });
  };

  const openImpersonation = (record: MemberUser) => {
    if (record.status !== 1 || impersonatingUserId.value) {
      return;
    }
    impersonatingUserId.value = record.userId;
    createMemberImpersonationTicket({ id: record.userId })
      .then((rep) => {
        if (rep.success && rep.data?.ticket) {
          const ticket = encodeURIComponent(rep.data.ticket);
          const redirect = encodeURIComponent('/profile');
          openWindow(`${h5BaseUrl}/#/admin-login?ticket=${ticket}&redirect=${redirect}`, {
            target: '_blank',
          });
          Message.success('模拟登录票据已生成，请在新窗口查看前台');
        }
      })
      .finally(() => {
        impersonatingUserId.value = null;
      });
  };

  fetchData();

  function statusText(status: number): string {
    const map: Record<number, string> = {
      1: '正常',
      2: '冻结',
    };
    return map[status] || '未知';
  }

  function moneyText(amount: number): string {
    return Number(amount || 0).toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 6,
    });
  }
</script>