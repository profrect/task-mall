<template>
  <a-menu
    v-model:collapsed="collapsed"
    v-model:open-keys="openKeys"
    :mode="topMenu ? 'horizontal' : 'vertical'"
    :auto-open="false"
    :selected-keys="selectedKey"
    :auto-open-selected="true"
    :level-indent="34"
    style="height: 100%; width: 100%"
    :on-collapse="setCollapse"
  >
    <menu-item :menu-tree="menuTree" @handle-update-key="handleUpdateKey" />
  </a-menu>
</template>

<script setup lang="ts">
  import { ref, computed, shallowRef, onMounted } from 'vue';
  import { RouteLocationNormalizedGeneric, RouteRecordRaw } from 'vue-router';
  import { useAppStore } from '@/store';
  import { listenerRouteChange } from '@/utils/route-listener';
  import MenuItem from '@/components/menu/menuItem.vue';
  import useMenuTree from './use-menu-tree';

  const appStore = useAppStore();
  const { menuTree } = await useMenuTree();
  // 简化collapsed计算逻辑，减少computed触发次数
  const collapsed = computed({
    get() {
      return appStore.device === 'desktop' ? appStore.menuCollapse : false;
    },
    set(value: boolean) {
      if (appStore.device === 'desktop') {
        appStore.updateSettings({ menuCollapse: value });
      }
    },
  });

  const topMenu = computed(() => appStore.topMenu);
  const openKeys = ref<string[]>([]);
  const selectedKey = ref<string[]>([]);

  // 缓存findMenuOpenKeys结果，避免重复计算（
  const keyCache = new Map<string, string[]>(); // 缓存目标key对应的openKeys
  const findMenuOpenKeys = (target: string) => {
    // 命中缓存直接返回，无需重复遍历
    if (keyCache.has(target)) {
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      return keyCache.get(target)!;
    }
    const result: string[] = [];
    let isFind = false;
    const backtrack = (item: RouteRecordRaw, keys: string[]) => {
      if (isFind || !item) return; // 提前终止，减少无效遍历
      if (item.name === target) {
        isFind = true;
        result.push(...keys);
        return;
      }
      if (item.children?.length) {
        // forEach改为for循环，支持break（性能更高）
        // eslint-disable-next-line no-restricted-syntax
        for (const element of item.children) {
          const el = element;
          backtrack(el, [...keys, el.name as string]);
          if (isFind) break; // 找到后立即终止循环
        }
      }
    };
    // menuTree未加载完成时不遍历
    if (menuTree.value.length) {
      // eslint-disable-next-line no-restricted-syntax
      for (const element of menuTree.value) {
        const el = element;
        if (isFind) break;
        backtrack(el, [el.name as string]);
      }
    }
    // 存入缓存
    keyCache.set(target, result);
    return result;
  };

  // 避免路由监听触发两次
  let isFirstTrigger = true;
  listenerRouteChange((newRoute) => {
    // 过滤初始化时的重复触发（保留功能，只减少一次执行）
    if (isFirstTrigger) {
      isFirstTrigger = false;
      // 首次触发可延迟执行，让出主线程
      setTimeout(() => handleRouteChange(newRoute), 0);
    } else {
      handleRouteChange(newRoute);
    }
  }, true);

  // 抽离路由变化逻辑，便于维护和优化
  const handleRouteChange = (newRoute: RouteLocationNormalizedGeneric) => {
    const { activeMenu, hideInMenu } = newRoute.meta;
    if (!hideInMenu || activeMenu) {
      const targetKey = activeMenu || (newRoute.name as string);
      const menuOpenKeys = findMenuOpenKeys(targetKey);

      // 优化：避免不必要的数组操作（原Set逻辑可简化）
      if (JSON.stringify(openKeys.value) !== JSON.stringify(menuOpenKeys)) {
        openKeys.value = [...menuOpenKeys];
      }
      const newSelectedKey = [
        activeMenu || menuOpenKeys[menuOpenKeys.length - 1],
      ];
      if (
        JSON.stringify(selectedKey.value) !== JSON.stringify(newSelectedKey)
      ) {
        selectedKey.value = newSelectedKey;
      }
    }
  };

  const setCollapse = (val: boolean) => {
    if (appStore.device === 'desktop')
      appStore.updateSettings({ menuCollapse: val });
  };
  // 子组件控制父组件的selectedKey变化
  const handleUpdateKey = (newValue: string[]) => {
    selectedKey.value = newValue;
    keyCache.clear();
  };
</script>

<style lang="less" scoped>
  :deep(.arco-menu-inner) {
    .arco-menu-inline-header {
      display: flex;
      align-items: center;
    }
    .arco-icon {
      &:not(.arco-icon-down) {
        font-size: 18px;
      }
    }
  }

  :deep(.arco-menu-collapse-button) {
    bottom: auto;
  }
</style>
