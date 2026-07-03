<template>
  <a-menu
    class="module-menu"
    mode="horizontal"
    :selected-keys="selectedKeys"
  >
    <a-menu-item
      v-for="module in availableModules"
      :key="module.key"
      @click="() => handleModuleClick(module.key)"
    >
      {{ module.label }}
    </a-menu-item>
  </a-menu>
</template>

<script setup lang="ts">
  import { computed } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import type { RouteRecordRaw } from 'vue-router';
  import useMenuTree from './use-menu-tree';
  import {
    ADMIN_MENU_MODULES,
    findFirstNavigableRoute,
    getMenuModuleRoutes,
    resolveMenuModuleKey,
  } from './menu-modules';

  const route = useRoute();
  const router = useRouter();
  const { menuTree } = await useMenuTree();

  const currentRouteName = computed(() =>
    String(route.meta.activeMenu || route.name || '')
  );

  const availableModules = computed(() =>
    ADMIN_MENU_MODULES.filter((module) =>
      module.rootNames.some((rootName) =>
        menuTree.value.some((item: RouteRecordRaw) => item.name === rootName)
      )
    )
  );

  const activeModuleKey = computed(() =>
    resolveMenuModuleKey(menuTree.value, currentRouteName.value)
  );

  const selectedKeys = computed(() =>
    activeModuleKey.value ? [activeModuleKey.value] : []
  );

  const handleModuleClick = (moduleKey: string) => {
    if (moduleKey === activeModuleKey.value) return;

    const target = findFirstNavigableRoute(
      getMenuModuleRoutes(menuTree.value, moduleKey)
    );
    if (!target?.name) return;

    const targetName = target.name === 'dashboard' ? 'home' : target.name;
    router.push({ name: targetName as string }).catch((err) => {
      console.warn('顶部模块跳转失败:', err);
    });
  };
</script>

<style scoped lang="less">
  .module-menu {
    height: 100%;
    border-bottom: 0;
    background: transparent;
  }

  :deep(.arco-menu-inner) {
    display: flex;
    align-items: center;
    height: 100%;
    padding: 0;
  }

  :deep(.arco-menu-item) {
    height: 60px;
    margin: 0 4px;
    padding: 0 16px;
    line-height: 60px;
    font-weight: 500;
  }
</style>