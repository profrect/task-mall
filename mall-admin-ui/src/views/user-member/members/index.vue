<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.user-member', 'menu.member-list']" />
    <a-card>
      <a-tabs
        v-model:active-key="activeVipTab"
        class="member-vip-tabs"
        @change="handleVipTabChange"
      >
        <a-tab-pane
          v-for="tab in vipTabs"
          :key="tab.value"
          :title="tab.label"
        />
      </a-tabs>

      <a-form :model="queryForm" layout="inline" class="member-search-form">
        <a-form-item field="userId" label="UID">
          <a-input
            v-model="queryForm.userId"
            allow-clear
            placeholder="用户ID"
            style="width: 150px"
            @press-enter="handleSearch"
          />
        </a-form-item>
        <a-form-item field="userName" label="账号">
          <a-input
            v-model="queryForm.userName"
            allow-clear
            placeholder="登录账号"
            style="width: 160px"
            @press-enter="handleSearch"
          />
        </a-form-item>
        <a-form-item field="inviteCode" label="邀请码">
          <a-input
            v-model="queryForm.inviteCode"
            allow-clear
            placeholder="邀请码"
            style="width: 150px"
            @press-enter="handleSearch"
          />
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-select
            v-model="queryForm.status"
            allow-clear
            placeholder="全部"
            style="width: 130px"
          >
            <a-option :value="1">正常</a-option>
            <a-option :value="2">冻结</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="groupId" label="分组">
          <a-select
            v-model="queryForm.groupId"
            allow-clear
            placeholder="全部分组"
            style="width: 150px"
          >
            <a-option v-for="item in groupOptions" :key="item.id" :value="item.id">
              {{ item.groupName }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="registerTimeRange" label="注册时间">
          <a-range-picker
            v-model="queryForm.registerTimeRange"
            show-time
            value-format="timestamp"
            style="width: 360px"
          />
        </a-form-item>
        <a-form-item>
          <a-space :size="8">
            <a-button type="primary" @click="handleSearch">查询</a-button>
            <a-button @click="resetSearch">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-button type="primary" @click="fetchData">
          <template #icon>
            <icon-refresh />
          </template>
          刷新
        </a-button>
        <a-button type="primary" @click="openCreateMember">添加</a-button>
        <a-popconfirm content="确认批量冻结选中会员？冻结后登录态会失效。" @ok="handleBatchStatus(2)">
          <a-button status="danger" :disabled="!selectedRowKeys.length" :loading="batchLoading">
            批量封禁
          </a-button>
        </a-popconfirm>
        <a-popconfirm content="确认批量恢复选中会员为正常状态？" @ok="handleBatchStatus(1)">
          <a-button status="success" :disabled="!selectedRowKeys.length" :loading="batchLoading">
            批量解封
          </a-button>
        </a-popconfirm>
        <a-popconfirm content="确认强制下线选中会员？" @ok="handleBatchLogout">
          <a-button :disabled="!selectedRowKeys.length" :loading="batchLoading">
            批量下线
          </a-button>
        </a-popconfirm>
        <a-button :disabled="!selectedRowKeys.length" @click="openGroupAssign">
          设置分组
        </a-button>
        <a-button :loading="exporting" @click="handleExport">
          导出会员
        </a-button>
      </a-space>

      <a-table
        v-model:selected-keys="selectedRowKeys"
        row-key="userId"
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="false"
        :row-selection="rowSelection"
        :scroll="{ x: 1840 }"
      >
        <template #vipLevel="{ record }">
          <a-tag color="arcoblue">VIP {{ record.vipLevel ?? 0 }}</a-tag>
        </template>
        <template #status="{ record }">
          <a-tag :color="record.status === 1 ? 'green' : 'red'">
            {{ statusText(record.status) }}
          </a-tag>
        </template>
        <template #groupName="{ record }">
          {{ record.groupName || '-' }}
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
          <a-space :size="4" wrap>
            <a-button type="text" size="mini" @click="openDetail(record)">
              详情
            </a-button>
            <a-button type="text" size="mini" @click="openEditMember(record)">
              编辑
            </a-button>
            <a-button type="text" size="mini" @click="openLineage(record)">
              查线
            </a-button>
            <a-button type="text" size="mini" @click="openParentChange(record)">
              改线
            </a-button>
            <a-button type="text" size="mini" @click="openManualRecharge(record)">
              补单
            </a-button>
            <a-button
              type="text"
              size="mini"
              :loading="downloadRechargeUserId === record.userId"
              @click="downloadRecharge(record)"
            >
              下载充值
            </a-button>
            <a-popconfirm content="确认强制下线该会员？" @ok="forceLogout(record)">
              <a-button
                type="text"
                size="mini"
                :loading="logoutUserId === record.userId"
              >
                下线
              </a-button>
            </a-popconfirm>
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

    <a-modal
      v-model:visible="detailVisible"
      title="会员详情"
      :footer="false"
      :width="760"
      :unmount-on-close="true"
    >
      <a-spin :loading="detailLoading">
        <div v-if="detailData" class="member-detail-grid">
          <div class="member-detail-item">
            <span>UID</span>
            <strong>{{ detailData.userId }}</strong>
          </div>
          <div class="member-detail-item">
            <span>账号</span>
            <strong>{{ detailData.userName || '-' }}</strong>
          </div>
          <div class="member-detail-item">
            <span>昵称</span>
            <strong>{{ detailData.nickname || '-' }}</strong>
          </div>
          <div class="member-detail-item">
            <span>邮箱</span>
            <strong>{{ detailData.email || '-' }}</strong>
          </div>
          <div class="member-detail-item">
            <span>VIP</span>
            <strong>VIP {{ detailData.vipLevel ?? 0 }}</strong>
          </div>
          <div class="member-detail-item">
            <span>状态</span>
            <strong>{{ statusText(detailData.status) }}</strong>
          </div>
          <div class="member-detail-item">
            <span>邀请码</span>
            <strong>{{ detailData.inviteCode || '-' }}</strong>
          </div>
          <div class="member-detail-item">
            <span>分组</span>
            <strong>{{ detailData.groupName || '-' }}</strong>
          </div>
          <div class="member-detail-item">
            <span>上级</span>
            <strong>
              {{ detailData.parentUserName || '-' }}
              <template v-if="detailData.parentUserId">
                （{{ detailData.parentUserId }}）
              </template>
            </strong>
          </div>
          <div class="member-detail-item">
            <span>可用余额</span>
            <strong>{{ moneyText(detailData.availableAmt) }}</strong>
          </div>
          <div class="member-detail-item">
            <span>冻结金额</span>
            <strong>{{ moneyText(detailData.freezeAmt) }}</strong>
          </div>
          <div class="member-detail-item wide">
            <span>注册时间</span>
            <strong>{{ detailData.registerTime ? parseTime(detailData.registerTime) : '-' }}</strong>
          </div>
        </div>
      </a-spin>
    </a-modal>

    <a-modal
      v-model:visible="memberModalVisible"
      :title="memberForm.userId ? '编辑会员' : '添加会员'"
      :footer="false"
      :width="640"
      :unmount-on-close="true"
    >
      <a-form :model="memberForm" layout="vertical">
        <a-form-item field="userName" label="登录账号" required>
          <a-input v-model="memberForm.userName" allow-clear placeholder="请输入登录账号" />
        </a-form-item>
        <a-form-item field="password" :label="memberForm.userId ? '重置密码' : '登录密码'" :required="!memberForm.userId">
          <a-input-password
            v-model="memberForm.password"
            allow-clear
            :placeholder="memberForm.userId ? '留空表示不修改密码' : '请输入登录密码'"
          />
        </a-form-item>
        <a-form-item field="nickname" label="昵称">
          <a-input v-model="memberForm.nickname" allow-clear placeholder="默认使用账号" />
        </a-form-item>
        <a-form-item field="email" label="邮箱">
          <a-input v-model="memberForm.email" allow-clear placeholder="可选" />
        </a-form-item>
        <a-form-item field="vipLevel" label="VIP等级">
          <a-input-number v-model="memberForm.vipLevel" :min="0" :max="9" style="width: 100%" />
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-select v-model="memberForm.status" placeholder="请选择状态">
            <a-option :value="1">正常</a-option>
            <a-option :value="2">冻结</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="parentUserId" label="上级 UID">
          <a-input-number
            v-model="memberForm.parentUserId"
            :min="1"
            allow-clear
            placeholder="可选；编辑时留空表示不调整上级"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item field="groupId" label="分组">
          <a-select v-model="memberForm.groupId" allow-clear placeholder="不绑定分组">
            <a-option v-for="item in groupOptions" :key="item.id" :value="item.id">
              {{ item.groupName }}
            </a-option>
          </a-select>
        </a-form-item>
      </a-form>
      <div class="modal-footer">
        <a-button @click="memberModalVisible = false">取消</a-button>
        <a-button type="primary" :loading="memberSaving" @click="submitMember">
          保存
        </a-button>
      </div>
    </a-modal>

    <a-modal
      v-model:visible="groupAssignVisible"
      title="设置分组"
      :footer="false"
      :width="460"
      :unmount-on-close="true"
    >
      <a-alert type="info" style="margin-bottom: 16px">
        已选择 {{ selectedRowKeys.length }} 个会员。清空分组后保存表示移出当前分组。
      </a-alert>
      <a-form :model="groupAssignForm" layout="vertical">
        <a-form-item field="groupId" label="目标分组">
          <a-select v-model="groupAssignForm.groupId" allow-clear placeholder="移出分组">
            <a-option v-for="item in groupOptions" :key="item.id" :value="item.id">
              {{ item.groupName }}
            </a-option>
          </a-select>
        </a-form-item>
      </a-form>
      <div class="modal-footer">
        <a-button @click="groupAssignVisible = false">取消</a-button>
        <a-button type="primary" :loading="groupAssigning" @click="submitGroupAssign">
          保存
        </a-button>
      </div>
    </a-modal>

    <a-modal
      v-model:visible="lineageVisible"
      title="会员链路"
      :footer="false"
      :width="820"
      :unmount-on-close="true"
    >
      <a-spin :loading="lineageLoading">
        <a-table
          row-key="userId"
          :columns="lineageColumns"
          :data="lineageRows"
          :pagination="false"
        >
          <template #relation="{ record }">
            {{ relationText(record.relation, record.depth) }}
          </template>
          <template #status="{ record }">
            <a-tag :color="record.status === 1 ? 'green' : 'red'">
              {{ statusText(record.status) }}
            </a-tag>
          </template>
        </a-table>
      </a-spin>
    </a-modal>

    <a-modal
      v-model:visible="parentChangeVisible"
      title="改线"
      :footer="false"
      :width="460"
      :unmount-on-close="true"
    >
      <a-alert type="warning" style="margin-bottom: 16px">
        仅允许调整到已存在会员，且不能调整到自己的下级链路中。
      </a-alert>
      <a-form :model="parentChangeForm" layout="vertical">
        <a-form-item field="userId" label="会员 UID">
          <a-input-number v-model="parentChangeForm.userId" disabled style="width: 100%" />
        </a-form-item>
        <a-form-item field="parentUserId" label="新上级 UID" required>
          <a-input-number
            v-model="parentChangeForm.parentUserId"
            :min="1"
            placeholder="请输入新上级 UID"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
      <div class="modal-footer">
        <a-button @click="parentChangeVisible = false">取消</a-button>
        <a-button type="primary" :loading="parentChanging" @click="submitParentChange">
          保存
        </a-button>
      </div>
    </a-modal>

    <a-modal
      v-model:visible="manualRechargeVisible"
      title="充值补单"
      :footer="false"
      :width="520"
      :unmount-on-close="true"
    >
      <a-alert type="warning" style="margin-bottom: 16px">
        补单会生成 MANUAL 充值订单、支付审计和钱包入账流水；请确认金额和凭证无误。
      </a-alert>
      <a-form :model="manualRechargeForm" layout="vertical">
        <a-form-item field="userId" label="会员 UID">
          <a-input-number v-model="manualRechargeForm.userId" disabled style="width: 100%" />
        </a-form-item>
        <a-form-item field="userName" label="会员账号">
          <a-input v-model="manualRechargeForm.userName" disabled />
        </a-form-item>
        <a-form-item field="amount" label="入账金额" required>
          <a-input-number
            v-model="manualRechargeForm.amount"
            :min="0.000001"
            :precision="6"
            placeholder="请输入补单入账金额"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item field="coin" label="币种">
          <a-input v-model="manualRechargeForm.coin" disabled />
        </a-form-item>
        <a-form-item field="referenceNo" label="凭证号">
          <a-input
            v-model="manualRechargeForm.referenceNo"
            allow-clear
            :max-length="64"
            placeholder="线下订单号/支付流水号/客服记录号"
          />
        </a-form-item>
        <a-form-item field="remark" label="补单备注">
          <a-textarea
            v-model="manualRechargeForm.remark"
            allow-clear
            :max-length="512"
            placeholder="请输入补单原因或审核说明"
          />
        </a-form-item>
      </a-form>
      <div class="modal-footer">
        <a-button @click="manualRechargeVisible = false">取消</a-button>
        <a-button type="primary" status="warning" :loading="manualRechargeSubmitting" @click="submitManualRecharge">
          确认补单
        </a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { parseTime } from '@/utils/dateUtils';
  import { openWindow } from '@/utils';
  import PageData from '@/model/pageData';
  import {
    assignMemberGroup,
    changeMemberParent,
    createMemberImpersonationTicket,
    exportMemberList,
    logoutMember,
    queryMemberDetail,
    queryMemberGroups,
    queryMemberLineage,
    queryMemberList,
    saveMember,
    updateMemberStatus,
    updateMemberStatusBatch,
  } from '@/api/member';
  import { manualRechargeCredit, rechargeList } from '@/api/recharge';
  import type {
    MemberGroup,
    MemberLineNode,
    MemberLineage,
    MemberSavePayload,
    MemberUser,
  } from '@/api/member';
  import type { RechargeOrder } from '@/api/recharge';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'member-list',
  });

  const columns: TableColumnData[] = [
    { title: '用户ID', dataIndex: 'userId', align: 'center', width: 110 },
    { title: '账号', dataIndex: 'userName', align: 'center', width: 140 },
    { title: 'VIP等级', slotName: 'vipLevel', align: 'center', width: 100 },
    { title: '邀请码', dataIndex: 'inviteCode', align: 'center', width: 120 },
    { title: '分组', slotName: 'groupName', align: 'center', width: 120 },
    { title: '上级账号', dataIndex: 'parentUserName', align: 'center', width: 140 },
    { title: '可用余额', slotName: 'availableAmt', align: 'center', width: 120 },
    { title: '冻结金额', slotName: 'freezeAmt', align: 'center', width: 120 },
    { title: '状态', slotName: 'status', align: 'center', width: 90 },
    { title: '注册时间', slotName: 'registerTime', align: 'center', width: 170 },
    { title: '操作', slotName: 'operations', align: 'center', fixed: 'right', width: 500 },
  ];

  const lineageColumns: TableColumnData[] = [
    { title: '关系', slotName: 'relation', align: 'center', width: 110 },
    { title: 'UID', dataIndex: 'userId', align: 'center', width: 120 },
    { title: '账号', dataIndex: 'userName', align: 'center', width: 150 },
    { title: '昵称', dataIndex: 'nickname', align: 'center', width: 150 },
    { title: 'VIP', dataIndex: 'vipLevel', align: 'center', width: 80 },
    { title: '状态', slotName: 'status', align: 'center', width: 90 },
    { title: '上级账号', dataIndex: 'parentUserName', align: 'center', width: 150 },
  ];

  const tableData = ref<MemberUser[]>([]);
  const groupOptions = ref<MemberGroup[]>([]);
  const loading = ref(false);
  const statusUpdating = ref(false);
  const batchLoading = ref(false);
  const exporting = ref(false);
  const detailVisible = ref(false);
  const detailLoading = ref(false);
  const detailData = ref<MemberUser | null>(null);
  const memberModalVisible = ref(false);
  const memberSaving = ref(false);
  const groupAssignVisible = ref(false);
  const groupAssigning = ref(false);
  const lineageVisible = ref(false);
  const lineageLoading = ref(false);
  const lineageData = ref<MemberLineage | null>(null);
  const parentChangeVisible = ref(false);
  const parentChanging = ref(false);
  const manualRechargeVisible = ref(false);
  const manualRechargeSubmitting = ref(false);
  const logoutUserId = ref<number | null>(null);
  const downloadRechargeUserId = ref<number | null>(null);
  const impersonatingUserId = ref<number | null>(null);
  const activeVipTab = ref('all');
  const selectedRowKeys = ref<number[]>([]);
  const rowSelection = {
    type: 'checkbox' as const,
    showCheckedAll: true,
  };
  const queryForm = reactive({
    userId: '',
    userName: '',
    inviteCode: '',
    status: undefined as number | undefined,
    groupId: undefined as number | undefined,
    registerTimeRange: [] as Array<number | string>,
  });
  const memberForm = reactive<MemberSavePayload>({
    userName: '',
    password: '',
    nickname: '',
    email: '',
    vipLevel: 0,
    status: 1,
  });
  const groupAssignForm = reactive({
    groupId: undefined as number | undefined,
  });
  const parentChangeForm = reactive({
    userId: 0,
    parentUserId: undefined as number | undefined,
  });
  const manualRechargeForm = reactive({
    userId: 0,
    userName: '',
    amount: undefined as number | undefined,
    coin: 'USDT',
    referenceNo: '',
    remark: '',
  });
  const page = reactive(new PageData());
  const h5BaseUrl = (import.meta.env.VITE_H5_BASE_URL || 'http://127.0.0.1:5173').replace(/\/$/, '');
  const vipTabs = [
    { label: '全部', value: 'all' },
    { label: 'VIP0', value: '0' },
    { label: 'VIP1', value: '1' },
    { label: 'VIP2', value: '2' },
    { label: 'VIP3', value: '3' },
    { label: 'VIP4', value: '4' },
    { label: 'VIP5', value: '5' },
  ];
  const lineageRows = computed<MemberLineNode[]>(() => {
    if (!lineageData.value) {
      return [];
    }
    return [
      ...(lineageData.value.ancestors || []),
      ...(lineageData.value.current ? [lineageData.value.current] : []),
      ...(lineageData.value.children || []),
    ];
  });

  const fetchData = () => {
    loading.value = true;
    queryMemberList(buildQueryParams(true))
      .then((rep) => {
        if (rep.success) {
          tableData.value = rep.data.records || [];
          const visibleIds = new Set(tableData.value.map((item) => item.userId));
          selectedRowKeys.value = selectedRowKeys.value.filter((id) => visibleIds.has(id));
          page.updatePage(rep.data);
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  const loadGroups = () => {
    queryMemberGroups().then((rep) => {
      if (rep.success) {
        groupOptions.value = rep.data || [];
      }
    });
  };

  const handleVipTabChange = (key: string | number) => {
    activeVipTab.value = String(key);
    page.pageNumber = 1;
    fetchData();
  };

  const handleSearch = () => {
    page.pageNumber = 1;
    fetchData();
  };

  const resetSearch = () => {
    queryForm.userId = '';
    queryForm.userName = '';
    queryForm.inviteCode = '';
    queryForm.status = undefined;
    queryForm.groupId = undefined;
    queryForm.registerTimeRange = [];
    page.pageNumber = 1;
    fetchData();
  };

  const handleBatchStatus = (status: number) => {
    if (!selectedRowKeys.value.length || batchLoading.value) {
      return;
    }
    batchLoading.value = true;
    updateMemberStatusBatch({ userIds: selectedRowKeys.value, status })
      .then((rep) => {
        if (rep.success) {
          Message.success(status === 1 ? '批量解封成功' : '批量封禁成功');
          selectedRowKeys.value = [];
          fetchData();
        }
      })
      .finally(() => {
        batchLoading.value = false;
      });
  };

  const handleBatchLogout = () => {
    if (!selectedRowKeys.value.length || batchLoading.value) {
      return;
    }
    batchLoading.value = true;
    Promise.all(selectedRowKeys.value.map((id) => logoutMember({ id })))
      .then((reps) => {
        if (reps.every((rep) => rep.success)) {
          Message.success('批量下线成功');
          selectedRowKeys.value = [];
        }
      })
      .finally(() => {
        batchLoading.value = false;
      });
  };

  const openDetail = (record: MemberUser) => {
    detailVisible.value = true;
    detailData.value = record;
    detailLoading.value = true;
    queryMemberDetail(record.userId)
      .then((rep) => {
        if (rep.success) {
          detailData.value = rep.data || record;
        }
      })
      .finally(() => {
        detailLoading.value = false;
      });
  };

  const openCreateMember = () => {
    resetMemberForm();
    memberModalVisible.value = true;
  };

  const openEditMember = (record: MemberUser) => {
    resetMemberForm();
    memberForm.userId = record.userId;
    memberForm.userName = record.userName;
    memberForm.nickname = record.nickname || '';
    memberForm.email = record.email || '';
    memberForm.vipLevel = record.vipLevel ?? 0;
    memberForm.status = record.status ?? 1;
    memberForm.parentUserId = record.parentUserId;
    memberForm.groupId = record.groupId;
    memberModalVisible.value = true;
  };

  const submitMember = () => {
    const userName = memberForm.userName?.trim();
    if (!userName) {
      Message.warning('请输入会员账号');
      return;
    }
    if (!memberForm.userId && !memberForm.password?.trim()) {
      Message.warning('新增会员必须填写登录密码');
      return;
    }
    if (memberForm.userId && memberForm.parentUserId === memberForm.userId) {
      Message.warning('上级不能是会员自己');
      return;
    }
    memberSaving.value = true;
    saveMember({
      ...memberForm,
      userName,
      password: memberForm.password?.trim() || undefined,
      nickname: memberForm.nickname?.trim(),
      email: memberForm.email?.trim(),
    })
      .then((rep) => {
        if (rep.success) {
          Message.success(memberForm.userId ? '会员已更新' : '会员已添加');
          memberModalVisible.value = false;
          loadGroups();
          fetchData();
        }
      })
      .finally(() => {
        memberSaving.value = false;
      });
  };

  const openGroupAssign = () => {
    groupAssignForm.groupId = undefined;
    groupAssignVisible.value = true;
  };

  const submitGroupAssign = () => {
    if (!selectedRowKeys.value.length) {
      return;
    }
    groupAssigning.value = true;
    assignMemberGroup({ userIds: selectedRowKeys.value, groupId: groupAssignForm.groupId })
      .then((rep) => {
        if (rep.success) {
          Message.success('分组已更新');
          groupAssignVisible.value = false;
          selectedRowKeys.value = [];
          loadGroups();
          fetchData();
        }
      })
      .finally(() => {
        groupAssigning.value = false;
      });
  };

  const handleExport = () => {
    exporting.value = true;
    exportMemberList(buildQueryParams(false))
      .then((rep) => {
        if (rep.success) {
          downloadMembers(rep.data || []);
        }
      })
      .finally(() => {
        exporting.value = false;
      });
  };

  const openLineage = (record: MemberUser) => {
    lineageVisible.value = true;
    lineageData.value = null;
    lineageLoading.value = true;
    queryMemberLineage(record.userId)
      .then((rep) => {
        if (rep.success) {
          lineageData.value = rep.data;
        }
      })
      .finally(() => {
        lineageLoading.value = false;
      });
  };

  const openParentChange = (record: MemberUser) => {
    parentChangeForm.userId = record.userId;
    parentChangeForm.parentUserId = record.parentUserId;
    parentChangeVisible.value = true;
  };

  const submitParentChange = () => {
    if (!parentChangeForm.parentUserId) {
      Message.warning('请输入新上级 UID');
      return;
    }
    if (parentChangeForm.parentUserId === parentChangeForm.userId) {
      Message.warning('上级不能是会员自己');
      return;
    }
    parentChanging.value = true;
    changeMemberParent({
      userId: parentChangeForm.userId,
      parentUserId: parentChangeForm.parentUserId,
    })
      .then((rep) => {
        if (rep.success) {
          Message.success('会员链路已更新');
          parentChangeVisible.value = false;
          fetchData();
        }
      })
      .finally(() => {
        parentChanging.value = false;
      });
  };

  const openManualRecharge = (record: MemberUser) => {
    manualRechargeForm.userId = record.userId;
    manualRechargeForm.userName = record.userName;
    manualRechargeForm.amount = undefined;
    manualRechargeForm.coin = 'USDT';
    manualRechargeForm.referenceNo = '';
    manualRechargeForm.remark = '';
    manualRechargeVisible.value = true;
  };

  const submitManualRecharge = () => {
    const amount = Number(manualRechargeForm.amount);
    if (!Number.isFinite(amount) || amount <= 0) {
      Message.warning('请输入有效的补单金额');
      return;
    }
    manualRechargeSubmitting.value = true;
    manualRechargeCredit({
      userId: manualRechargeForm.userId,
      amount,
      coin: manualRechargeForm.coin,
      referenceNo: manualRechargeForm.referenceNo.trim() || undefined,
      remark: manualRechargeForm.remark.trim() || undefined,
    })
      .then((rep) => {
        if (rep.success) {
          Message.success(`补单成功，订单号：${rep.data.orderNo}`);
          manualRechargeVisible.value = false;
          fetchData();
        }
      })
      .finally(() => {
        manualRechargeSubmitting.value = false;
      });
  };

  const downloadRecharge = (record: MemberUser) => {
    if (downloadRechargeUserId.value) {
      return;
    }
    downloadRechargeUserId.value = record.userId;
    rechargeList(undefined, 1000, record.userId)
      .then((rep) => {
        if (rep.success) {
          downloadRechargeOrders(record, rep.data || []);
        }
      })
      .finally(() => {
        downloadRechargeUserId.value = null;
      });
  };

  const forceLogout = (record: MemberUser) => {
    if (logoutUserId.value) {
      return;
    }
    logoutUserId.value = record.userId;
    logoutMember({ id: record.userId })
      .then((rep) => {
        if (rep.success) {
          Message.success('已强制下线');
        }
      })
      .finally(() => {
        logoutUserId.value = null;
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

  loadGroups();
  fetchData();

  function buildQueryParams(includePage: boolean): Map<string, string> {
    const params = new Map<string, string>();
    if (includePage) {
      params.set('pageNumber', String(page.pageNumber));
      params.set('pageSize', String(page.pageSize));
    }
    if (queryForm.userId.trim()) {
      params.set('userId', queryForm.userId.trim());
    }
    if (queryForm.userName.trim()) {
      params.set('userName', queryForm.userName.trim());
    }
    if (queryForm.inviteCode.trim()) {
      params.set('inviteCode', queryForm.inviteCode.trim());
    }
    if (queryForm.status != null) {
      params.set('status', String(queryForm.status));
    }
    if (queryForm.groupId != null) {
      params.set('groupId', String(queryForm.groupId));
    }
    const [startTime, endTime] = queryForm.registerTimeRange || [];
    if (startTime) {
      params.set('registerStartTime', normalizeTimestamp(startTime));
    }
    if (endTime) {
      params.set('registerEndTime', normalizeTimestamp(endTime));
    }
    if (activeVipTab.value !== 'all') {
      params.set('vipLevel', activeVipTab.value);
    }
    return params;
  }

  function resetMemberForm() {
    memberForm.userId = undefined;
    memberForm.userName = '';
    memberForm.password = '';
    memberForm.nickname = '';
    memberForm.email = '';
    memberForm.vipLevel = 0;
    memberForm.status = 1;
    memberForm.parentUserId = undefined;
    memberForm.groupId = undefined;
  }

  function normalizeTimestamp(value: number | string): string {
    const timestamp = Number(value);
    if (Number.isFinite(timestamp)) {
      return String(timestamp);
    }
    const parsed = new Date(value).getTime();
    return Number.isFinite(parsed) ? String(parsed) : '';
  }

  function statusText(status: number): string {
    const map: Record<number, string> = {
      1: '正常',
      2: '冻结',
    };
    return map[status] || '未知';
  }

  function relationText(relation: string, depth: number): string {
    if (relation === 'SELF') {
      return '当前会员';
    }
    if (relation === 'PARENT') {
      return `${Math.abs(depth)}级上级`;
    }
    return '直属下级';
  }

  function moneyText(amount: number): string {
    return Number(amount || 0).toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 6,
    });
  }

  function downloadMembers(rows: MemberUser[]) {
    if (!rows.length) {
      Message.warning('当前筛选没有可导出的会员');
      return;
    }
    const header = ['UID', '账号', 'VIP', '邀请码', '分组', '上级账号', '状态', '可用余额', '冻结金额', '注册时间'];
    const body = rows.map((row) => [
      row.userId,
      row.userName,
      `VIP ${row.vipLevel ?? 0}`,
      row.inviteCode,
      row.groupName || '',
      row.parentUserName || '',
      statusText(row.status),
      moneyText(row.availableAmt),
      moneyText(row.freezeAmt),
      row.registerTime ? parseTime(row.registerTime) : '',
    ]);
    downloadCsv(`members-${Date.now()}.csv`, header, body);
    Message.success('会员导出已生成');
  }

  function downloadRechargeOrders(member: MemberUser, rows: RechargeOrder[]) {
    if (!rows.length) {
      Message.warning('该会员暂无充值记录');
      return;
    }
    const header = ['订单号', 'UID', '账号', '来源', '币种', '金额', '状态', '交易哈希/凭证', '确认数', '入账时间', '创建时间'];
    const body = rows.map((row) => [
      row.orderNo,
      row.userId,
      member.userName,
      row.chainCode,
      row.coin,
      moneyText(row.amount),
      row.status,
      row.txHash,
      row.confirmations ?? 0,
      row.creditedAt ? parseTime(row.creditedAt) : '',
      row.createTime ? parseTime(row.createTime) : '',
    ]);
    downloadCsv(`recharge-${member.userId}-${Date.now()}.csv`, header, body);
    Message.success('充值记录导出已生成');
  }

  function downloadCsv(filename: string, header: string[], rows: unknown[][]) {
    const csv = [header, ...rows]
      .map((line) => line.map(csvCell).join(','))
      .join('\n');
    const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
  }

  function csvCell(value: unknown): string {
    return `"${String(value ?? '').replace(/"/g, '""')}"`;
  }
</script>

<style scoped lang="less">
  .member-vip-tabs {
    margin-bottom: 12px;
  }

  .member-search-form {
    margin-bottom: 16px;
  }

  .member-detail-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px;
  }

  .member-detail-item {
    padding: 12px;
    border: 1px solid var(--color-border-2);
    border-radius: 6px;
    background: var(--color-fill-1);

    span {
      display: block;
      margin-bottom: 6px;
      color: var(--color-text-3);
      font-size: 12px;
    }

    strong {
      color: var(--color-text-1);
      font-weight: 500;
      word-break: break-all;
    }
  }

  .member-detail-item.wide {
    grid-column: span 2;
  }

  .modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 20px;
  }
</style>