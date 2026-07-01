<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.user-member', 'menu.user-member.vip-config']" />
    <a-card>
      <a-space :size="12" wrap style="margin-bottom: 16px">
        <a-select v-model="filterStatus" style="width: 140px" @change="loadData">
          <a-option :value="undefined">全部</a-option>
          <a-option :value="1">启用</a-option>
          <a-option :value="0">停用</a-option>
        </a-select>
        <a-button type="primary" @click="loadData">查询</a-button>
        <a-button @click="resetQuery">重置</a-button>
        <a-button type="primary" @click="openCreate">新增等级</a-button>
      </a-space>

      <a-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1200 }"
      >
        <template #level="{ record }">
          <a-tag color="arcoblue">VIP {{ record.level }}</a-tag>
        </template>
        <template #price="{ record }">
          {{ moneyText(record.price) }} USDT
        </template>
        <template #rebateRate="{ record }">
          {{ rateText(record.rebateRate) }}
        </template>
        <template #benefits="{ record }">
          <a-typography-paragraph
            :ellipsis="{ rows: 2, expandable: true }"
            class="benefits-text"
          >
            {{ record.benefits || '-' }}
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
            <a-popconfirm
              v-if="record.level > 0"
              content="确认删除该 VIP 等级配置？"
              @ok="doDelete(record.id)"
            >
              <a-button type="text" size="mini" status="danger">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="modalVisible"
      :title="form.id ? '编辑 VIP 等级' : '新增 VIP 等级'"
      :confirm-loading="saving"
      width="760px"
      @ok="doSave"
    >
      <a-form :model="form" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item field="level" label="VIP 等级" required>
              <a-input-number
                v-model="form.level"
                :min="0"
                :precision="0"
                :disabled="Boolean(form.id)"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="levelName" label="等级名称" required>
              <a-input v-model="form.levelName" allow-clear :max-length="50" />
            </a-form-item>
          </a-col>
          <a-col :span="5">
            <a-form-item field="sortOrder" label="排序">
              <a-input-number v-model="form.sortOrder" :min="0" :precision="0" />
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
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item field="price" label="升级价格 USDT" required>
              <a-input-number v-model="form.price" :min="0" :precision="6" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="rebateRate" label="返佣比例">
              <a-input-number v-model="form.rebateRate" :min="0" :precision="4" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="dailyTasks" label="每日任务数">
              <a-input-number v-model="form.dailyTasks" :min="0" :precision="0" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item field="benefits" label="权益说明">
          <a-textarea
            v-model="form.benefits"
            allow-clear
            :auto-size="{ minRows: 4, maxRows: 10 }"
            :max-length="1000"
            placeholder="每行一条权益，移动端会按换行/逗号拆分展示"
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
    VipLevelConfig,
    VipLevelConfigPayload,
    deleteVipConfig,
    saveVipConfig,
    vipConfigList,
  } from '@/api/vip';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'vip-config',
  });

  const columns: TableColumnData[] = [
    {
      title: '等级',
      slotName: 'level',
      align: 'center',
      width: 90,
    },
    {
      title: '名称',
      dataIndex: 'levelName',
      align: 'center',
      width: 140,
    },
    {
      title: '价格',
      slotName: 'price',
      align: 'center',
      width: 140,
    },
    {
      title: '返佣比例',
      slotName: 'rebateRate',
      align: 'center',
      width: 120,
    },
    {
      title: '每日任务数',
      dataIndex: 'dailyTasks',
      align: 'center',
      width: 120,
    },
    {
      title: '权益说明',
      slotName: 'benefits',
      align: 'center',
      width: 280,
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
      width: 180,
    },
  ];

  const emptyForm = (): VipLevelConfig => ({
    level: 0,
    levelName: '',
    price: 0,
    rebateRate: 0,
    dailyTasks: 0,
    benefits: '',
    sortOrder: 0,
    status: 1,
  });

  const tableData = ref<VipLevelConfig[]>([]);
  const loading = ref(false);
  const saving = ref(false);
  const modalVisible = ref(false);
  const filterStatus = ref<number | undefined>();
  const form = reactive<VipLevelConfig>(emptyForm());

  const assignForm = (item?: VipLevelConfig) => {
    Object.assign(form, emptyForm(), item || {});
  };

  const loadData = () => {
    loading.value = true;
    vipConfigList({ status: filterStatus.value })
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
    filterStatus.value = undefined;
    loadData();
  };

  const openCreate = () => {
    assignForm();
    modalVisible.value = true;
  };

  const openEdit = (item: VipLevelConfig) => {
    assignForm(item);
    modalVisible.value = true;
  };

  const doSave = () => {
    if (!form.levelName.trim()) {
      Message.warning('请填写等级名称');
      return false;
    }
    if (form.level > 0 && form.price <= 0) {
      Message.warning('付费 VIP 等级价格必须大于 0');
      return false;
    }
    saving.value = true;
    return saveVipConfig(payload(form))
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

  const toggleStatus = (item: VipLevelConfig) => {
    saveVipConfig(payload({ ...item, status: item.status === 1 ? 0 : 1 }))
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
    deleteVipConfig(id).then((rep) => {
      if (rep.success) {
        Message.success('删除成功');
        loadData();
      }
    });
  };

  const payload = (item: VipLevelConfig): VipLevelConfigPayload => ({
    id: item.id,
    level: item.level,
    levelName: item.levelName.trim(),
    price: Number(item.price || 0),
    rebateRate: Number(item.rebateRate || 0),
    dailyTasks: Number(item.dailyTasks || 0),
    benefits: item.benefits || '',
    sortOrder: Number(item.sortOrder || 0),
    status: item.status,
  });

  loadData();

  function moneyText(amount: number): string {
    return Number(amount || 0).toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 6,
    });
  }

  function rateText(rate: number): string {
    return `${parseFloat((Number(rate || 0) * 100).toFixed(4))}%`;
  }
</script>

<style scoped>
  .benefits-text {
    margin-bottom: 0;
    text-align: left;
    white-space: pre-line;
  }
</style>