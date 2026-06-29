<template>
  <a-card>
    <a-row style="margin-top: 10px">
      <a-col flex="auto" class="search_input">
        <a-form
          :label-col-props="{ span: 0 }"
          :wrapper-col-props="{ span: 5 }"
          label-align="left"
          :model="formModel"
        >
          <a-form-item field="name">
            <a-input
              v-model="formModel.name"
              placeholder="请输入路由名称"
              allow-clear
            />
          </a-form-item>
        </a-form>
      </a-col>
      <a-divider style="height: 35px" direction="vertical" />
      <a-col :flex="'50px'">
        <a-space :size="18">
          <a-button type="primary" @click="searchMenuTree">
            <template #icon>
              <icon-search />
            </template>
            {{ t(props.title + '.form.search') }}
          </a-button>
          <a-button @click="reset">
            <template #icon>
              <icon-refresh />
            </template>
            {{ t(props.title + '.form.reset') }}
          </a-button>
          <a-button status="warning" @click="openAddPage(0)">
            <template #icon>
              <icon-user-add />
            </template>
            {{ t(props.title + '.form.add') }}
          </a-button>
        </a-space>
      </a-col>
    </a-row>

    <a-divider style="margin-top: 3px" />

    <a-table
      v-model:expanded-keys="expandedKeys"
      :columns="columns"
      :data="menuData"
      :loading="loading"
      :scroll="scrollPercent"
      :scrollbar="scrollbar"
    >
      <template #menuKey="{ record }">
        <dynamic-icon v-if="record.icon" :icon="record.icon" :size="18" />
        {{ t(record.menuKey) }}
      </template>
      <template #type="{ record }">
        <a-tag :color="record.type === 1 ? 'orange' : 'green'">
          {{ record.type === 1 ? '目录' : '菜单' }}
        </a-tag>
      </template>
      <template #hideInMenu="{ record }">
        <a-tag :color="record.hideInMenu === 1 ? 'green' : 'gray'">
          {{ record.hideInMenu === 1 ? '显示' : '隐藏' }}
        </a-tag>
      </template>
      <template #operations="{ record }">
        <a-button
          type="text"
          size="mini"
          status="normal"
          @click="openAddPage(record.key)"
        >
          {{ '新增' }}
        </a-button>
        <a-button
          type="text"
          size="mini"
          status="warning"
          @click="openEditPage(record)"
        >
          {{ '编辑' }}
        </a-button>
      </template>
    </a-table>
  </a-card>

  <a-drawer v-model:visible="visible" :width="500">
    <template #title>
      {{ drawerFlag.title }}
    </template>
    <a-form ref="formRef" layout="vertical" :model="editData" :rules="rules">
      <a-form-item field="pno" label="父级菜单">
        <a-tree-select
          v-model="editData.pno"
          :data="pnoOptions"
          placeholder="选择父级菜单"
          :render-after-expand="false"
        />
      </a-form-item>
      <a-form-item field="menuKey" label="菜单名称key">
        <a-input v-model="editData.menuKey" placeholder="菜单名称key" />
      </a-form-item>
      <a-form-item field="type" label="菜单类型">
        <a-radio-group v-model="editData.type">
          <a-radio :value="1">目录</a-radio>
          <a-radio :value="0">菜单</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item field="name" label="路由名称">
        <a-input v-model="editData.name" placeholder="路由名称" />
      </a-form-item>
      <a-form-item field="path" label="路由路径">
        <a-input v-model="editData.path" placeholder="路由路径" />
      </a-form-item>
      <a-form-item field="component" label="组件路径">
        <a-input v-model="editData.component" placeholder="组件路径" />
      </a-form-item>
      <a-form-item field="hideInMenu" label="显示状态">
        <a-radio-group v-model="editData.hideInMenu">
          <a-radio :value="1">显示</a-radio>
          <a-radio :value="0">隐藏</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="图标">
        <a-input v-model="editData.icon" placeholder="请以'icon-'或'el-'开头" />
      </a-form-item>
    </a-form>
    <template #footer>
      <a-button status="success" @click="drawerSubmit">
        {{ '确定' }}
      </a-button>
      <a-button @click="visible = false">{{ '关闭' }}</a-button>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';
  import { computed, reactive, ref } from 'vue';
  import useLoading from '@/hooks/loading';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import {
    addMenu,
    deleteMenu,
    downloadSysMenu,
    getMenuTreeList,
    MenuInfo,
    MenuPo,
    updateMenu,
    uploadSysMenu,
  } from '@/api/menu';
  import DynamicIcon from '@/components/icon/dynamicIcon.vue';
  import { OptionTree } from '@/model/common';
  import { Message } from '@arco-design/web-vue';
  import userRoutesStore from '@/router/app-menus/user-routes';
  import ExcelTransfer from '@/components/excel-transfer/excelTransfer.vue';

  const { t } = useI18n();
  const { loading, setLoading } = useLoading(true);
  const permissionStore = userRoutesStore();
  const scrollPercent = {
    x: 1500,
  };
  const scrollbar = ref(true);

  const props = defineProps<{
    title: string;
    menuTag: number;
  }>();

  const columns = computed<TableColumnData[]>(() => [
    {
      title: t(`${props.title}.columns.menuKey`),
      dataIndex: 'menuKey',
      slotName: 'menuKey',
      align: 'center',
      width: 290,
    },
    {
      title: t(`${props.title}.columns.type`),
      dataIndex: 'type',
      slotName: 'type',
      align: 'center',
      width: 100,
    },
    {
      title: t(`${props.title}.columns.name`),
      dataIndex: 'name',
      slotName: 'name',
      align: 'center',
      width: 290,
    },
    {
      title: t(`${props.title}.columns.path`),
      dataIndex: 'path',
      slotName: 'path',
      align: 'center',
      width: 290,
    },
    {
      title: t(`${props.title}.columns.component`),
      dataIndex: 'component',
      slotName: 'component',
      align: 'center',
      ellipsis: true,
      width: 300,
    },
    {
      title: t(`${props.title}.columns.order`),
      dataIndex: 'order',
      slotName: 'order',
      align: 'center',
      width: 80,
    },
    {
      title: t(`${props.title}.columns.hideInMenu`),
      dataIndex: 'hideInMenu',
      slotName: 'hideInMenu',
      align: 'center',
      width: 80,
    },
    {
      title: t(`${props.title}.columns.operations`),
      dataIndex: 'operations',
      slotName: 'operations',
      align: 'center',
      fixed: 'right',
      width: 220,
    },
  ]);

  const expandedKeys = ref([]);
  const visible = ref(false);
  const drawerFlag = ref({
    title: '',
    type: 1,
  });

  const formRef = ref();
  const rules = {
    pno: [{ required: true, message: '请选择父级菜单' }],
    menuKey: [{ required: true, message: '请输入菜单名称key' }],
    type: [{ required: true, message: '请选择菜单类型' }],
    name: [{ required: true, message: '请输入路由名称' }],
    path: [{ required: true, message: '请输入路由路径' }],
    component: [{ required: true, message: '请输入组件路径' }],
    hideInMenu: [{ required: true, message: '请选择显示状态' }],
  };

  const searchFormData = () => {
    return {
      name: '',
      tag: props.menuTag,
    };
  };
  const formModel = ref(searchFormData());
  // 顶级菜单下拉选项
  const pnoOptions = ref<OptionTree[]>([]);

  const menuData = reactive<MenuInfo[]>([]);
  const editData = reactive(new MenuPo({}));

  async function searchMenuTree() {
    setLoading(true);
    menuData.splice(0);
    pnoOptions.value.splice(0);
    await getMenuTreeList(formModel.value).then((rep: any) => {
      menuData.push(...rep.data);
      pnoOptions.value.push(...getPnoOptionTree(0, menuData));
    });
    setLoading(false);
  }

  searchMenuTree();

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

  const reset = () => {
    formModel.value = searchFormData();
    searchMenuTree();
  };

  function openAddPage(key: number) {
    editData.update({});
    editData.pno = key;
    drawerFlag.value.title = '新增菜单';
    drawerFlag.value.type = 1;
    visible.value = true;
  }
  function openEditPage(record: MenuInfo) {
    editData.update(record);
    drawerFlag.value.title = '修改菜单';
    drawerFlag.value.type = 2;
    visible.value = true;
  }
  function drawerSubmit() {
    editData.tag = props.menuTag;
    editData.status = 1;

    formRef.value.validate((valid: any) => {
      if (valid !== undefined) {
        Message.warning('请按要求填写');
        return;
      }

      if (drawerFlag.value.type === 1) {
        addMenu(editData).then(async (rep) => {
          if (rep.success) {
            Message.success(rep.msg);
            await searchMenuTree();
            await permissionStore.generateRoute(true);
            visible.value = false;
          }
        });
      } else {
        updateMenu(editData).then(async (rep) => {
          if (rep.success) {
            Message.success(rep.msg);
            await searchMenuTree();
            await permissionStore.generateRoute(true);
            visible.value = false;
          }
        });
      }
    });
  }

  function submitDelete(key: number) {
    deleteMenu(key).then(async (rep) => {
      if (rep.success) {
        Message.success(rep.msg);
        await searchMenuTree();
        await permissionStore.generateRoute(true);
      }
    });
  }
</script>
