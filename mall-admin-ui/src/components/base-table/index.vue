<template>
  <a-card>
    <a-row style="margin-top: 10px">
      <a-form :model="searchParam">
        <a-grid
          :cols="openMore ? 4 : 5"
          :col-gap="20"
          :row-gap="12"
          :collapsed="openMore"
        >
          <!-- 父组件自定义搜索条件 -->
          <slot name="search-item"></slot>
          <!-- 操作按钮组 -->
          <a-grid-item suffix class="grid-suffix-item">
            <a-space :size="10" class="toggle-btn-space">
              <a-button
                v-if="searchNum > 4 && !openMore"
                type="text"
                @click="handleCollapse"
              >
                <template #icon>
                  <icon-up />
                </template>
                {{ '收起' }}
              </a-button>
              <a-button
                v-if="searchNum > 4 && openMore"
                type="text"
                @click="toggleMore"
              >
                <template #icon>
                  <icon-down />
                </template>
                {{ '展开' }}
              </a-button>
            </a-space>
          </a-grid-item>
        </a-grid>
      </a-form>
    </a-row>
    <a-divider style="margin-top: 20px" />

    <!--操作按钮-->
    <a-row class="operation-btn-row">
      <a-space :size="14">
        <a-button type="primary" @click="handleSearch">
          <template #icon>
            <icon-search />
          </template>
          {{ '查询' }}
        </a-button>
        <a-button @click="handleReset">
          <template #icon>
            <icon-refresh />
          </template>
          {{ '重置' }}
        </a-button>
        <slot name="button"></slot>
      </a-space>
    </a-row>

    <div style="display: flex">
      <!-- 表格主体 -->
      <a-table
        v-model:selected-keys="selectedKeys"
        :row-key="rowKey"
        :data="tableData"
        :columns="columns"
        :pagination="!ifPage"
        :scroll="{ y: '60vh' }"
        :loading="loading"
        :row-selection="rowSelection"
        :style="{ width: tableWidth || '100%' }"
      >
        <!-- 动态插槽 -->
        <template
          v-for="col in getSlotColumns()"
          :key="col.slotName || col.dataIndex"
          #[col.slotName]="{ record, column, index }"
        >
          <!-- 父组件的自定义内容 -->
          <slot
            v-if="col.slotName"
            :name="col.slotName"
            :record="record"
            :column="column"
            :index="index"
          />
        </template>
      </a-table>
      <!-- 其他div -->
      <slot name="other-div"></slot>
    </div>
    <!-- 分页 -->
    <a-pagination
      v-if="ifPage"
      v-model:current="pageData.pageNumber"
      v-model:page-size="pageData.pageSize"
      :total="pageData.totalRow"
      show-page-size
      class="pagination_style"
      @change="handlePageChange"
      @page-size-change="handlePageChange"
    />
  </a-card>
</template>

<script setup lang="ts">
  import { onMounted, onUnmounted, reactive, ref, useSlots, watch } from 'vue';
  import type { TableColumnData } from '@arco-design/web-vue/es/table/interface';
  import PageData from '@/model/pageData';

  // 扩展列配置类型：增加slotName字段
  interface CustomTableColumnData extends TableColumnData {
    slotName?: string; // 插槽名，用于父组件自定义列渲染
  }

  const props = defineProps({
    searchParam: {
      type: Object as () => Record<string, any>,
      required: false,
      default: () => ({}),
    },
    tableData: {
      type: Array as () => Record<string, any>[],
      required: false,
      default: () => [],
    },
    columns: Array as () => CustomTableColumnData[],
    ifPage: {
      type: Boolean,
      default: true,
    },
    loading: {
      type: Boolean,
      default: false,
    },
    // 父组件传入初始化分页值（仅初始化用）
    initPageData: {
      type: Object as () => PageData,
      required: false,
      default: () => new PageData(),
    },
    // 行多选
    rowKey: {
      type: String,
      required: false,
    },
    rowSelection: {
      type: Object as () => {
        type: 'checkbox';
        showCheckedAll: boolean;
        onlyCurrent: boolean;
        selectable?: any;
      },
      required: false,
    },
    tableWidth: {
      type: String,
      required: false,
    },
  });

  // 过滤为slotName空的column
  const getSlotColumns = (): (CustomTableColumnData & {
    slotName: string;
  })[] => {
    if (!props.columns) return [];
    // 过滤出slotName为非空字符串的列，并强制类型断言
    return props.columns.filter((col) => {
      return typeof col.slotName === 'string' && col.slotName.trim() !== '';
    }) as (CustomTableColumnData & { slotName: string })[];
  };

  // 计算查询条件个数
  const slots = useSlots();
  const searchNum = ref(0);
  const calcSearchItemCount = async () => {
    // 1. 获取search-item插槽的所有VNode
    const searchSlot = slots['search-item'];
    if (!searchSlot) {
      searchNum.value = 0;
      return;
    }
    // 2. 执行插槽函数，获取真实VNode数组
    const slotVNodes = searchSlot();
    searchNum.value = slotVNodes.length || 0;
  };

  // 展开/收起搜索
  const openMore = ref(false);
  const toggleMore = () => {
    openMore.value = !openMore.value;
  };

  // 选择row
  const selectedKeys = ref<Array<string>>();

  const emit = defineEmits([
    'search', // 查询事件
    'reset', // 重置事件
    'pageChange', // 分页对象变化
    'collapse', // 收起按钮事件
    'getSelectedRow', // 行选择参数变化
  ]);

  // 触发查询
  const handleSearch = () => {
    emit('search');
  };

  // 触发重置
  const handleReset = () => {
    pageData.pageNumber = 1;
    pageData.pageSize = 10;
    selectedKeys.value?.splice(0);
    emit('getSelectedRow', { ...selectedKeys.value });
    emit('pageChange', { ...pageData });
    emit('reset');
  };

  // 监听父组件 分页对象变化
  let pageWatchStop: (() => void) | null = null;
  // 分页 子组件的分页对象变化时传给父组件
  const pageData = reactive(new PageData());
  onMounted(() => {
    calcSearchItemCount();

    if (props.initPageData) {
      pageData.pageNumber = props.initPageData.pageNumber || 1;
      pageData.pageSize = props.initPageData.pageSize || 10;
      pageData.totalRow = props.initPageData.totalRow || 0;
      pageData.totalPage = props.initPageData.totalPage || 0;
    }

    pageWatchStop = watch(
      () => props.initPageData,
      (newVal) => {
        if (newVal) {
          pageData.pageNumber = newVal.pageNumber;
          pageData.pageSize = newVal.pageSize;
          pageData.totalRow = newVal.totalRow;
          pageData.totalPage = newVal.totalPage;
        }
      },
      { deep: true }
    );
  });
  const handlePageChange = () => {
    emit('pageChange', { ...pageData });
    emit('search');
  };
  // 离开组件销毁监听器
  onUnmounted(() => {
    if (pageWatchStop) {
      pageWatchStop();
      pageWatchStop = null;
    }
  });

  // 点击收起按钮
  const handleCollapse = () => {
    emit('collapse');
    toggleMore();
  };

  // 获取选择行
  watch(
    () => selectedKeys.value,
    () => {
      emit('getSelectedRow', { ...selectedKeys.value });
    }
  );

  // 子组件同步父组件获取行keys
  function setSelectedKeys(keys: Array<string> | { [key: number]: string }) {
    const newRowArray = Array.isArray(keys) ? keys : Object.values(keys);
    selectedKeys.value?.splice(0);
    selectedKeys.value?.push(...newRowArray);
  }

  defineExpose({
    setSelectedKeys,
  });
</script>

<style scoped lang="less">
  :deep(.arco-card-body) {
    width: 100%;
  }

  .grid-suffix-item {
    // 强制 suffix 区域的容器宽度占满，确保按钮能靠右
    width: 100%;
    .toggle-btn-space {
      display: flex;
      justify-content: flex-end; // 核心：按钮靠右
      width: 100%;
    }
  }

  .operation-btn-row {
    display: flex;
    margin-bottom: 10px;
    justify-content: flex-end;
  }
</style>
