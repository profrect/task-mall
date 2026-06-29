<template>
  <div class="container_all">
    <Breadcrumb :items="['menu.sys-permission', 'menu.role-control']" />
    <a-card>
      <a-row style="margin-top: 10px">
        <a-col flex="auto" class="search_input">
          <a-input
            v-model="formModel.roleCode"
            placeholder="请输入代码"
            allow-clear
          />
          <a-input
            v-model="formModel.roleName"
            placeholder="请输入名称"
            allow-clear
          />
        </a-col>
        <a-divider style="height: 35px" direction="vertical" />
        <a-col :flex="'50px'">
          <a-space :size="18">
            <a-button type="primary" @click="fetchData">
              <template #icon>
                <icon-search />
              </template>
              {{ t('roleControl.form.search') }}
            </a-button>
            <a-button @click="reset">
              <template #icon>
                <icon-refresh />
              </template>
              {{ t('roleControl.form.reset') }}
            </a-button>
            <a-button status="warning" @click="openAddPage">
              <template #icon>
                <icon-user-add />
              </template>
              {{ t('roleControl.form.add') }}
            </a-button>
          </a-space>
        </a-col>
      </a-row>
      <a-divider style="margin-top: 20px" />
      <a-table :columns="columns" :data="roleData" :loading="loading">
        <template #createTime="{ record }">
          {{ parseTime(record.createTime) }}
        </template>
        <template #updateTime="{ record }">
          {{ parseTime(record.updateTime) }}
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
          <a-button
            type="text"
            size="mini"
            status="normal"
            @click="openMenuPage(record)"
          >
            {{ '菜单授权' }}
          </a-button>
          <a-popconfirm
            content="确定删除该角色吗？"
            @ok="submitDelete(record.id)"
          >
            <a-button type="text" size="mini" status="danger">
              {{ '删除' }}
            </a-button>
          </a-popconfirm>
        </template>
      </a-table>
    </a-card>

    <a-drawer v-model:visible="visible" :width="600">
      <template #title>
        {{ drawerFlag.title }}
      </template>
      <!--新增/编辑角色-->
      <div v-if="drawerFlag.type < 3">
        <a-form
          ref="formRef"
          layout="vertical"
          :model="editData"
          :rules="rules"
        >
          <a-form-item field="roleCode" label="角色代码">
            <a-input v-model="editData.roleCode" placeholder="请输入角色代码" :disabled="drawerFlag.type === 2" class="disabled-style" />
          </a-form-item>
          <a-form-item field="roleName" label="角色名称">
            <a-input v-model="editData.roleName" placeholder="请输入角色名称" />
          </a-form-item>
          <a-form-item label="角色说明">
            <a-input
              v-model="editData.desc"
              placeholder="请输入角色说明"
            />
          </a-form-item>
          <a-form-item v-if="drawerFlag.type !== 1" label="添加时间">
            {{ parseTime(editData.createTime) }}
          </a-form-item>
          <a-form-item v-if="drawerFlag.type !== 1" label="修改时间">
            {{ parseTime(editData.updateTime) }}
          </a-form-item>
        </a-form>
      </div>

      <!--菜单授权-->
      <div v-if="drawerFlag.type === 3" style="display: flex">
        <div>
          <a-tree
            v-if="menuData.systemTree.length > 0"
            v-model:checked-keys="systemCheckedKeys"
            v-model:expanded-keys="menuData.systemExpandedKeys"
            :default-expand-checked="true"
            :checkable="true"
            :data="menuData.systemTree"
            @check="onSysCheck"
          />
          <div v-else>
            {{ '暂无数据，请至 系统管理 -> 菜单路由配置 添加' }}
          </div>
        </div>
      </div>
      <template #footer>
        <template v-if="drawerFlag.type !== 3">
          <a-button status="success" @click="submitEdit">
            {{ '确定' }}
          </a-button>
        </template>
        <template v-else>
          <a-button status="success" @click="submitAssignMenu">
            {{ '确定' }}
          </a-button>
        </template>
        <a-button @click="visible = false">{{ '关闭' }}</a-button>
      </template>
    </a-drawer>

    <a-modal v-model:visible="apiVisible" width="50%" title="接口授权">
      <div style="padding: 20px">
        <a-tabs
          v-model:active-key="tabKey"
          position="left"
          @tab-click="changeApis"
        >
          <a-tab-pane v-for="name in apiData.keys()" :key="name" :title="name">
            <a-table
              v-model:selected-keys="apiSelectKeys"
              :data="apiTableList"
              row-key="id"
              :row-selection="rowSelection"
              style="height: 450px"
            >
              <template #columns>
                <a-table-column
                  align="center"
                  data-index="apiName"
                  title="接口名称"
                />
                <a-table-column
                  align="center"
                  data-index="apiUrl"
                  title="接口地址"
                />
              </template>
            </a-table>
          </a-tab-pane>
        </a-tabs>
      </div>
      <template #footer>
        <a-button status="success" @click="submitAssignApi">确定</a-button>
        <a-button @click="apiVisible = false">关闭</a-button>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import useLoading from '@/hooks/loading';
  import { computed, onMounted, reactive, ref } from 'vue';
  import { parseTime } from '@/utils/dateUtils';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import {
    Role,
    selectRole,
    RoleSearch,
    addUserRole,
    updateUserRole,
    deleteUserRole,
    RoleAssignIds,
    assignRoleMenu,
    assignRoleApi,
  } from '@/api/role';
  import { Message } from '@arco-design/web-vue';
  import { getRoleMenuIdList, getTwoMenuTreeList, MenuInfo } from '@/api/menu';
  import { OptionTree } from '@/model/common';
  import { allApisByType, ServerApi } from '@/api/api';

  defineOptions({
    // eslint-disable-next-line vue/component-definition-name-casing
    name: 'role-control',
  });

  const { t } = useI18n();
  const { loading, setLoading } = useLoading(true);
  const visible = ref(false);
  const apiVisible = ref(false);
  const drawerFlag = ref({
    title: '',
    type: 1,
  });
  const current = ref(1);
  const formRef = ref();
  const rules = {
    roleCode: [{ required: true, message: '请填写角色代码' }],
    roleName: [{ required: true, message: '请填写角色名称' }],
  };

  const systemCheckedKeys = ref([]);
  const systemSelectKeys = ref(Array<number>());

  const tabKey = ref('');
  const apiSelectKeys = ref(Array<number>());
  const rowSelection = ref({
    type: 'checkbox',
    showCheckedAll: true,
    onlyCurrent: true,
    selectable: (record: any) => record.id !== null,
  });

  const formModel = reactive(new RoleSearch({}));

  const columns = computed<TableColumnData[]>(() => [
    {
      title: t('roleControl.columns.code'),
      dataIndex: 'roleCode',
      slotName: 'roleCode',
      align: 'center',
    },
    {
      title: t('roleControl.columns.name'),
      dataIndex: 'roleName',
      slotName: 'roleName',
      align: 'center',
    },
    {
      title: t('roleControl.columns.level'),
      dataIndex: 'level',
      slotName: 'level',
      align: 'center',
    },
    {
      title: t('roleControl.columns.description'),
      dataIndex: 'desc',
      slotName: 'desc',
      align: 'center',
    },
    {
      title: t('roleControl.columns.createTime'),
      dataIndex: 'createTime',
      slotName: 'createTime',
      align: 'center',
    },
    {
      title: t('roleControl.columns.updateTime'),
      dataIndex: 'updateTime',
      slotName: 'updateTime',
      align: 'center',
    },
    {
      title: t('roleControl.columns.operations'),
      dataIndex: 'operations',
      slotName: 'operations',
      align: 'center',
      width: 280,
    },
  ]);

  const roleData = reactive(new Array<Role>());

  const editData = reactive(new Role({}));

  const menuData = reactive({
    systemTree: Array<OptionTree>(),
    systemExpandedKeys: [],
  });

  const apiData = reactive(new Map<string, Array<ServerApi>>());
  const apiTableList = reactive(new Array<ServerApi>());

  const assignData = reactive(new RoleAssignIds({}));

  const fetchData = async () => {
    setLoading(true);
    roleData.splice(0);
    formModel.pageNumber = 1;
    formModel.pageSize = 10;
    await selectRole(formModel).then((rep) => {
      if (rep.success) {
        roleData.push(...(rep.data.records || []));
      }
      setLoading(false);
    });
  };

  fetchData();

  const reset = () => {
    formModel.update(new RoleSearch({}));
    fetchData();
  };

  function openAddPage() {
    drawerFlag.value.type = 1;
    drawerFlag.value.title = '添加角色';
    editData.update(new Role({}));
    visible.value = true;
  }

  function openEditPage(row: Role) {
    drawerFlag.value.type = 2;
    drawerFlag.value.title = '编辑角色';
    editData.update(row);
    visible.value = true;
  }

  function openMenuPage(row: Role) {
    drawerFlag.value.type = 3;
    drawerFlag.value.title = '菜单授权';
    infoAndEditCommon(row);
  }

  async function infoAndEditCommon(row: Role) {
    const sId = menuData.systemTree.map((item) => item.key);
    current.value = 1;
    editData.update(row);
    menuData.systemExpandedKeys.splice(0);
    systemCheckedKeys.value.splice(0);
    systemSelectKeys.value.splice(0);
    if (row.menuIds) {
      await getRoleMenuIdList(row.roleCode).then((rep) => {
        if (rep.success) {
          const sData = rep.data.systemMenuIds;
          systemCheckedKeys.value.push(
            ...sData.filter((item) => !sId.includes(item))
          );
          const firstId = sData.filter((item) => item === 1);
          if (firstId.length > 0) {
            systemCheckedKeys.value.push(firstId[0]);
          }
          menuData.systemExpandedKeys.push(...sData);
          systemSelectKeys.value.push(...sData);
        }
      });
    }
    visible.value = true;
  }

  function submitEdit() {
    formRef.value.validate((valid: any) => {
      if (valid !== undefined) {
        Message.warning('请按要求填写');
        return;
      }

      if (drawerFlag.value.type === 1) {
        addUserRole(editData).then(async (rep: any) => {
          if (rep.success) {
            Message.success('新增角色成功');
            await fetchData();
            visible.value = false;
          }
        });
      } else {
        updateUserRole(editData).then(async (rep: any) => {
          if (rep.success) {
            Message.success('修改角色成功');
            await fetchData();
            visible.value = false;
          }
        });
      }
    });
  }

  function submitAssignMenu() {
    const idList = new Array<number>();
    idList.push(...systemSelectKeys.value);
    assignData.idList = idList;
    assignData.roleCode = editData.roleCode;
    assignRoleMenu(assignData).then(async (rep) => {
      if (rep.success) {
        Message.success('授权角色菜单成功');
        await fetchData();
        visible.value = false;
      }
    });
  }

  function submitDelete(id: number) {
    deleteUserRole(id).then(async (rep) => {
      if (rep.success) {
        Message.success('删除角色成功');
        await fetchData();
        visible.value = false;
      }
    });
  }

  function getTwoList() {
    getTwoMenuTreeList().then((rep) => {
      menuData.systemTree.push(
        ...getPnoOptionTree(0, rep.data.systemMenuTreeList)
      );
    });
  }

  function getPnoOptionTree(pno: number, list: MenuInfo[]): OptionTree[] {
    if (list.length < 1) {
      return [];
    }
    const filterList = list.filter((item) => item.pno === pno);
    const resultList: OptionTree[] = [];
    filterList.forEach((item) => {
      const data: OptionTree = {
        key: 0,
        title: '顶级菜单',
      };
      if (item.key) {
        data.key = item.key;
        if (item.menuKey) {
          data.title = t(item.menuKey);
        }
        data.children = getPnoOptionTree(item.key, item.children || []);
        resultList.push(data);
      }
    });
    return resultList;
  }

  // 接口资源相关
  function getApiTypeList() {
    apiData.clear();
    allApisByType().then((rep) => {
      if (rep.success) {
        rep.data.forEach((item) => {
          apiData.set(item.typeName, item.apis);
        });
      }
    });
  }

  function openApiPage(row: Role) {
    tabKey.value = apiData.entries().next().value?.[0];
    apiTableList.splice(0);
    apiTableList.push(...(apiData.entries().next().value?.[1] || []));
    apiSelectKeys.value.splice(0);
    apiSelectKeys.value.push(...row.apiIds);
    editData.roleCode = row.roleCode;
    apiVisible.value = true;
  }

  function changeApis(key: string) {
    const list = apiData.get(key);
    apiTableList.splice(0);
    apiTableList.push(...(list || []));
  }

  function submitAssignApi() {
    assignData.roleCode = editData.roleCode;
    assignData.idList = apiSelectKeys.value;
    assignRoleApi(assignData).then(async (rep) => {
      if (rep.success) {
        Message.success('授权角色接口成功');
        await fetchData();
        apiVisible.value = false;
      }
    });
  }

  onMounted(() => {
    getTwoList();
    // getApiTypeList();
  });

  const onSysCheck = (keys: number[], info: any) => {
    systemSelectKeys.value.splice(0);
    systemSelectKeys.value.push(...keys);
    if (info.halfCheckedKeys.length > 0) {
      systemSelectKeys.value.push(...(info.halfCheckedKeys as number[]));
    }
  };
</script>
