<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.promotion', 'menu.promotion.prize-config']" />
    <a-card>
      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-input v-model="query.keyword" allow-clear placeholder="奖品名称/编码" style="width: 220px" />
        <a-select v-model="query.prizeType" allow-clear placeholder="类型" style="width: 140px">
          <a-option value="CASH">现金奖</a-option>
          <a-option value="POINT">非现金奖</a-option>
          <a-option value="COUPON">优惠券</a-option>
          <a-option value="PHYSICAL">实物</a-option>
        </a-select>
        <a-select v-model="query.status" allow-clear placeholder="状态" style="width: 120px">
          <a-option :value="1">启用</a-option>
          <a-option :value="0">停用</a-option>
        </a-select>
        <a-button type="primary" @click="loadData">查询</a-button>
        <a-button @click="resetQuery">重置</a-button>
        <a-button type="primary" @click="openCreate">新增奖品</a-button>
      </a-space>

      <a-alert style="margin-bottom: 12px" type="info">
        现金奖只定义金额口径，用户中奖后由 mall-promotion 调用钱包结算 Provider 入账；本页不直接改钱包余额。
      </a-alert>

      <a-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1300 }"
        @page-change="onPageChange"
      >
        <template #prizeType="{ record }">
          <a-tag :color="record.prizeType === 'CASH' ? 'green' : 'arcoblue'">
            {{ prizeTypeText(record.prizeType) }}
          </a-tag>
        </template>
        <template #amount="{ record }">
          {{ moneyText(record.amount) }} {{ record.currency }}
        </template>
        <template #stock="{ record }">
          {{ record.stockTotal === 0 ? '不限量' : `${record.stockUsed}/${record.stockTotal}` }}
        </template>
        <template #status="{ record }">
          <a-tag :color="record.status === 1 ? 'green' : 'gray'">
            {{ record.status === 1 ? '启用' : '停用' }}
          </a-tag>
        </template>
        <template #operations="{ record }">
          <a-space :size="4">
            <a-button type="text" size="mini" @click="openEdit(record)">编辑</a-button>
            <a-button
              type="text"
              size="mini"
              :status="record.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(record)"
            >
              {{ record.status === 1 ? '停用' : '启用' }}
            </a-button>
            <a-popconfirm content="确认删除该奖品？" @ok="doDelete(record.id)">
              <a-button type="text" size="mini" status="danger">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="modalVisible"
      :title="form.id ? '编辑奖品' : '新增奖品'"
      :confirm-loading="saving"
      width="720px"
      @ok="doSave"
    >
      <a-form :model="form" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item field="prizeCode" label="奖品编码">
              <a-input v-model="form.prizeCode" allow-clear placeholder="留空自动生成" />
            </a-form-item>
          </a-col>
          <a-col :span="10">
            <a-form-item field="prizeName" label="奖品名称" required>
              <a-input v-model="form.prizeName" allow-clear :max-length="100" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="prizeType" label="类型">
              <a-select v-model="form.prizeType">
                <a-option value="CASH">现金奖</a-option>
                <a-option value="POINT">非现金奖</a-option>
                <a-option value="COUPON">优惠券</a-option>
                <a-option value="PHYSICAL">实物</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item field="amount" label="金额">
              <a-input-number v-model="form.amount" :min="0" :precision="6" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="currency" label="币种">
              <a-input v-model="form.currency" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="stockTotal" label="总库存">
              <a-input-number v-model="form.stockTotal" :min="0" :precision="0" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item field="stockUsed" label="已用库存">
              <a-input-number v-model="form.stockUsed" :min="0" :precision="0" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item field="sortOrder" label="排序">
              <a-input-number v-model="form.sortOrder" :min="0" :precision="0" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="status" label="状态">
              <a-select v-model="form.status">
                <a-option :value="1">启用</a-option>
                <a-option :value="0">停用</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item field="remark" label="备注">
          <a-textarea v-model="form.remark" allow-clear :auto-size="{ minRows: 3, maxRows: 6 }" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import {
    PromotionPrize,
    deletePromotionPrize,
    promotionPrizePage,
    savePromotionPrize,
  } from '@/api/promotion';

  defineOptions({
    name: 'promotion-prize-config',
  });

  const query = reactive({
    keyword: '',
    prizeType: undefined as string | undefined,
    status: undefined as number | undefined,
    pageNumber: 1,
    pageSize: 10,
  });

  const columns: TableColumnData[] = [
    { title: 'ID', dataIndex: 'id', align: 'center', width: 80 },
    { title: '奖品编码', dataIndex: 'prizeCode', align: 'center', width: 170 },
    { title: '奖品名称', dataIndex: 'prizeName', align: 'center', width: 180 },
    { title: '类型', slotName: 'prizeType', align: 'center', width: 120 },
    { title: '金额', slotName: 'amount', align: 'center', width: 150 },
    { title: '库存', slotName: 'stock', align: 'center', width: 120 },
    { title: '排序', dataIndex: 'sortOrder', align: 'center', width: 90 },
    { title: '状态', slotName: 'status', align: 'center', width: 100 },
    { title: '备注', dataIndex: 'remark', align: 'center', width: 220 },
    { title: '操作', slotName: 'operations', align: 'center', fixed: 'right', width: 180 },
  ];

  const tableData = ref<PromotionPrize[]>([]);
  const loading = ref(false);
  const saving = ref(false);
  const total = ref(0);
  const modalVisible = ref(false);

  const defaultForm = (): PromotionPrize => ({
    prizeName: '',
    prizeType: 'CASH',
    currency: 'USDT',
    amount: 0,
    stockTotal: 0,
    stockUsed: 0,
    sortOrder: 0,
    status: 1,
    remark: '',
  });

  const form = reactive<PromotionPrize>(defaultForm());

  const pagination = computed(() => ({
    current: query.pageNumber,
    pageSize: query.pageSize,
    total: total.value,
    showTotal: true,
  }));

  const loadData = () => {
    loading.value = true;
    promotionPrizePage(query)
      .then((rep) => {
        if (rep.success) {
          tableData.value = rep.data?.records || [];
          total.value = rep.data?.totalRow || 0;
        }
      })
      .finally(() => {
        loading.value = false;
      });
  };

  const resetQuery = () => {
    Object.assign(query, {
      keyword: '',
      prizeType: undefined,
      status: undefined,
      pageNumber: 1,
    });
    loadData();
  };

  const onPageChange = (page: number) => {
    query.pageNumber = page;
    loadData();
  };

  const openCreate = () => {
    Object.assign(form, defaultForm());
    modalVisible.value = true;
  };

  const openEdit = (record: PromotionPrize) => {
    Object.assign(form, { ...record });
    modalVisible.value = true;
  };

  const doSave = () => {
    if (!form.prizeName) {
      Message.warning('请填写奖品名称');
      return;
    }
    saving.value = true;
    savePromotionPrize(form)
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

  const doDelete = (id?: number) => {
    if (!id) return;
    deletePromotionPrize(id).then((rep) => {
      if (rep.success) {
        Message.success('删除成功');
        loadData();
      }
    });
  };

  const toggleStatus = (record: PromotionPrize) => {
    savePromotionPrize({ ...record, status: record.status === 1 ? 0 : 1 }).then((rep) => {
      if (rep.success) {
        Message.success('状态已更新');
        loadData();
      }
    });
  };

  const prizeTypeText = (type: string) => {
    const map: Record<string, string> = {
      CASH: '现金奖',
      POINT: '非现金奖',
      COUPON: '优惠券',
      PHYSICAL: '实物',
    };
    return map[type] || type;
  };

  const moneyText = (value?: number) => Number(value || 0).toFixed(6);

  loadData();
</script>