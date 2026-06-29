<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.sys-permission', 'menu.user-control']" />
    <a-card>
      <a-row style="margin-top: 10px">
        <a-col flex="auto" class="search_input">
          <a-input
            v-model="formModel.username"
            placeholder="请输入名称"
            allow-clear
          />
          <a-select
            v-model="formModel.roleCode"
            placeholder="请选择角色"
            style="width: 200px"
            allow-clear
          >
            <a-option
              v-for="id in userRoleMap.keys()"
              :key="id"
              :value="userRoleMap.get(id)"
              :label="id"
            />
          </a-select>
        </a-col>
        <a-divider style="height: 35px" direction="vertical" />
        <a-col :flex="'50px'">
          <a-space :size="18">
            <a-button type="primary" @click="fetchData">
              <template #icon>
                <icon-search />
              </template>
              {{ t('userControl.form.search') }}
            </a-button>
            <a-button @click="reset">
              <template #icon>
                <icon-refresh />
              </template>
              {{ t('userControl.form.reset') }}
            </a-button>
            <a-button status="warning" @click="openAddPage">
              <template #icon>
                <icon-user-add />
              </template>
              {{ t('userControl.form.add') }}
            </a-button>
          </a-space>
        </a-col>
      </a-row>
      <a-divider style="margin-top: 20px" />
      <a-table :columns="columns" :data="userData" :loading="loading">
        <template #roles="{ record }">
          {{ record.roles }}
        </template>
        <template #updateTime="{ record }">
          {{ parseTime(record.updateTime) }}
        </template>
        <template #operations="{ record }">
          <a-button
            type="text"
            size="mini"
            status="success"
            @click="openUserInfo(record)"
          >
            {{ '详情' }}
          </a-button>
          <a-button
            type="text"
            size="mini"
            status="normal"
            @click="openEditPage(record)"
          >
            {{ '编辑' }}
          </a-button>
          <a-popconfirm
            content="确定删除该用户吗？"
            @ok="submitDelete(record.id)"
          >
            <a-button type="text" size="mini" status="danger">
              {{ '删除' }}
            </a-button>
          </a-popconfirm>
          <a-popconfirm
            content="确定要重置该用户的密码？"
            @ok="resetPasswd(record.username)"
          >
            <a-button type="text" size="mini" status="warning">
              {{ '重置密码' }}
            </a-button>
          </a-popconfirm>
        </template>
      </a-table>
    </a-card>

    <a-drawer v-model:visible="visible" :width="500">
      <template #title>
        {{ drawerFlag.title }}
      </template>
      <a-form ref="formRef" layout="vertical" :model="editData" :rules="rules">
        <a-form-item :label="t('userControl.drawer.name')" field="username">
          <a-input
            v-model="editData.username"
            placeholder="请输入用户名"
            :disabled="drawerFlag.type != 1"
            class="disabled-style"
          />
        </a-form-item>
        <a-form-item :label="t('userControl.drawer.realName')">
          <a-input
            v-model="editData.realName"
            placeholder="请输入真实姓名"
            :disabled="drawerFlag.show"
            class="disabled-style"
          />
        </a-form-item>
        <a-form-item
          v-show="drawerFlag.type === 1"
          :label="t('userControl.drawer.password')"
        >
          <a-input
            v-model="editData.password"
            placeholder="如果不设置密码将自动生成"
          />
        </a-form-item>
        <a-form-item :label="t('userControl.drawer.role')">
          <a-checkbox-group
            v-model="editData.roleCodeList"
            direction="horizontal"
            :disabled="drawerFlag.show"
          >
            <div v-for="id in userRoleMap.keys()" :key="id">
              <a-checkbox :value="userRoleMap.get(id)" class="disabled-style">
                {{ id }}
              </a-checkbox>
            </div>
          </a-checkbox-group>
        </a-form-item>
        <a-form-item
          v-if="drawerFlag.type === 0"
          :label="t('userControl.drawer.id')"
        >
          <a-input-number
            v-model="editData.id"
            disabled
            class="disabled-style"
          />
        </a-form-item>
        <a-form-item
          v-if="drawerFlag.type === 0"
          :label="t('userControl.drawer.addUser')"
        >
          <a-input v-model="editData.creator" disabled class="disabled-style" />
        </a-form-item>
        <a-form-item
          v-if="drawerFlag.type === 0"
          :label="t('userControl.drawer.createTimeLong')"
        >
          <a-input
            :default-value="parseTime(editData.createTime)"
            disabled
            class="disabled-style"
          />
        </a-form-item>
        <a-form-item
          v-if="drawerFlag.type === 0"
          :label="t('userControl.drawer.lastUpdateUser')"
        >
          <a-input v-model="editData.updater" disabled class="disabled-style" />
        </a-form-item>
        <a-form-item
          v-if="drawerFlag.type === 0"
          :label="t('userControl.drawer.updateTimeLong')"
        >
          <a-input
            :default-value="parseTime(editData.updateTime)"
            disabled
            class="disabled-style"
          />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-button
          v-if="drawerFlag.type > 0"
          status="success"
          @click="drawerSubmit"
        >
          {{ '确定' }}
        </a-button>
        <a-button @click="visible = false">{{ '关闭' }}</a-button>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
  import { computed, reactive, ref } from 'vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import { useI18n } from 'vue-i18n';
  import useLoading from '@/hooks/loading';
  import { parseTime } from '@/utils/dateUtils';
  import {
    addNewUser,
    deleteUserById,
    queryUserInfoList,
    resetUserPasswd,
    updateUser,
    UserFull,
  } from '@/api/user';
  import { Message } from '@arco-design/web-vue';
  import { userRoleOptions } from '@/api/role';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'user-control',
  });

  const { t } = useI18n();
  const { loading, setLoading } = useLoading(true);
  const visible = ref(false);
  const drawerFlag = ref({
    title: '',
    type: 1,
    show: false,
  });
  const formRef = ref();
  const rules = {
    username: [{ required: true, message: '请填写名称' }],
  };

  const searchFormData = () => {
    return {
      username: '',
      roleCode: '',
    };
  };
  const formModel = ref(searchFormData());

  const userRoleMap = reactive(new Map<string, string>());

  const columns = computed<TableColumnData[]>(() => [
    {
      title: t('userControl.columns.name'),
      dataIndex: 'username',
      slotName: 'username',
      align: 'center',
    },
    {
      title: t('userControl.columns.realName'),
      dataIndex: 'realName',
      slotName: 'realName',
      align: 'center',
    },
    {
      title: t('userControl.columns.role'),
      dataIndex: 'roles',
      slotName: 'roles',
      align: 'center',
    },
    {
      title: t('userControl.columns.creator'),
      dataIndex: 'creator',
      slotName: 'creator',
      align: 'center',
    },
    {
      title: t('userControl.columns.updateTime'),
      dataIndex: 'updateTime',
      slotName: 'updateTime',
      align: 'center',
    },
    {
      title: t('userControl.columns.operations'),
      dataIndex: 'operations',
      slotName: 'operations',
      align: 'center',
      width: 250,
    },
  ]);

  const userData = ref<UserFull[]>();

  const editData = reactive(new UserFull({}));

  function getUserGroup() {
    userRoleMap.clear();
    userRoleOptions().then((rep) => {
      if (rep.success) {
        rep.data.forEach((d) => {
          userRoleMap.set(d.label, d.value);
        });
      }
    });
  }

  getUserGroup();

  const fetchData = async () => {
    setLoading(true);
    try {
      const searchFormDataMap = new Map<string, any>([
        ['username', formModel.value.username],
        ['roleCode', formModel.value.roleCode],
      ]);
      const { data } = await queryUserInfoList(searchFormDataMap);
      userData.value = data.records;
    } catch (err) {
      // you can report use errorHandler or other
    } finally {
      setLoading(false);
    }
  };

  fetchData();

  const reset = () => {
    formModel.value = searchFormData();
    fetchData();
  };

  function openUserInfo(row: UserFull) {
    drawerFlag.value.type = 0;
    drawerFlag.value.title = '用户详情';
    drawerFlag.value.show = true;
    editData.updateUser(row);
    editData.roleCodeList = getUserRoleList(row.roles);
    visible.value = true;
  }

  function openAddPage() {
    drawerFlag.value.type = 1;
    drawerFlag.value.title = '新增用户';
    drawerFlag.value.show = false;
    editData.updateUser(new UserFull({}));
    visible.value = true;
  }

  function openEditPage(row: UserFull) {
    drawerFlag.value.type = 2;
    drawerFlag.value.title = '编辑用户';
    drawerFlag.value.show = false;
    editData.updateUser(row);
    editData.roleCodeList = getUserRoleList(row.roles);
    visible.value = true;
  }

  function getUserRoleList(roles: string): Array<string> {
    const arr = roles ? roles.split(',').map((item) => item.trim()) : [];
    if (arr.length > 0) {
      const list = new Array<string>();
      arr.forEach((key) => {
        if (userRoleMap.has(key) && userRoleMap.get(key)) {
          list.push(userRoleMap.get(key) as string);
        }
      });
      return list;
    }
    return arr;
  }

  function drawerSubmit() {
    editData.roles = JSON.stringify(editData.roleCodeList);

    formRef.value.validate((valid: any) => {
      if (valid !== undefined) {
        Message.warning('请按要求填写');
        return;
      }

      if (drawerFlag.value.type === 1) {
        addNewUser(editData).then(async (rep) => {
          if (rep.success) {
            Message.success('新增用户成功');
            await fetchData();
            visible.value = false;
          }
        });
      } else {
        updateUser(editData).then(async (rep) => {
          if (rep.success) {
            Message.success('修改用户信息成功');
            await fetchData();
            visible.value = false;
          }
        });
      }
    });
  }

  function submitDelete(id: number) {
    deleteUserById(id).then(async (rep) => {
      if (rep.success) {
        Message.success('删除用户成功');
        await fetchData();
        visible.value = false;
      }
    });
  }

  function resetPasswd(name: string) {
    resetUserPasswd(name).then(async (rep) => {
      if (rep.success) {
        Message.success('密码重置成功');
        visible.value = false;
      }
    });
  }
</script>
